/*
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
import org.apache.http.client.methods.HttpGet;
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
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ProspectRepository prospectRepository;
    private final ProspectProfileRepository prospectProfileRepository;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    @Value("${meta.page.access.token}")
    private String pageAccessToken;

    @Value("${meta.whatsapp.access.token}")
    private String whatsappAccessToken;

    @Value("${meta.api.version}")
    private String metaApiVersion;

    public MessageServiceImpl(MessageRepository messageRepository, CustomerRepository customerRepository,
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
    public void processWebhookPayload(String payload) {
        try {
            System.out.println("Processing payload: " + payload);
            JsonNode rootNode = objectMapper.readTree(payload);
            String objectType = rootNode.has("object") ? rootNode.get("object").asText() : "";
            Platform platform;
            if ("instagram".equals(objectType)) {
                platform = Platform.INSTAGRAM;
            } else if ("whatsapp".equals(objectType)) {
                platform = Platform.WHATSAPP;
            } else {
                platform = Platform.FACEBOOK;
            }
            System.out.println("Detected platform: " + platform);

            JsonNode entries = rootNode.path("entry");
            if (entries.isEmpty()) {
                System.out.println("No entries found in payload: " + payload);
                return;
            }

            for (JsonNode entry : entries) {
                JsonNode messaging = platform == Platform.WHATSAPP ? entry.path("changes") : entry.path("messaging");
                if (messaging.isEmpty()) {
                    System.out.println("No messaging/changes field found in entry for " + platform + ": " + entry.toString());
                    continue;
                }
                for (JsonNode event : messaging) {
                    System.out.println("Processing event for " + platform + ": " + event.toString());
                    String senderId;
                    String recipientId;
                    JsonNode messageNode;

                    if (platform == Platform.WHATSAPP) {
                        JsonNode valueNode = event.path("value");
                        if (!valueNode.has("contacts") || !valueNode.has("messages") || !valueNode.has("metadata")) {
                            System.out.println("Invalid WhatsApp payload structure: " + valueNode.toString());
                            continue;
                        }
                        senderId = valueNode.path("contacts").path(0).path("wa_id").asText("");
                        recipientId = valueNode.path("metadata").path("display_phone_number").asText("");
                        messageNode = valueNode.path("messages").path(0);
                        if (senderId.isEmpty() || recipientId.isEmpty()) {
                            System.out.println("Missing senderId or recipientId in WhatsApp payload: " + valueNode.toString());
                            continue;
                        }
                    } else {
                        senderId = event.path("sender").path("id").asText("");
                        recipientId = event.path("recipient").path("id").asText("");
                        messageNode = event.path("message");
                        if (senderId.isEmpty() || recipientId.isEmpty()) {
                            System.out.println("Missing senderId or recipientId in payload for " + platform + ": " + event.toString());
                            continue;
                        }
                    }

                    System.out.println("Processing message for " + platform + ": senderId=" + senderId + ", recipientId=" + recipientId + ", messageNode=" + messageNode.toString());

                    // Create or update Prospect and ProspectProfile
                    Prospect prospect = createOrUpdateProspect(senderId, platform);

                    // Save Message
                    Message message = new Message();
                    message.setPlatform(platform);
                    message.setSenderId(senderId);
                    message.setRecipientId(recipientId);
                    message.setTimestamp(LocalDateTime.now());

                    // Try multiple text fields for flexibility
                    String text = null;
                    if (messageNode.has("text")) {
                        if (messageNode.path("text").has("body")) {
                            text = messageNode.path("text").path("body").asText(null);
                        } else {
                            text = messageNode.path("text").asText(null);
                        }
                    } else if (messageNode.has("message")) {
                        text = messageNode.path("message").asText(null);
                    }

                    if (text != null && !text.isEmpty()) {
                        message.setMessageText(text);
                        System.out.println("Text message received: " + text);
                    } else {
                        message.setMessageText("No text content");
                        System.out.println("No text content in event for " + platform + ": " + messageNode.toString());
                    }

                    // Link to Customer (if exists)
                    Optional<Customer> customer = findCustomerBySenderId(senderId);
                    customer.ifPresent(message::setCustomer);

                    messageRepository.save(message);
                    System.out.println("Saved message from " + platform + ": " + message.getMessageText());

                    // Handle interaction (simple response)
                    handleInteraction(message);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to process webhook payload: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Failed to process webhook payload: " + e.getMessage(), e);
        }
    }

    private Prospect createOrUpdateProspect(String senderId, Platform platform) {
        Optional<ProspectProfile> existingProfile = prospectProfileRepository.findBySenderId(senderId);
        if (existingProfile.isPresent()) {
            return existingProfile.get().getProspect();
        }

        String accessToken = platform == Platform.WHATSAPP ? whatsappAccessToken : pageAccessToken;

        String name = "Unknown";
        String profileLink;
        switch (platform) {
            case FACEBOOK:
                profileLink = "https://www.facebook.com/" + senderId;
                break;
            case INSTAGRAM:
                profileLink = "https://www.instagram.com/user/" + senderId;
                break;
            case WHATSAPP:
                profileLink = "https://wa.me/" + senderId;
                break;
            default:
                profileLink = "";
        }

        try {
            String url = String.format("https://graph.facebook.com/%s/%s?fields=name,username&access_token=%s",
                    metaApiVersion, senderId, accessToken);
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode node = objectMapper.readTree(responseBody);
                if (node.has("name")) {
                    name = node.get("name").asText();
                }
                if (platform == Platform.INSTAGRAM && node.has("username")) {
                    profileLink = "https://www.instagram.com/" + node.get("username").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch sender details for " + platform + ": " + e.getMessage());
        }

        Prospect prospect = new Prospect();
        prospect.setName(name);
        prospect.setProspectStatus(ProspectStatus.NEW);
        prospect.setProspectionType(switch (platform) {
            case FACEBOOK -> ProspectionType.FACEBOOK;
            case INSTAGRAM -> ProspectionType.INSTAGRAM;
            case WHATSAPP -> ProspectionType.WHATSAPP;
        });
        prospect.setProspectDetails("Auto-created from " + platform + " message");
        prospect = prospectRepository.save(prospect);

        ProspectProfile profile = new ProspectProfile();
        profile.setPlatform(platform);
        profile.setSenderId(senderId);
        profile.setProfileLink(profileLink);
        profile.setProspect(prospect);
        prospectProfileRepository.save(profile);

        System.out.println("Created Prospect: " + name + " with Profile: " + platform + ", Sender ID: " + senderId);
        return prospect;
    }

    private void handleInteraction(Message message) {
        String responseText = "مرحبا! Bienvenue! Welcome! Thank you for your message. How can we assist you?";
        sendMessage(message.getSenderId(), responseText, null, message.getPlatform().name());
    }

    @Override
    public void sendMessage(String recipientId, String messageText, String quickReplies, String platform) {
        try {
            String accessToken = platform.equals(Platform.WHATSAPP.name()) ? whatsappAccessToken : pageAccessToken;

            String endpoint = String.format("https://graph.facebook.com/%s/me/messages?access_token=%s",
                    metaApiVersion, accessToken);
            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            String jsonBody = """
                    {
                        "recipient": {"id": "%s"},
                        "message": {"text": "%s"}
                    }
                    """.formatted(recipientId, messageText.replace("\"", "\\\""));
            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            System.out.println("Sending Meta API request to " + endpoint + ": " + jsonBody);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Meta API response: " + responseBody);
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("Meta API error: " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send message via Meta API: " + e.getMessage());
            e.printStackTrace();
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
}*/
