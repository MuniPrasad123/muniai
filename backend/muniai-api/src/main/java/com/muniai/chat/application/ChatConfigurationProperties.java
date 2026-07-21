package com.muniai.chat.application;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("muniai.chat")
public record ChatConfigurationProperties(int maxMessageLength) {
    public ChatConfigurationProperties { if (maxMessageLength <= 0) maxMessageLength = 10_000; }
}
