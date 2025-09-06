
package com.example.crm.service.interfaces;

import com.example.crm.dto.MessageDTO;
import java.util.List;

public interface MessageService {
    void processWebhookPayload(String payload, String platform);
    void sendMessage(String recipientId, String messageText, String quickReplies, String platform, String sessionId);
    List<MessageDTO> getAllMessages();
}
