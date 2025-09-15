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

@Service("instagramMessageService")
public class InstagramMessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ProspectRepository prospectRepository;
    private final ProspectProfileRepository prospectProfileRepository;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${instagram.access.token}")
    private String instagramAccessToken;

    @Value("${meta.page.access.token}")
    private String pageAccessToken;

    @Value("${meta.api.version}")
    private String metaApiVersion;

    public InstagramMessageServiceImpl(MessageRepository messageRepository, CustomerRepository customerRepository,
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
        if (!"instagram".equalsIgnoreCase(platform)) {
            System.out.println("Instagram service ignoring payload for platform: " + platform);
            return;
        }
        try {
            System.out.println("Instagram: Processing payload: " + payload);
            JsonNode rootNode = objectMapper.readTree(payload);
            System.out.println("Instagram: Parsed JSON structure: " + rootNode.toPrettyString());

            JsonNode entries = rootNode.path("entry");
            if (entries.isEmpty()) {
                System.out.println("Instagram: No entries found in payload: " + payload);
                return;
            }

            for (JsonNode entry : entries) {
                System.out.println("Instagram: Processing entry: " + entry.toPrettyString());
                JsonNode messaging = entry.path("messaging");
                if (messaging.isEmpty()) {
                    System.out.println("Instagram: No messaging field found in entry: " + entry.toPrettyString());
                    continue;
                }
                for (JsonNode event : messaging) {
                    System.out.println("Instagram: Processing event: " + event.toPrettyString());
                    String senderId = event.path("sender").path("id").asText("");
                    String recipientId = event.path("recipient").path("id").asText("");
                    JsonNode messageNode = event.path("message");
                    if (senderId.isEmpty() || recipientId.isEmpty()) {
                        System.out.println("Instagram: Missing senderId or recipientId: " + event.toPrettyString());
                        continue;
                    }

                    boolean isEcho = messageNode.path("is_echo").asBoolean(false);
                    if (isEcho) {
                        System.out.println("Instagram: Skipping echo message from senderId=" + senderId);
                        continue;
                    }

                    System.out.println("Instagram: Processing message: senderId=" + senderId + ", recipientId=" + recipientId + ", messageNode=" + messageNode.toPrettyString());

                    String sessionId = generateSessionId(senderId, recipientId);

                    Prospect prospect = createOrUpdateProspect(senderId);
                    Message message = new Message();
                    message.setPlatform(Platform.INSTAGRAM);
                    message.setSenderId(senderId);
                    message.setRecipientId(recipientId);
                    message.setSessionId(sessionId);
                    message.setTimestamp(LocalDateTime.now());

                    String text = null;
                    if (messageNode.has("text")) {
                        text = messageNode.path("text").asText(null);
                    }

                    if (text != null && !text.isEmpty()) {
                        message.setMessageText(text);
                        System.out.println("Instagram: Text message received: " + text);
                    } else {
                        message.setMessageText("No text content");
                        System.out.println("Instagram: No text content: " + messageNode.toPrettyString());
                    }

                    Optional<Customer> customer = findCustomerBySenderId(senderId);
                    customer.ifPresent(message::setCustomer);

                    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.INSTAGRAM_ROUTING_KEY, objectMapper.writeValueAsString(message));
                    System.out.println("Instagram: Published message to RabbitMQ: " + message.getMessageText());
                }
            }
        } catch (Exception e) {
            System.err.println("Instagram: Failed to process payload: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Instagram: Failed to process payload: " + e.getMessage(), e);
        }
    }

    private String generateSessionId(String senderId, String recipientId) {
        String id1 = senderId.compareTo(recipientId) < 0 ? senderId : recipientId;
        String id2 = senderId.compareTo(recipientId) < 0 ? recipientId : senderId;
        return Platform.INSTAGRAM + ":" + id1 + ":" + id2;
    }

    private Prospect createOrUpdateProspect(String senderId) {
        Optional<ProspectProfile> existingProfile = prospectProfileRepository.findBySenderId(senderId);
        if (existingProfile.isPresent()) {
            System.out.println("Instagram: Found existing ProspectProfile for senderId=" + senderId);
            return existingProfile.get().getProspect();
        }

        String name = "Unknown";
        String profileLink = "https://www.instagram.com/user/" + senderId;

        try {
            String url = String.format("https://graph.facebook.com/%s/%s?fields=username&access_token=%s",
                    metaApiVersion, senderId, pageAccessToken);
            HttpGet request = new HttpGet(url);
            System.out.println("Instagram: Fetching sender details for senderId=" + senderId + " with token (redacted): " + pageAccessToken.substring(0, 10) + "...");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Instagram: Sender details response (status " + statusCode + "): " + responseBody);
                if (statusCode == 200) {
                    JsonNode node = objectMapper.readTree(responseBody);
                    if (node.has("username")) {
                        name = node.get("username").asText();
                        profileLink = "https://www.instagram.com/" + name + "/";
                        System.out.println("Instagram: Fetched username: " + name);
                    } else {
                        System.err.println("Instagram: No username in response for senderId=" + senderId + ": " + responseBody);
                    }
                } else if (statusCode == 429) {
                    System.err.println("Instagram: Rate limit exceeded for senderId=" + senderId + ". Retrying after delay...");
                    Thread.sleep(1000);
                    try (CloseableHttpResponse retryResponse = httpClient.execute(new HttpGet(url))) {
                        int retryStatusCode = retryResponse.getStatusLine().getStatusCode();
                        String retryResponseBody = EntityUtils.toString(retryResponse.getEntity());
                        System.out.println("Instagram: Retry response (status " + retryStatusCode + "): " + retryResponseBody);
                        if (retryStatusCode == 200) {
                            JsonNode node = objectMapper.readTree(retryResponseBody);
                            if (node.has("username")) {
                                name = node.get("username").asText();
                                profileLink = "https://www.instagram.com/" + name + "/";
                                System.out.println("Instagram: Fetched username on retry: " + name);
                            }
                        } else {
                            System.err.println("Instagram: Retry failed for senderId=" + senderId + ", status: " + retryStatusCode + ", response: " + retryResponseBody);
                        }
                    }
                } else {
                    System.err.println("Instagram: Failed to fetch sender details for senderId=" + senderId + ", status: " + statusCode + ", response: " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Instagram: Error fetching sender details for senderId=" + senderId + ": " + e.getMessage());
            e.printStackTrace();
        }

        Prospect prospect = new Prospect();
        prospect.setName(name);
        prospect.setProspectStatus(ProspectStatus.NEW);
        prospect.setProspectionType(ProspectionType.INSTAGRAM);
        prospect.setProspectDetails("Auto-created from Instagram message");
        prospect = prospectRepository.save(prospect);

        ProspectProfile profile = new ProspectProfile();
        profile.setPlatform(Platform.INSTAGRAM);
        profile.setSenderId(senderId);
        profile.setProfileLink(profileLink);
        profile.setProspect(prospect);
        prospectProfileRepository.save(profile);

        System.out.println("Instagram: Created Prospect: " + name + ", Sender ID: " + senderId + ", Profile Link: " + profileLink);
        return prospect;
    }

    @Override
    @Transactional
    public void sendMessage(String recipientId, String messageText, String quickReplies, String platform, String sessionId) {
        if (!Platform.INSTAGRAM.name().equalsIgnoreCase(platform)) {
            return;
        }

        try {
            if (recipientId == null || recipientId.trim().isEmpty()) {
                System.err.println("Instagram: ❌ recipientId is null or empty, cannot send message");
                return;
            }

            String endpoint = String.format(
                    "https://graph.facebook.com/%s/me/messages?access_token=%s",
                    metaApiVersion,
                    pageAccessToken // IMPORTANT: must use page token
            );

            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            // Build JSON safely
            ObjectNode body = new ObjectMapper().createObjectNode();
            ObjectNode recipientNode = body.putObject("recipient");
            recipientNode.put("id", recipientId);

            ObjectNode messageNode = body.putObject("message");
            messageNode.put("text", messageText);

            body.put("messaging_type", "RESPONSE");

            String jsonBody = new ObjectMapper().writeValueAsString(body);
            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            System.out.println("Instagram: Sending message to recipientId=" + recipientId + ": " + jsonBody);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Instagram: Meta API response (status " + statusCode + "): " + responseBody);

                if (statusCode == 200) {
                    Message botMessage = new Message();
                    botMessage.setPlatform(Platform.INSTAGRAM);
                    botMessage.setSenderId("chatbot");
                    botMessage.setRecipientId(recipientId);
                    botMessage.setSessionId(sessionId);
                    botMessage.setMessageText(messageText);
                    botMessage.setTimestamp(LocalDateTime.now());
                    messageRepository.save(botMessage);
                    System.out.println("Instagram: ✅ Saved chatbot response for sessionId=" + sessionId);
                } else {
                    System.err.println("Instagram: ❌ Meta API error for recipientId=" + recipientId + ": " + responseBody);
                }
            }

        } catch (Exception e) {
            System.err.println("Instagram: Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }




    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .filter(m -> m.getPlatform() == Platform.INSTAGRAM)
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