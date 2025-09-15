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
import com.example.crm.repository.ProspectProfileRepository;
import com.example.crm.repository.ProspectRepository;
import com.example.crm.service.interfaces.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("whatsappMessageService")
public class WhatsAppMessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ProspectRepository prospectRepository;
    private final ProspectProfileRepository prospectProfileRepository;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${meta.whatsapp.access.token}")
    private String whatsappAccessToken;

    @Value("${meta.api.version}")
    private String metaApiVersion;

    @Value("${meta.whatsapp.phone_number_id}")
    private String phoneNumberId;

    public WhatsAppMessageServiceImpl(MessageRepository messageRepository, CustomerRepository customerRepository,
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
        if (!"whatsapp".equalsIgnoreCase(platform)) {
            System.out.println("WhatsApp service ignoring payload for platform: " + platform);
            return;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            JsonNode entries = rootNode.path("entry");
            if (entries.isEmpty()) {
                System.out.println("WhatsApp: No entries found in payload");
                return;
            }

            for (JsonNode entry : entries) {
                JsonNode changes = entry.path("changes");
                if (changes.isEmpty()) continue;

                for (JsonNode change : changes) {
                    JsonNode valueNode = change.path("value");
                    if (!valueNode.has("messages") || !valueNode.has("metadata")) continue;

                    JsonNode messages = valueNode.path("messages");
                    if (messages.isEmpty()) continue;

                    // Bot identifier (your phone number id)
                    String businessNumberId = valueNode.path("metadata").path("phone_number_id").asText("");
                    if (businessNumberId.isEmpty()) {
                        System.err.println("WhatsApp: missing phone_number_id in metadata");
                        continue;
                    }

                    for (JsonNode messageNode : messages) {
                        String userPhone = messageNode.path("from").asText("");
                        if (userPhone.isEmpty()) continue;

                        boolean isEcho = messageNode.path("context").has("from");
                        if (isEcho) {
                            System.out.println("WhatsApp: Skipping echo message from " + userPhone);
                            continue;
                        }

                        String sessionId = generateSessionId(userPhone, businessNumberId);

                        Prospect prospect = createOrUpdateProspect(userPhone, valueNode);
                        Message message = new Message();
                        message.setPlatform(Platform.WHATSAPP);
                        message.setSenderId(userPhone);           // user sending
                        message.setRecipientId(businessNumberId); // our bot phone id
                        message.setSessionId(sessionId);
                        message.setTimestamp(LocalDateTime.now());

                        String text = messageNode.path("text").path("body").asText(null);
                        if (text != null && !text.isEmpty()) {
                            message.setMessageText(text);
                        } else {
                            message.setMessageText("No text content");
                        }

                        Optional<Customer> customer = findCustomerBySenderId(userPhone);
                        customer.ifPresent(message::setCustomer);

                        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                                RabbitMQConfig.WHATSAPP_ROUTING_KEY,
                                objectMapper.writeValueAsString(message));
                        System.out.println("WhatsApp: Published message from user=" + userPhone + " to RabbitMQ");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("WhatsApp: Failed to process payload: " + e.getMessage());
            throw new IllegalArgumentException("WhatsApp: Failed to process payload", e);
        }
    }

    private String generateSessionId(String senderId, String recipientId) {
        String id1 = senderId.compareTo(recipientId) < 0 ? senderId : recipientId;
        String id2 = senderId.compareTo(recipientId) < 0 ? recipientId : senderId;
        return Platform.WHATSAPP + ":" + id1 + ":" + id2;
    }

    private Prospect createOrUpdateProspect(String senderId, JsonNode valueNode) {
        Optional<ProspectProfile> existingProfile = prospectProfileRepository.findBySenderId(senderId);
        if (existingProfile.isPresent()) {
            return existingProfile.get().getProspect();
        }

        String name = "Unknown";
        String profileLink = "https://wa.me/" + senderId;

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

        return prospect;
    }

    // --- FIXED sendMessage: build proper JSON, use Authorization header, use ContentType.APPLICATION_JSON
    @Override
    @Transactional
    public void sendMessage(String recipientId, String messageText, String quickReplies, String platform, String sessionId) {
        if (!Platform.WHATSAPP.name().equalsIgnoreCase(platform)) return;

        try {
            // Normalize recipient phone (WhatsApp expects international digits only)
            String to = normalizePhone(recipientId);
            if (to.isEmpty()) {
                System.err.println("WhatsApp: invalid recipient phone: " + recipientId);
                return;
            }

            // Endpoint: https://graph.facebook.com/{apiVersion}/{phoneNumberId}/messages
            String endpoint = String.format("https://graph.facebook.com/%s/%s/messages",
                    metaApiVersion, phoneNumberId);

            // Build JSON using ObjectMapper (avoid escaping issues)
            JsonNode bodyNode = objectMapper.createObjectNode()
                    .put("messaging_product", "whatsapp")
                    .put("to", to)
                    .put("recipient_type", "individual")
                    .put("type", "text")
                    .set("text", objectMapper.createObjectNode().put("body", messageText));

            String jsonBody = objectMapper.writeValueAsString(bodyNode);

            HttpPost request = new HttpPost(endpoint);
            // Use Authorization header instead of access_token in URL
            request.setHeader("Authorization", "Bearer " + whatsappAccessToken);

            // Ensure entity has application/json content type
            StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            System.out.println("WhatsApp: Sending API request to " + endpoint + " payload: " + jsonBody.replaceAll("\\n",""));
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println("WhatsApp API response (" + statusCode + "): " + responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    Message message = new Message();
                    message.setPlatform(Platform.WHATSAPP);
                    message.setSenderId("chatbot");
                    message.setRecipientId(to); // user number (normalized)
                    message.setSessionId(sessionId);
                    message.setMessageText(messageText);
                    message.setTimestamp(LocalDateTime.now());
                    messageRepository.save(message);
                } else {
                    System.err.println("WhatsApp: Error sending to " + to + ": " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("WhatsApp: Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        // Remove any non-digit characters (keeps country code)
        return phone.replaceAll("\\D", "");
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
