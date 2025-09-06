package com.example.crm.dto;

import com.example.crm.enums.Platform;
import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private Platform platform;
    private String senderId;
    private String recipientId;
    private String sessionId; // New field for bidirectional session ID
    private String messageText;
    private String buttonPayload;
    private LocalDateTime timestamp;
    private CustomerDTO customer;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public String getButtonPayload() { return buttonPayload; }
    public void setButtonPayload(String buttonPayload) { this.buttonPayload = buttonPayload; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }
}