package com.example.crm.service.impl;

import com.example.crm.dto.ConversationSessionDTO;
import com.example.crm.model.ConversationSession;
import com.example.crm.model.Message;
import com.example.crm.repository.ConversationSessionRepository;
import com.example.crm.service.interfaces.ConversationSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgvector.PGvector;
import org.springframework.stereotype.Service;

@Service
public class ConversationSessionServiceImpl implements ConversationSessionService {

    private final ConversationSessionRepository repository;
    private final ObjectMapper objectMapper;

    public ConversationSessionServiceImpl(ConversationSessionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void processMessage(Message userMessage, Message chatbotMessage) {
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

        // Generate embedding (placeholder)
        float[] embedding = generateEmbedding(historyArray.toString());
        session.setSessionVectorFromArray(embedding);

        try {
            repository.save(session);
        } catch (Exception e) {
            System.err.println("Error saving session: " + e.getMessage());
            throw new RuntimeException("Failed to save session: " + e.getMessage(), e);
        }
    }

    @Override
    public ConversationSessionDTO getSessionById(String sessionId) {
        ConversationSession session = repository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        return mapToDTO(session);
    }

    @Override
    public ConversationSessionDTO findNearestSession(float[] queryVector) {
        ConversationSession session = repository.findNearestNeighbor(new PGvector(queryVector))
                .orElseThrow(() -> new RuntimeException("No similar session found"));
        return mapToDTO(session);
    }

    private ConversationSessionDTO mapToDTO(ConversationSession session) {
        ConversationSessionDTO dto = new ConversationSessionDTO();
        dto.setId(session.getId());
        dto.setSessionId(session.getSessionId());
        dto.setHistory(session.getHistory());
        dto.setSummary(session.getSummary());
        try {
            dto.setSessionVector(session.getSessionVector() != null ? session.getSessionVector().toArray() : null);
        } catch (Exception e) {
            System.err.println("Error converting PGvector to array: " + e.getMessage());
            dto.setSessionVector(null);
        }
        dto.setClientInfo(session.getClientInfo());
        return dto;
    }

    private String generateSummary(ArrayNode history) {
        // Placeholder: Call external summarization model (e.g., OpenAI)
        return "Summary of conversation for session: User and chatbot discussed issues.";
    }

    private float[] generateEmbedding(String text) {
        // Placeholder: Return null until actual embedding service is integrated
        return null; // Replace with actual embedding logic (e.g., OpenAI API)
    }
}