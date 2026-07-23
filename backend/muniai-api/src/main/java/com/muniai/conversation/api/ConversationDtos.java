package com.muniai.conversation.api;

import com.muniai.conversation.domain.MessageRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ConversationDtos {
    private ConversationDtos() {}

    public record RenameRequest(@NotBlank @Size(max = 200) String title) {}
    public record SendMessageRequest(@NotBlank String message) {}
    public record MessageResponse(UUID id, UUID conversationId, MessageRole role, String content,
                                  String model, Instant createdAt) {}
    public record ConversationResponse(UUID id, String title, Instant createdAt, Instant updatedAt,
                                       List<MessageResponse> messages) {}
    public record SendMessageResponse(UUID conversationId, MessageResponse userMessage,
                                      MessageResponse assistantMessage, String provider, String correlationId) {}
}
