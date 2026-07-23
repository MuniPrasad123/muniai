package com.muniai.conversation.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Conversation(UUID id, String title, Instant createdAt, Instant updatedAt, List<ConversationMessage> messages) {
}
