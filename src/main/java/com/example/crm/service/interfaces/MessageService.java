package com.example.crm.service.interfaces;

import com.example.crm.dto.MessageDTO;
import java.util.List;

public interface MessageService {
    void processWebhookPayload(String payload);
    List<MessageDTO> getAllMessages();
}