package com.muniai.conversation.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties("muniai.rag")
public record RagConfigurationProperties(int topK, int maximumTopK, double similarityThreshold,
        int maxContextChunks, int maxContextCharacters, int maxHistoryMessages,
        String chatModel, double temperature, Duration requestTimeout) {
    public RagConfigurationProperties {
        if (topK <= 0) topK = 5;
        if (maximumTopK <= 0 || maximumTopK > 20) maximumTopK = 10;
        if (topK > maximumTopK) topK = maximumTopK;
        if (similarityThreshold < 0 || similarityThreshold > 1) similarityThreshold = 0.45;
        if (maxContextChunks <= 0) maxContextChunks = 5;
        if (maxContextCharacters <= 0) maxContextCharacters = 6000;
        if (maxHistoryMessages < 0) maxHistoryMessages = 8;
        if (temperature < 0 || temperature > 1) temperature = 0.1;
        if (requestTimeout == null || requestTimeout.isNegative() || requestTimeout.isZero())
            requestTimeout = Duration.ofSeconds(300);
    }
}
