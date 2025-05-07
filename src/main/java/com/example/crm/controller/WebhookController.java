package com.example.crm.controller;

import com.example.crm.service.interfaces.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final String VERIFY_TOKEN = "testoken"; // Replace with your actual verify token
    private final MessageService messageService;

    public WebhookController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {
        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            System.out.println("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }

    @PostMapping
    public ResponseEntity<String> handleMessage(@RequestBody String payload) {
        try {
            messageService.processWebhookPayload(payload);
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (IllegalArgumentException e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing webhook: " + e.getMessage());
        }
    }
}