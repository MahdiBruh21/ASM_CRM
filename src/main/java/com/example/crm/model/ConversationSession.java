package com.example.crm.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgvector.PGvector;
import com.example.crm.config.PGvectorType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "conversation_session")
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true)
    private String sessionId;

    @Column(name = "history", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode history;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Type(PGvectorType.class)
    @Column(name = "session_vector", columnDefinition = "vector(1536)")
    private PGvector sessionVector;

    @Column(name = "client_info", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode clientInfo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public JsonNode getHistory() { return history; }
    public void setHistory(JsonNode history) { this.history = history; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public PGvector getSessionVector() { return sessionVector; }
    public void setSessionVector(PGvector sessionVector) { this.sessionVector = sessionVector; }
    public void setSessionVectorFromArray(float[] embedding) {
        this.sessionVector = embedding != null ? new PGvector(embedding) : null;
    }
    public JsonNode getClientInfo() { return clientInfo; }
    public void setClientInfo(JsonNode clientInfo) { this.clientInfo = clientInfo; }
}