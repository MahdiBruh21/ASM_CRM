package com.example.crm.dto;

import com.example.crm.enums.Platform;
import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private Platform platform;
    private String senderId;
    private String recipientId;
    private String messageText;
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
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }
}