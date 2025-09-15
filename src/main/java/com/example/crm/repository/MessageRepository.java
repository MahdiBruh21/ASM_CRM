package com.example.crm.repository;

import com.example.crm.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
        Message findTopBySessionIdAndSenderIdAndMessageTextOrderByTimestampDesc(String sessionId, String senderId, String messageText);
        Message findTopBySessionIdAndSenderIdNotOrderByTimestampDesc(String sessionId, String senderId);
}