package com.example.crm.service;

import com.example.crm.dto.ConversationSessionDTO;
import com.example.crm.enums.Platform;
import com.example.crm.model.Message;
import com.example.crm.repository.MessageRepository;
import com.example.crm.service.interfaces.ConversationSessionService;
import com.example.crm.service.interfaces.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

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

    @RabbitListener(queues = "facebook-message-queue", ackMode = "MANUAL")
    public void processFacebookMessage(String messageJson, Channel channel, org.springframework.amqp.core.Message amqpMessage) throws Exception {
        processMessage(messageJson, Platform.FACEBOOK, facebookMessageService, channel, amqpMessage);
    }

    @RabbitListener(queues = "instagram-message-queue", ackMode = "MANUAL")
    public void processInstagramMessage(String messageJson, Channel channel, org.springframework.amqp.core.Message amqpMessage) throws Exception {
        processMessage(messageJson, Platform.INSTAGRAM, instagramMessageService, channel, amqpMessage);
    }

    @RabbitListener(queues = "whatsapp-message-queue", ackMode = "MANUAL")
    public void processWhatsAppMessage(String messageJson, Channel channel, org.springframework.amqp.core.Message amqpMessage) throws Exception {
        processMessage(messageJson, Platform.WHATSAPP, whatsappMessageService, channel, amqpMessage);
    }

    @Transactional
    protected void processMessage(String messageJson, Platform platform, MessageService messageService, Channel channel, org.springframework.amqp.core.Message amqpMessage) throws Exception {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        String sessionId = "unknown";
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String senderId = jsonNode.get("senderId").asText();
            String recipientId = jsonNode.get("recipientId").asText();
            String messageText = jsonNode.get("messageText").asText();
            sessionId = jsonNode.get("sessionId").asText();

            logger.info("{}: Processing message from queue: {}, sessionId: {}, deliveryTag: {}", platform, messageText, sessionId, deliveryTag);

            // Check for duplicate message
            Message existingMessage = messageRepository.findTopBySessionIdAndSenderIdAndMessageTextOrderByTimestampDesc(
                    sessionId, senderId, messageText);
            if (existingMessage != null) {
                logger.info("{}: Duplicate message detected, skipping: {}, sessionId: {}, deliveryTag: {}", platform, messageText, sessionId, deliveryTag);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // Save user message
            Message userMessage = new Message();
            userMessage.setPlatform(platform);
            userMessage.setSenderId(senderId);
            userMessage.setRecipientId(recipientId);
            userMessage.setSessionId(sessionId);
            userMessage.setMessageText(messageText);
            userMessage.setTimestamp(LocalDateTime.now());
            messageRepository.save(userMessage);
            logger.info("{}: Saved user message to message table, sessionId: {}", platform, sessionId);

            // Log session state before update
            try {
                ConversationSessionDTO sessionBefore = conversationSessionService.getSessionById(sessionId);
                logger.info("{}: Session before update, sessionId: {}, id: {}, history: {}, is_rag_processed: {}",
                        platform, sessionId, sessionBefore.getId(), sessionBefore.getHistory(), sessionBefore.isRagProcessed());
            } catch (Exception e) {
                logger.warn("{}: Session not found before update, sessionId: {}", platform, sessionId);
            }

            // Update conversation session (triggers RAG pipeline)
            conversationSessionService.processMessage(userMessage, null);
            logger.info("{}: Updated conversation session, waiting for RAG pipeline response, sessionId: {}, deliveryTag: {}", platform, sessionId, deliveryTag);

            // Log session state after update
            try {
                ConversationSessionDTO sessionAfter = conversationSessionService.getSessionById(sessionId);
                logger.info("{}: Session after update, sessionId: {}, id: {}, history: {}, is_rag_processed: {}",
                        platform, sessionId, sessionAfter.getId(), sessionAfter.getHistory(), sessionAfter.isRagProcessed());
            } catch (Exception e) {
                logger.error("{}: Failed to fetch session after update, sessionId: {}, error: {}", platform, sessionId, e.getMessage());
            }

            // Acknowledge message
            channel.basicAck(deliveryTag, false);
            logger.info("{}: Acknowledged message, sessionId: {}, deliveryTag: {}", platform, sessionId, deliveryTag);
        } catch (Exception e) {
            logger.error("{}: Error processing message: {}, sessionId: {}, deliveryTag: {}", platform, e.getMessage(), sessionId, deliveryTag, e);
            // Reject message without requeueing
            channel.basicNack(deliveryTag, false, false);
            logger.info("{}: Rejected message without requeue, sessionId: {}, deliveryTag: {}", platform, sessionId, deliveryTag);
        }
    }
}