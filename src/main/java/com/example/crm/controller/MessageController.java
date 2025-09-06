package com.example.crm.controller;

import com.example.crm.dto.MessageDTO;
import com.example.crm.service.interfaces.MessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService whatsappMessageService;
    private final MessageService instagramMessageService;
    private final MessageService facebookMessageService;

    public MessageController(
            @Qualifier("whatsappMessageService") MessageService whatsappMessageService,
            @Qualifier("instagramMessageService") MessageService instagramMessageService,
            @Qualifier("facebookMessageService") MessageService facebookMessageService) {
        this.whatsappMessageService = whatsappMessageService;
        this.instagramMessageService = instagramMessageService;
        this.facebookMessageService = facebookMessageService;
    }

    @GetMapping
    public List<MessageDTO> getAllMessages() {
        List<MessageDTO> messages = new ArrayList<>();
        messages.addAll(whatsappMessageService.getAllMessages());
        messages.addAll(instagramMessageService.getAllMessages());
        messages.addAll(facebookMessageService.getAllMessages());
        return messages;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestParam String platform) {
        switch (platform.toLowerCase()) {
            case "facebook":
                facebookMessageService.processWebhookPayload(payload, platform);
                break;
            case "instagram":
                instagramMessageService.processWebhookPayload(payload, platform);
                break;
            case "whatsapp":
                whatsappMessageService.processWebhookPayload(payload, platform);
                break;
            default:
                return ResponseEntity.badRequest().body("Unsupported platform: " + platform);
        }
        return ResponseEntity.ok("Webhook processed");
    }
}