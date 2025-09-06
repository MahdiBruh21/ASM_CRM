package com.example.crm.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class ConversationSessionDTO {
    private Long id;
    private String sessionId;
    private JsonNode history;
    private String summary;
    private float[] sessionVector;
    private JsonNode clientInfo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public JsonNode getHistory() { return history; }
    public void setHistory(JsonNode history) { this.history = history; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public float[] getSessionVector() { return sessionVector; }
    public void setSessionVector(float[] sessionVector) { this.sessionVector = sessionVector; }
    public JsonNode getClientInfo() { return clientInfo; }
    public void setClientInfo(JsonNode clientInfo) { this.clientInfo = clientInfo; }
}