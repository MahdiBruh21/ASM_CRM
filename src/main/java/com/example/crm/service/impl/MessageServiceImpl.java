package com.example.crm.service.impl;

import com.example.crm.dto.CustomerDTO;
import com.example.crm.dto.MessageDTO;
import com.example.crm.enums.Platform;
import com.example.crm.model.Customer;
import com.example.crm.model.Message;
import com.example.crm.repository.CustomerRepository;
import com.example.crm.repository.MessageRepository;
import com.example.crm.service.interfaces.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    public MessageServiceImpl(MessageRepository messageRepository, CustomerRepository customerRepository, ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.customerRepository = customerRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void processWebhookPayload(String payload) {
        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            JsonNode entries = rootNode.path("entry");
            String platformStr = rootNode.has("object") && "instagram".equals(rootNode.get("object").asText()) ? "INSTAGRAM" : "FACEBOOK";
            Platform platform = Platform.valueOf(platformStr);

            for (JsonNode entry : entries) {
                JsonNode messaging = entry.path("messaging");
                for (JsonNode event : messaging) {
                    Message message = new Message();
                    message.setPlatform(platform);
                    message.setSenderId(event.path("sender").path("id").asText());
                    message.setRecipientId(event.path("recipient").path("id").asText());
                    message.setMessageText(event.path("message").path("text").asText());
                    message.setTimestamp(LocalDateTime.now());

                    // Optional: Link to Customer (e.g., based on senderId or custom field)
                    Optional<Customer> customer = findCustomerBySenderId(message.getSenderId());
                    customer.ifPresent(message::setCustomer);

                    messageRepository.save(message);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to process webhook payload: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Optional<Customer> findCustomerBySenderId(String senderId) {
        // Placeholder: Implement logic to map senderId to Customer
        // Example: Match against a new field in Customer (e.g., facebookId)
        return Optional.empty(); // Update with actual logic
    }

    private MessageDTO toDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setPlatform(message.getPlatform());
        dto.setSenderId(message.getSenderId());
        dto.setRecipientId(message.getRecipientId());
        dto.setMessageText(message.getMessageText());
        dto.setTimestamp(message.getTimestamp());
        if (message.getCustomer() != null) {
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(message.getCustomer().getId());
            customerDTO.setName(message.getCustomer().getName());
            customerDTO.setEmail(message.getCustomer().getEmail());
            customerDTO.setAddress(message.getCustomer().getAddress());
            customerDTO.setCustomerType(message.getCustomer().getCustomerType());
            customerDTO.setPhone(message.getCustomer().getPhone());
            customerDTO.setLeadSourceProspectId(message.getCustomer().getLeadSourceProspectId());
            dto.setCustomer(customerDTO);
        }
        return dto;
    }
}