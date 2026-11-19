package com.muniai.memory.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="muniai.memory")
public record MemoryConfigurationProperties(boolean enabled, int maxActive, int maxProposed, int maxValueLength,
        int maxPromptItems, int maxPromptCharacters, boolean usageMetadataEnabled, boolean suggestionsEnabled,
        int suggestionCooldownMinutes) {
    public MemoryConfigurationProperties {
        if(maxActive<1||maxProposed<1||maxValueLength<1||maxPromptItems<1||maxPromptCharacters<1)
            throw new IllegalArgumentException("Memory limits must be positive.");
    }
}
