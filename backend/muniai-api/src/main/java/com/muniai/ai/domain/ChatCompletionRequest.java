package com.muniai.ai.domain;

import java.time.Duration;

public record ChatCompletionRequest(String message, String model, Double temperature, Duration timeout) {
    public ChatCompletionRequest(String message) { this(message, null, null, null); }
}
