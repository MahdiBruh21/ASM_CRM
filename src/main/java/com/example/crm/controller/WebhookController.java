package com.example.crm.controller;

import com.example.crm.service.interfaces.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final MessageService whatsappMessageService;
    private final MessageService instagramMessageService;
    private final MessageService facebookMessageService;
    private final ObjectMapper objectMapper;

    @Value("${meta.verify.token}")
    private String verifyToken;

    public WebhookController(
            @Qualifier("whatsappMessageService") MessageService whatsappMessageService,
            @Qualifier("instagramMessageService") MessageService instagramMessageService,
            @Qualifier("facebookMessageService") MessageService facebookMessageService,
            ObjectMapper objectMapper) {
        this.whatsappMessageService = whatsappMessageService;
        this.instagramMessageService = instagramMessageService;
        this.facebookMessageService = facebookMessageService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") String mode,
                                                @RequestParam("hub.verify_token") String token,
                                                @RequestParam("hub.challenge") String challenge,
                                                HttpServletRequest request) {
        String headers = Collections.list(request.getHeaderNames()).stream()
                .map(name -> name + ": " + request.getHeader(name))
                .collect(Collectors.joining(", "));
        System.out.println("Received webhook GET request: mode=" + mode + ", token=" + token + ", challenge=" + challenge + ", headers=[" + headers + "]");

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            System.out.println("Webhook verified successfully!");
            return ResponseEntity.ok(challenge);
        } else {
            System.err.println("Webhook verification failed: mode=" + mode + ", token=" + token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload, HttpServletRequest request) {
        String headers = Collections.list(request.getHeaderNames()).stream()
                .map(name -> name + ": " + request.getHeader(name))
                .collect(Collectors.joining(", "));
        System.out.println("Received webhook POST request: payload=" + payload + ", headers=[" + headers + "]");

        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            String objectType = rootNode.has("object") ? rootNode.get("object").asText() : "";
            String platform;

            switch (objectType) {
                case "whatsapp_business_account":
                    platform = "whatsapp";
                    whatsappMessageService.processWebhookPayload(payload, platform);
                    break;
                case "instagram":
                    platform = "instagram";
                    instagramMessageService.processWebhookPayload(payload, platform);
                    break;
                case "page":
                    platform = "facebook";
                    facebookMessageService.processWebhookPayload(payload, platform);
                    break;
                default:
                    System.err.println("Unknown object type: " + objectType);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            System.out.println("Webhook payload processed successfully for platform: " + platform);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error processing webhook payload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.DELETE}, produces = "application/json")
    public ResponseEntity<String> handleUnexpectedRequests(HttpServletRequest request) {
        String headers = Collections.list(request.getHeaderNames()).stream()
                .map(name -> name + ": " + request.getHeader(name))
                .collect(Collectors.joining(", "));
        String method = request.getMethod();
        String queryString = request.getQueryString() != null ? request.getQueryString() : "";
        System.out.println("Received unexpected " + method + " request to /api/webhook: query=" + queryString + ", headers=[" + headers + "]");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Method not allowed: " + method);
    }
}