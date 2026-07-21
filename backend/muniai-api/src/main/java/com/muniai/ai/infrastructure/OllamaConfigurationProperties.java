package com.muniai.ai.infrastructure;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("muniai.ollama")
public record OllamaConfigurationProperties(URI baseUrl, String model, Duration timeout) {
    public OllamaConfigurationProperties {
        timeout = timeout == null ? Duration.ofSeconds(120) : timeout;
    }
}
