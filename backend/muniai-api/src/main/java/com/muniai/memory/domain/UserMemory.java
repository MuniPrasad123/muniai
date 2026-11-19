package com.muniai.memory.domain;

import java.time.Instant;
import java.util.UUID;

public record UserMemory(UUID id, MemoryCategory category, String key, String value, String displayLabel,
                         MemoryStatus status, MemorySourceType sourceType, UUID sourceConversationId,
                         UUID sourceMessageId, Double confidence, Instant approvedAt, Instant disabledAt,
                         Instant rejectedAt, Instant deletedAt, Instant expiresAt, UUID supersededByMemoryId,
                         Instant lastUsedAt, long usageCount, Instant createdAt, Instant updatedAt) {}
