package com.example.crm.service.impl;

import com.example.crm.dto.ConversationSessionDTO;
import com.example.crm.model.Message;
import com.example.crm.model.ConversationSession;
import com.example.crm.repository.ConversationSessionRepository;
import com.example.crm.service.interfaces.ConversationSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationSessionServiceImpl implements ConversationSessionService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationSessionServiceImpl.class);

    private final ConversationSessionRepository repository;
    private final ObjectMapper objectMapper;

    public ConversationSessionServiceImpl(ConversationSessionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void processMessage(Message userMessage, Message chatbotMessage) {
        logger.info("Processing message for sessionId: {}, userMessage: {}", userMessage.getSessionId(), userMessage.getMessageText());

        ConversationSession session = repository.findBySessionId(userMessage.getSessionId())
                .orElse(new ConversationSession());
        session.setSessionId(userMessage.getSessionId());

        // Update history
        JsonNode history = session.getHistory() != null ? session.getHistory() : objectMapper.createArrayNode();
        ArrayNode historyArray = (ArrayNode) history;

        // Add user message
        ObjectNode userMessageNode = objectMapper.createObjectNode();
        userMessageNode.put("role", "user");
        userMessageNode.put("content", userMessage.getMessageText());
        userMessageNode.put("timestamp", userMessage.getTimestamp().toString());
        historyArray.add(userMessageNode);

        // Add chatbot response
        if (chatbotMessage != null) {
            ObjectNode chatbotMessageNode = objectMapper.createObjectNode();
            chatbotMessageNode.put("role", "chatbot");
            chatbotMessageNode.put("content", chatbotMessage.getMessageText());
            chatbotMessageNode.put("timestamp", chatbotMessage.getTimestamp().toString());
            historyArray.add(chatbotMessageNode);
        }

        session.setHistory(historyArray);

        // Update client info
        JsonNode clientInfo = session.getClientInfo() != null ? session.getClientInfo() : objectMapper.createObjectNode();
        ObjectNode clientInfoNode = (ObjectNode) clientInfo;
        clientInfoNode.put("clientId", userMessage.getCustomer() != null ? userMessage.getCustomer().getId().toString() : null);
        clientInfoNode.put("clientPhoneNumber", userMessage.getSenderId());
        clientInfoNode.put("clientAddress", (String) null);
        clientInfoNode.put("clientComplaint", (String) null);
        session.setClientInfo(clientInfoNode);

        // Generate summary (placeholder)
        String summary = generateSummary(historyArray);
        session.setSummary(summary);

        // Set is_rag_processed to false for user message
        session.setIsRagProcessed(false);

        try {
            ConversationSession savedSession = repository.save(session);
            logger.info("Successfully saved session for sessionId: {}, id: {}, history: {}, is_rag_processed: {}",
                    userMessage.getSessionId(), savedSession.getId(), historyArray, savedSession.isRagProcessed());
        } catch (Exception e) {
            logger.error("Error saving session for sessionId: {}: {}", userMessage.getSessionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save session: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationSessionDTO getSessionById(String sessionId) {
        ConversationSession session = repository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        return mapToDTO(session);
    }

    private ConversationSessionDTO mapToDTO(ConversationSession session) {
        ConversationSessionDTO dto = new ConversationSessionDTO();
        dto.setId(session.getId());
        dto.setSessionId(session.getSessionId());
        dto.setHistory(session.getHistory());
        dto.setSummary(session.getSummary());
        dto.setClientInfo(session.getClientInfo());
        dto.setIsRagProcessed(session.isRagProcessed());
        return dto;
    }

    private String generateSummary(ArrayNode history) {
        // Placeholder: Call external summarization model
        return "Summary of conversation for session: User and chatbot discussed issues.";
    }
}