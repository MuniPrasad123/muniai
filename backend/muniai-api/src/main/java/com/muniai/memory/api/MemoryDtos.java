package com.muniai.memory.api;

import com.muniai.memory.domain.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class MemoryDtos {
    private MemoryDtos(){}
    public record ProposalRequest(@NotNull MemoryCategory category,@NotBlank @Size(max=100) String key,
            @NotBlank @Size(max=1000) String value,@NotBlank @Size(max=150) String displayLabel,
            MemorySourceType sourceType,UUID sourceConversationId,UUID sourceMessageId,Double confidence,Instant expiresAt){}
    public record UpdateRequest(@NotNull MemoryCategory category,@NotBlank @Size(max=100) String key,
            @NotBlank @Size(max=1000) String value,@NotBlank @Size(max=150) String displayLabel,Instant expiresAt){}
    public record SupersedeRequest(@NotNull UUID replacementMemoryId){}
    public record MemoryResponse(UUID id,MemoryCategory category,String key,String value,String displayLabel,
            MemoryStatus status,MemorySourceType sourceType,UUID sourceConversationId,UUID sourceMessageId,
            Instant approvedAt,Instant expiresAt,UUID supersededByMemoryId,Instant lastUsedAt,long usageCount,
            Instant createdAt,Instant updatedAt){}
}
