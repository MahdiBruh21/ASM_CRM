package com.example.crm.repository;

import com.example.crm.model.ConversationSession;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {
    Optional<ConversationSession> findBySessionId(String sessionId);

    @Query(value = "SELECT * FROM conversation_session WHERE session_vector IS NOT NULL ORDER BY session_vector <-> ?1 LIMIT 1", nativeQuery = true)
    Optional<ConversationSession> findNearestNeighbor(PGvector vector);
}