package com.muniai.chat.api;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record ChatRequestDto(@NotNull @NotBlank String message) {}
