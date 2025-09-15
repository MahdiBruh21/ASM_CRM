package com.example.crm.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "conversation_session")
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode history;

    @Column
    private String summary;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode clientInfo;

    @Column(name = "is_rag_processed", nullable = false)
    private boolean isRagProcessed = false;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public JsonNode getHistory() {
        return history;
    }

    public void setHistory(JsonNode history) {
        this.history = history;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public JsonNode getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(JsonNode clientInfo) {
        this.clientInfo = clientInfo;
    }

    public boolean isRagProcessed() {
        return isRagProcessed;
    }

    public void setIsRagProcessed(boolean isRagProcessed) {
        this.isRagProcessed = isRagProcessed;
    }
}