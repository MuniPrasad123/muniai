package com.muniai.chat.api;
public record ChatResponseDto(String answer, String model, String provider, String correlationId) {}
