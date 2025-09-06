package com.example.crm.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "message-exchange";
    public static final String FACEBOOK_QUEUE = "facebook-message-queue";
    public static final String INSTAGRAM_QUEUE = "instagram-message-queue";
    public static final String WHATSAPP_QUEUE = "whatsapp-message-queue";
    public static final String FACEBOOK_ROUTING_KEY = "facebook";
    public static final String INSTAGRAM_ROUTING_KEY = "instagram";
    public static final String WHATSAPP_ROUTING_KEY = "whatsapp";

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue facebookQueue() {
        return new Queue(FACEBOOK_QUEUE, true);
    }

    @Bean
    public Queue instagramQueue() {
        return new Queue(INSTAGRAM_QUEUE, true);
    }

    @Bean
    public Queue whatsappQueue() {
        return new Queue(WHATSAPP_QUEUE, true);
    }

    @Bean
    public Binding facebookBinding(Queue facebookQueue, DirectExchange messageExchange) {
        return BindingBuilder.bind(facebookQueue).to(messageExchange).with(FACEBOOK_ROUTING_KEY);
    }

    @Bean
    public Binding instagramBinding(Queue instagramQueue, DirectExchange messageExchange) {
        return BindingBuilder.bind(instagramQueue).to(messageExchange).with(INSTAGRAM_ROUTING_KEY);
    }

    @Bean
    public Binding whatsappBinding(Queue whatsappQueue, DirectExchange messageExchange) {
        return BindingBuilder.bind(whatsappQueue).to(messageExchange).with(WHATSAPP_ROUTING_KEY);
    }
}