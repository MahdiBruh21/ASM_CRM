
        package com.example.crm.service.impl;

import com.example.crm.config.RabbitMQConfig;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("facebookMessageService")
public class FacebookMessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ProspectRepository prospectRepository;
    private final ProspectProfileRepository prospectProfileRepository;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${meta.page.access.token}")
    private String pageAccessToken;

    @Value("${meta.api.version}")
    private String metaApiVersion;

    public FacebookMessageServiceImpl(MessageRepository messageRepository, CustomerRepository customerRepository,
                                      ProspectRepository prospectRepository, ProspectProfileRepository prospectProfileRepository,
                                      ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.messageRepository = messageRepository;
        this.customerRepository = customerRepository;
        this.prospectRepository = prospectRepository;
        this.prospectProfileRepository = prospectProfileRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClients.createDefault();
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public void processWebhookPayload(String payload, String platform) {
        if (!"facebook".equalsIgnoreCase(platform)) {
            System.out.println("Facebook service ignoring payload for platform: " + platform);
            return;
        }
        try {
            System.out.println("Facebook: Processing payload: " + payload);
            JsonNode rootNode = objectMapper.readTree(payload);
            System.out.println("Facebook: Parsed JSON structure: " + rootNode.toPrettyString());

            JsonNode entries = rootNode.path("entry");
            if (entries.isEmpty()) {
                System.out.println("Facebook: No entries found in payload: " + payload);
                return;
            }

            for (JsonNode entry : entries) {
                System.out.println("Facebook: Processing entry: " + entry.toPrettyString());
                JsonNode messaging = entry.path("messaging");
                if (messaging.isEmpty()) {
                    System.out.println("Facebook: No messaging field found in entry: " + entry.toPrettyString());
                    continue;
                }
                for (JsonNode event : messaging) {
                    System.out.println("Facebook: Processing event: " + event.toPrettyString());
                    String senderId = event.path("sender").path("id").asText("");
                    String recipientId = event.path("recipient").path("id").asText("");
                    JsonNode messageNode = event.path("message");
                    if (senderId.isEmpty() || recipientId.isEmpty()) {
                        System.out.println("Facebook: Missing senderId or recipientId: " + event.toPrettyString());
                        continue;
                    }

                    System.out.println("Facebook: Processing message: senderId=" + senderId + ", recipientId=" + recipientId + ", messageNode=" + messageNode.toPrettyString());

                    String sessionId = generateSessionId(senderId, recipientId);

                    Prospect prospect = createOrUpdateProspect(senderId);
                    Message message = new Message();
                    message.setPlatform(Platform.FACEBOOK);
                    message.setSenderId(senderId);
                    message.setRecipientId(recipientId);
                    message.setSessionId(sessionId);
                    message.setTimestamp(LocalDateTime.now());

                    String text = null;
                    if (messageNode.has("text")) {
                        text = messageNode.path("text").asText(null);
                    } else if (messageNode.has("message")) {
                        text = messageNode.path("message").asText(null);
                    }

                    if (text != null && !text.isEmpty()) {
                        message.setMessageText(text);
                        System.out.println("Facebook: Text message received: " + text);
                    } else {
                        message.setMessageText("No text content");
                        System.out.println("Facebook: No text content: " + messageNode.toPrettyString());
                    }

                    Optional<Customer> customer = findCustomerBySenderId(senderId);
                    customer.ifPresent(message::setCustomer);

                    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.FACEBOOK_ROUTING_KEY, objectMapper.writeValueAsString(message));
                    System.out.println("Facebook: Published message to RabbitMQ: " + message.getMessageText());
                }
            }
        } catch (Exception e) {
            System.err.println("Facebook: Failed to process payload: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Facebook: Failed to process payload: " + e.getMessage(), e);
        }
    }

    private String generateSessionId(String senderId, String recipientId) {
        String id1 = senderId.compareTo(recipientId) < 0 ? senderId : recipientId;
        String id2 = senderId.compareTo(recipientId) < 0 ? recipientId : senderId;
        return Platform.FACEBOOK + ":" + id1 + ":" + id2;
    }

    private Prospect createOrUpdateProspect(String senderId) {
        Optional<ProspectProfile> existingProfile = prospectProfileRepository.findBySenderId(senderId);
        if (existingProfile.isPresent()) {
            return existingProfile.get().getProspect();
        }

        String name = "Unknown";
        String profileLink = "https://www.facebook.com/" + senderId;

        try {
            String url = String.format("https://graph.facebook.com/%s/%s?fields=name&access_token=%s",
                    metaApiVersion, senderId, pageAccessToken);
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode node = objectMapper.readTree(responseBody);
                if (node.has("name")) {
                    name = node.get("name").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Facebook: Failed to fetch sender details: " + e.getMessage());
        }

        Prospect prospect = new Prospect();
        prospect.setName(name);
        prospect.setProspectStatus(ProspectStatus.NEW);
        prospect.setProspectionType(ProspectionType.FACEBOOK);
        prospect.setProspectDetails("Auto-created from Facebook message");
        prospect = prospectRepository.save(prospect);

        ProspectProfile profile = new ProspectProfile();
        profile.setPlatform(Platform.FACEBOOK);
        profile.setSenderId(senderId);
        profile.setProfileLink(profileLink);
        profile.setProspect(prospect);
        prospectProfileRepository.save(profile);

        System.out.println("Facebook: Created Prospect: " + name + ", Sender ID: " + senderId);
        return prospect;
    }

    @Override
    @Transactional
    public void sendMessage(String recipientId, String messageText, String quickReplies, String platform, String sessionId) {
        if (!Platform.FACEBOOK.name().equalsIgnoreCase(platform)) {
            return;
        }

        try {
            if (recipientId == null || recipientId.trim().isEmpty()) {
                System.err.println("Facebook: ❌ recipientId is null or empty, cannot send message");
                return;
            }

            String endpoint = String.format(
                    "https://graph.facebook.com/%s/me/messages?access_token=%s",
                    metaApiVersion,
                    pageAccessToken // must be the PAGE access token
            );

            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            // Build JSON safely with ObjectMapper
            ObjectNode body = objectMapper.createObjectNode();
            ObjectNode recipientNode = body.putObject("recipient");
            recipientNode.put("id", recipientId);

            ObjectNode messageNode = body.putObject("message");
            messageNode.put("text", messageText);

            body.put("messaging_type", "RESPONSE");

            String jsonBody = objectMapper.writeValueAsString(body);
            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            System.out.println("Facebook: Sending Meta API request to recipientId=" + recipientId + ": " + jsonBody);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Facebook: Meta API response (status " + statusCode + "): " + responseBody);

                if (statusCode == 200) {
                    Message message = new Message();
                    message.setPlatform(Platform.FACEBOOK);
                    message.setSenderId("chatbot");
                    message.setRecipientId(recipientId);
                    message.setSessionId(sessionId);
                    message.setMessageText(messageText);
                    message.setTimestamp(LocalDateTime.now());
                    messageRepository.save(message);
                    System.out.println("Facebook: ✅ Saved chatbot response for sessionId=" + sessionId);
                } else {
                    System.err.println("Facebook: ❌ Meta API error for recipientId=" + recipientId + ": " + responseBody);
                }
            }

        } catch (Exception e) {
            System.err.println("Facebook: Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .filter(m -> m.getPlatform() == Platform.FACEBOOK)
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
        dto.setSessionId(message.getSessionId());
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