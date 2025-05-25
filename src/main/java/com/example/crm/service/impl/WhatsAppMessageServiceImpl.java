package com.example.crm.service.impl;

import com.example.crm.dto.CustomerDTO;
import com.example.crm.dto.MessageDTO;
import com.example.crm.enums.Platform;
import com.example.crm.model.Customer;
import com.example.crm.model.Message;
import com.example.crm.model.Prospect;
import com.example.crm.model.ProspectProfile;
import com.example.crm.enums.ProspectStatus;
import com.example.crm.enums.ProspectionType;
import com.example.crm.repository.CustomerRepository;
import com.example.crm.repository.MessageRepository;
import com.example.crm.repository.ProspectRepository;
import com.example.crm.repository.ProspectProfileRepository;
import com.example.crm.service.interfaces.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WhatsAppMessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ProspectRepository prospectRepository;
    private final ProspectProfileRepository prospectProfileRepository;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    @Value("${meta.whatsapp.access.token}")
    private String whatsappAccessToken;

    @Value("${meta.api.version}")
    private String metaApiVersion;

    @Value("${meta.whatsapp.phone_number_id}")
    private String phoneNumberId;

    public WhatsAppMessageServiceImpl(MessageRepository messageRepository, CustomerRepository customerRepository,
                                      ProspectRepository prospectRepository, ProspectProfileRepository prospectProfileRepository,
                                      ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.customerRepository = customerRepository;
        this.prospectRepository = prospectRepository;
        this.prospectProfileRepository = prospectProfileRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    @Transactional
    public void processWebhookPayload(String payload, String platform) {
        if (!"whatsapp".equalsIgnoreCase(platform)) {
            System.out.println("WhatsApp service ignoring payload for platform: " + platform);
            return;
        }
        try {
            System.out.println("WhatsApp: Processing payload: " + payload);
            JsonNode rootNode = objectMapper.readTree(payload);
            System.out.println("WhatsApp: Parsed JSON structure: " + rootNode.toPrettyString());

            JsonNode entries = rootNode.path("entry");
            if (entries.isEmpty()) {
                System.out.println("WhatsApp: No entries found in payload: " + payload);
                return;
            }

            for (JsonNode entry : entries) {
                System.out.println("WhatsApp: Processing entry: " + entry.toPrettyString());
                JsonNode changes = entry.path("changes");
                if (changes.isEmpty()) {
                    System.out.println("WhatsApp: No changes field found in entry: " + entry.toPrettyString());
                    continue;
                }
                for (JsonNode change : changes) {
                    System.out.println("WhatsApp: Processing change: " + change.toPrettyString());
                    JsonNode valueNode = change.path("value");
                    if (!valueNode.has("messages") || !valueNode.has("metadata")) {
                        System.out.println("WhatsApp: Invalid payload structure: " + valueNode.toPrettyString());
                        continue;
                    }

                    JsonNode messages = valueNode.path("messages");
                    if (messages.isEmpty()) {
                        System.out.println("WhatsApp: No messages found in value: " + valueNode.toPrettyString());
                        continue;
                    }

                    String recipientId = valueNode.path("metadata").path("phone_number_id").asText("");
                    if (recipientId.isEmpty()) {
                        System.out.println("WhatsApp: Missing phone_number_id in metadata: " + valueNode.toPrettyString());
                        continue;
                    }

                    for (JsonNode messageNode : messages) {
                        String senderId = messageNode.path("from").asText("");
                        if (senderId.isEmpty()) {
                            System.out.println("WhatsApp: Missing senderId: " + messageNode.toPrettyString());
                            continue;
                        }

                        // Skip echo messages
                        boolean isEcho = messageNode.path("context").has("from");
                        if (isEcho) {
                            System.out.println("WhatsApp: Skipping echo message from senderId=" + senderId);
                            continue;
                        }

                        System.out.println("WhatsApp: Processing message: senderId=" + senderId + ", recipientId=" + recipientId + ", messageNode=" + messageNode.toPrettyString());

                        Prospect prospect = createOrUpdateProspect(senderId, valueNode);
                        Message message = new Message();
                        message.setPlatform(Platform.WHATSAPP);
                        message.setSenderId(senderId);
                        message.setRecipientId(recipientId);
                        message.setTimestamp(LocalDateTime.now());

                        String text = messageNode.path("text").path("body").asText(null);
                        if (text != null && !text.isEmpty()) {
                            message.setMessageText(text);
                            System.out.println("WhatsApp: Text message received: " + text);
                        } else {
                            message.setMessageText("No text content");
                            System.out.println("WhatsApp: No text content: " + messageNode.toPrettyString());
                        }

                        Optional<Customer> customer = findCustomerBySenderId(senderId);
                        customer.ifPresent(message::setCustomer);

                        messageRepository.save(message);
                        System.out.println("WhatsApp: Saved message: " + message.getMessageText());

                        handleInteraction(message);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("WhatsApp: Failed to process payload: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("WhatsApp: Failed to process payload: " + e.getMessage(), e);
        }
    }

    private Prospect createOrUpdateProspect(String senderId, JsonNode valueNode) {
        Optional<ProspectProfile> existingProfile = prospectProfileRepository.findBySenderId(senderId);
        if (existingProfile.isPresent()) {
            return existingProfile.get().getProspect();
        }

        String name = "Unknown";
        String profileLink = "https://wa.me/" + senderId;

        // Use contact name from payload
        JsonNode contacts = valueNode.path("contacts");
        if (!contacts.isEmpty()) {
            name = contacts.path(0).path("profile").path("name").asText("Unknown");
        }

        Prospect prospect = new Prospect();
        prospect.setName(name);
        prospect.setProspectStatus(ProspectStatus.NEW);
        prospect.setProspectionType(ProspectionType.WHATSAPP);
        prospect.setProspectDetails("Auto-created from WhatsApp message");
        prospect = prospectRepository.save(prospect);

        ProspectProfile profile = new ProspectProfile();
        profile.setPlatform(Platform.WHATSAPP);
        profile.setSenderId(senderId);
        profile.setProfileLink(profileLink);
        profile.setProspect(prospect);
        prospectProfileRepository.save(profile);

        System.out.println("WhatsApp: Created Prospect: " + name + ", Sender ID: " + senderId);
        return prospect;
    }

    private void handleInteraction(Message message) {
        String responseText = "مرحبا! Bienvenue! Welcome! Thank you for your message. How can we assist you?";
        sendMessage(message.getSenderId(), responseText, null, Platform.WHATSAPP.name());
    }

    @Override
    public void sendMessage(String recipientId, String messageText, String quickReplies, String platform) {
        if (!Platform.WHATSAPP.name().equalsIgnoreCase(platform)) {
            return;
        }
        try {
            String endpoint = String.format("https://graph.facebook.com/%s/%s/messages?access_token=%s",
                    metaApiVersion, phoneNumberId, whatsappAccessToken);
            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            String jsonBody = """
                    {
                        "messaging_product": "whatsapp",
                        "recipient_type": "individual",
                        "to": "%s",
                        "type": "text",
                        "text": {
                            "body": "%s"
                        }
                    }
                    """.formatted(recipientId, messageText.replace("\"", "\\\""));
            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            System.out.println("WhatsApp: Sending Meta API request to recipientId=" + recipientId + ": " + jsonBody);
            System.out.println("WhatsApp: Using access token (redacted): " + whatsappAccessToken.substring(0, 10) + "...");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("WhatsApp: Meta API response (status " + statusCode + "): " + responseBody);
                if (statusCode != 200) {
                    System.err.println("WhatsApp: Meta API error for recipientId=" + recipientId + ": " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("WhatsApp: Failed to send message to recipientId=" + recipientId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .filter(m -> m.getPlatform() == Platform.WHATSAPP)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Optional<Customer> findCustomerBySenderId(String senderId) {
        Optional<ProspectProfile> profile = prospectProfileRepository.findBySenderId(senderId);
        return profile.map(ProspectProfile::getProspect)
                .flatMap(p -> customerRepository.findByLeadSourceProspectId(p.getId()));
    }

    private MessageDTO toDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setPlatform(message.getPlatform());
        dto.setSenderId(message.getSenderId());
        dto.setRecipientId(message.getRecipientId());
        dto.setMessageText(message.getMessageText());
        dto.setButtonPayload(message.getButtonPayload());
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