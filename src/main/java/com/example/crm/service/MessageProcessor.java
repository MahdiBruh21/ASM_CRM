package com.example.crm.service;

import com.example.crm.dto.ConversationSessionDTO;
import com.example.crm.enums.Platform;
import com.example.crm.model.Message;
import com.example.crm.repository.MessageRepository;
import com.example.crm.service.interfaces.ConversationSessionService;
import com.example.crm.service.interfaces.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageProcessor {

    private final MessageService facebookMessageService;
    private final MessageService instagramMessageService;
    private final MessageService whatsappMessageService;
    private final MessageRepository messageRepository;
    private final ConversationSessionService conversationSessionService;
    private final ObjectMapper objectMapper;

    public MessageProcessor(
            @Qualifier("facebookMessageService") MessageService facebookMessageService,
            @Qualifier("instagramMessageService") MessageService instagramMessageService,
            @Qualifier("whatsappMessageService") MessageService whatsappMessageService,
            MessageRepository messageRepository,
            ConversationSessionService conversationSessionService,
            ObjectMapper objectMapper) {
        this.facebookMessageService = facebookMessageService;
        this.instagramMessageService = instagramMessageService;
        this.whatsappMessageService = whatsappMessageService;
        this.messageRepository = messageRepository;
        this.conversationSessionService = conversationSessionService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "facebook-message-queue")
    public void processFacebookMessage(String messageJson) throws Exception {
        processMessage(messageJson, Platform.FACEBOOK, facebookMessageService);
    }

    @RabbitListener(queues = "instagram-message-queue")
    public void processInstagramMessage(String messageJson) throws Exception {
        processMessage(messageJson, Platform.INSTAGRAM, instagramMessageService);
    }

    @RabbitListener(queues = "whatsapp-message-queue")
    public void processWhatsAppMessage(String messageJson) throws Exception {
        processMessage(messageJson, Platform.WHATSAPP, whatsappMessageService);
    }

    private void processMessage(String messageJson, Platform platform, MessageService messageService) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String senderId = jsonNode.get("senderId").asText();
            String recipientId = jsonNode.get("recipientId").asText();
            String messageText = jsonNode.get("messageText").asText();
            String sessionId = jsonNode.get("sessionId").asText();

            System.out.println(platform + ": Processing message from queue: " + messageText);

            Message userMessage = new Message();
            userMessage.setPlatform(platform);
            userMessage.setSenderId(senderId);
            userMessage.setRecipientId(recipientId);
            userMessage.setSessionId(sessionId);
            userMessage.setMessageText(messageText);
            userMessage.setTimestamp(LocalDateTime.now());
            messageRepository.save(userMessage);

            String chatbotResponse = getLastChatbotMessage(sessionId);
            if (chatbotResponse == null) {
                chatbotResponse = "مرحبا! Bienvenue! Welcome! This is a placeholder response from " + platform;
            }

            messageService.sendMessage(senderId, chatbotResponse, null, platform.name(), sessionId);

            Message chatbotMessage = new Message();
            chatbotMessage.setPlatform(platform);
            chatbotMessage.setSenderId(recipientId); // Chatbot as sender
            chatbotMessage.setRecipientId(senderId); // User as recipient
            chatbotMessage.setSessionId(sessionId);
            chatbotMessage.setMessageText(chatbotResponse);
            chatbotMessage.setTimestamp(LocalDateTime.now());
            messageRepository.save(chatbotMessage);

            conversationSessionService.processMessage(userMessage, chatbotMessage);

            System.out.println(platform + ": Sent response: " + chatbotResponse);
        } catch (Exception e) {
            System.err.println(platform + ": Error processing message: " + e.getMessage());
            throw e; // Re-throw to allow RabbitMQ retry/DLQ handling
        }
    }

    private String getLastChatbotMessage(String sessionId) {
        try {
            ConversationSessionDTO session = conversationSessionService.getSessionById(sessionId);
            JsonNode history = session.getHistory();
            if (history != null && history.isArray()) {
                for (int i = history.size() - 1; i >= 0; i--) {
                    JsonNode messageNode = history.get(i);
                    if (messageNode.has("role") && "chatbot".equals(messageNode.get("role").asText())) {
                        return messageNode.get("content").asText();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching last chatbot message for session " + sessionId + ": " + e.getMessage());
            return null;
        }
    }
}