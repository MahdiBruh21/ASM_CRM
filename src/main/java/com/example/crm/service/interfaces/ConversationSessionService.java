package com.example.crm.service.interfaces;

import com.example.crm.dto.ConversationSessionDTO;
import com.example.crm.model.Message;

public interface ConversationSessionService {
    void processMessage(Message userMessage, Message chatbotMessage);
    ConversationSessionDTO getSessionById(String sessionId);
    ConversationSessionDTO findNearestSession(float[] queryVector);
}