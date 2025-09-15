package com.example.crm.service;

import com.example.crm.dto.ConversationSessionDTO;
import com.example.crm.enums.Platform;
import com.example.crm.model.Message;
import com.example.crm.repository.MessageRepository;
import com.example.crm.service.interfaces.ConversationSessionService;
import com.example.crm.service.interfaces.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class ConversationNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(ConversationNotificationListener.class);

    private final ConversationSessionService conversationSessionService;
    private final MessageService facebookMessageService;
    private final MessageService instagramMessageService;
    private final MessageService whatsappMessageService;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final DataSource dataSource;
    private Connection connection;
    private Thread listenerThread;

    public ConversationNotificationListener(
            ConversationSessionService conversationSessionService,
            @Qualifier("facebookMessageService") MessageService facebookMessageService,
            @Qualifier("instagramMessageService") MessageService instagramMessageService,
            @Qualifier("whatsappMessageService") MessageService whatsappMessageService,
            MessageRepository messageRepository,
            ObjectMapper objectMapper,
            DataSource dataSource
    ) {
        this.conversationSessionService = conversationSessionService;
        this.facebookMessageService = facebookMessageService;
        this.instagramMessageService = instagramMessageService;
        this.whatsappMessageService = whatsappMessageService;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void startListening() {
        try {
            connection = dataSource.getConnection();
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("LISTEN conversation_history_changes");
                logger.info("Listening for conversation_history_changes notifications");
            }

            listenerThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        PGConnection pgConnection = connection.unwrap(PGConnection.class);
                        PGNotification[] notifications = pgConnection.getNotifications();
                        if (notifications != null) {
                            for (PGNotification notification : notifications) {
                                logger.info("Received notification on channel {}: {}", notification.getName(), notification.getParameter());
                                try {
                                    JsonNode payloadJson = objectMapper.readTree(notification.getParameter());
                                    String sessionId = payloadJson.get("session_id").asText();
                                    processChatbotResponse(sessionId);
                                } catch (Exception e) {
                                    logger.error("Error processing notification payload: {}", notification.getParameter(), e);
                                }
                            }
                        }
                        Thread.sleep(500); // Poll every 500ms
                    }
                } catch (SQLException | InterruptedException e) {
                    logger.error("Error in notification listener thread", e);
                }
            });
            listenerThread.start();
            logger.info("Started PostgreSQL notification listener thread");
        } catch (SQLException e) {
            logger.error("Failed to start PostgreSQL notification listener", e);
            throw new RuntimeException("Failed to start notification listener", e);
        }
    }

    @PreDestroy
    public void stopListening() {
        try {
            if (listenerThread != null) {
                listenerThread.interrupt();
            }
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("UNLISTEN conversation_history_changes");
                }
                connection.close();
                logger.info("Stopped listening for conversation_history_changes notifications");
            }
        } catch (SQLException e) {
            logger.error("Error stopping PostgreSQL notification listener", e);
        }
    }

    private void processChatbotResponse(String sessionId) {
        try {
            ConversationSessionDTO session = conversationSessionService.getSessionById(sessionId);
            JsonNode history = session.getHistory();
            if (history == null || !history.isArray() || history.size() == 0) {
                logger.warn("No history found for sessionId: {}, id: {}", sessionId, session.getId());
                return;
            }

            if (!session.isRagProcessed()) {
                logger.info("Session not processed by RAG for sessionId: {}, id: {}, skipping", sessionId, session.getId());
                return;
            }

            JsonNode lastMessage = history.get(history.size() - 1);
            if (!lastMessage.has("role") || !"chatbot".equals(lastMessage.get("role").asText())) {
                logger.info("Last message in sessionId: {}, id: {} is not from chatbot, skipping", sessionId, session.getId());
                return;
            }

            String chatbotResponse = lastMessage.get("content").asText();
            String platformStr = sessionId.split(":")[0];
            Platform platform;
            try {
                platform = Platform.valueOf(platformStr);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid platform in sessionId: {}, id: {}", sessionId, session.getId());
                return;
            }

            String recipientId = messageRepository.findTopBySessionIdAndSenderIdNotOrderByTimestampDesc(sessionId, "chatbot").getSenderId();

            // Check for duplicate chatbot response
            Message existingChatbotMessage = messageRepository.findTopBySessionIdAndSenderIdAndMessageTextOrderByTimestampDesc(
                    sessionId, "chatbot", chatbotResponse);
            if (existingChatbotMessage != null) {
                logger.info("Chatbot response already sent for sessionId: {}, id: {}, message: {}", sessionId, session.getId(), chatbotResponse);
                return;
            }

            // Send response
            MessageService messageService = switch (platform) {
                case FACEBOOK -> facebookMessageService;
                case INSTAGRAM -> instagramMessageService;
                case WHATSAPP -> whatsappMessageService;
            };
            messageService.sendMessage(recipientId, chatbotResponse, null, platform.name(), sessionId);

            // Save chatbot message
            Message chatbotMessage = new Message();
            chatbotMessage.setPlatform(platform);
            chatbotMessage.setSenderId("chatbot");
            chatbotMessage.setRecipientId(recipientId);
            chatbotMessage.setSessionId(sessionId);
            chatbotMessage.setMessageText(chatbotResponse);
            chatbotMessage.setTimestamp(LocalDateTime.now());
            messageRepository.save(chatbotMessage);

            logger.info("Sent chatbot response for sessionId: {}, id: {}, platform: {}, message: {}", sessionId, session.getId(), platform, chatbotResponse);
        } catch (Exception e) {
            logger.error("Error processing chatbot response for sessionId: {}", sessionId, e);
        }
    }
}