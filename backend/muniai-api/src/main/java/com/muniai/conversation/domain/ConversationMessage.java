package com.muniai.conversation.domain;

import java.time.Instant;
import java.util.UUID;

public record ConversationMessage(UUID id, UUID conversationId, MessageRole role, String content, String model, Instant createdAt) {
}
