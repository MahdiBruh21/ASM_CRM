package com.example.crm.controller;

import com.example.crm.dto.ConversationSessionDTO;
import com.example.crm.service.interfaces.ConversationSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationSessionController {

    private final ConversationSessionService service;

    public ConversationSessionController(ConversationSessionService service) {
        this.service = service;
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ConversationSessionDTO> getSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(service.getSessionById(sessionId));
    }

    @PostMapping("/rag")
    public ResponseEntity<ConversationSessionDTO> findSimilarSession(@RequestBody float[] queryVector) {
        return ResponseEntity.ok(service.findNearestSession(queryVector));
    }}