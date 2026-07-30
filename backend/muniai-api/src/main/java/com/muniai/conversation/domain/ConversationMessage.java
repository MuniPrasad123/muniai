package com.muniai.conversation.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationMessage(UUID id, UUID conversationId, MessageRole role, String content, String model,
                                  ChatMode mode, List<MessageCitation> citations, Instant createdAt) {}
