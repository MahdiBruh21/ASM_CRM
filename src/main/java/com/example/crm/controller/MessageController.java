package com.example.crm.controller;

import com.example.crm.dto.MessageDTO;
import com.example.crm.service.interfaces.MessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService whatsappMessageService;
    private final MessageService instagramMessageService;
    private final MessageService facebookMessageService;

    public MessageController(
            @Qualifier("whatsAppMessageServiceImpl") MessageService whatsappMessageService,
            @Qualifier("instagramMessageServiceImpl") MessageService instagramMessageService,
            @Qualifier("facebookMessageServiceImpl") MessageService facebookMessageService) {
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
}