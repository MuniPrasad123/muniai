package com.muniai.memory.infrastructure;

import com.muniai.memory.domain.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="user_memory")
public class UserMemoryEntity {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private MemoryCategory category;
    @Column(name="memory_key",nullable=false,length=100) private String memoryKey;
    @Column(name="memory_value",nullable=false,length=1000) private String memoryValue;
    @Column(name="normalized_value",nullable=false,length=1000) private String normalizedValue;
    @Column(name="display_label",nullable=false,length=150) private String displayLabel;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private MemoryStatus status;
    @Enumerated(EnumType.STRING) @Column(name="source_type",nullable=false,length=40) private MemorySourceType sourceType;
    @Column(name="source_conversation_id") private UUID sourceConversationId;
    @Column(name="source_message_id") private UUID sourceMessageId;
    private Double confidence;
    @Column(name="approved_at") private Instant approvedAt;
    @Column(name="disabled_at") private Instant disabledAt;
    @Column(name="rejected_at") private Instant rejectedAt;
    @Column(name="deleted_at") private Instant deletedAt;
    @Column(name="expires_at") private Instant expiresAt;
    @Column(name="superseded_by_memory_id") private UUID supersededByMemoryId;
    @Column(name="last_used_at") private Instant lastUsedAt;
    @Column(name="usage_count",nullable=false) private long usageCount;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Version private long version;
    protected UserMemoryEntity(){}
    public UserMemoryEntity(UUID id,MemoryCategory category,String key,String value,String normalized,String label,
            MemoryStatus status,MemorySourceType source,UUID conversationId,UUID messageId,Double confidence,
            Instant expiresAt,Instant now){this.id=id;this.category=category;this.memoryKey=key;this.memoryValue=value;
        this.normalizedValue=normalized;this.displayLabel=label;this.status=status;this.sourceType=source;
        this.sourceConversationId=conversationId;this.sourceMessageId=messageId;this.confidence=confidence;
        this.expiresAt=expiresAt;this.createdAt=now;this.updatedAt=now;}
    public UUID getId(){return id;} public MemoryCategory getCategory(){return category;} public String getMemoryKey(){return memoryKey;}
    public String getMemoryValue(){return memoryValue;} public String getNormalizedValue(){return normalizedValue;}
    public String getDisplayLabel(){return displayLabel;} public MemoryStatus getStatus(){return status;}
    public MemorySourceType getSourceType(){return sourceType;} public UUID getSourceConversationId(){return sourceConversationId;}
    public UUID getSourceMessageId(){return sourceMessageId;} public Double getConfidence(){return confidence;}
    public Instant getApprovedAt(){return approvedAt;} public Instant getDisabledAt(){return disabledAt;}
    public Instant getRejectedAt(){return rejectedAt;} public Instant getDeletedAt(){return deletedAt;}
    public Instant getExpiresAt(){return expiresAt;} public UUID getSupersededByMemoryId(){return supersededByMemoryId;}
    public Instant getLastUsedAt(){return lastUsedAt;} public long getUsageCount(){return usageCount;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void edit(MemoryCategory category,String key,String value,String normalized,String label,Instant expiresAt,Instant now){
        this.category=category;this.memoryKey=key;this.memoryValue=value;this.normalizedValue=normalized;this.displayLabel=label;this.expiresAt=expiresAt;this.updatedAt=now;}
    public void activate(Instant now){status=MemoryStatus.ACTIVE;approvedAt=now;disabledAt=null;rejectedAt=null;updatedAt=now;}
    public void reject(Instant now){status=MemoryStatus.REJECTED;rejectedAt=now;updatedAt=now;}
    public void disable(Instant now){status=MemoryStatus.DISABLED;disabledAt=now;updatedAt=now;}
    public void supersede(UUID replacement,Instant now){status=MemoryStatus.SUPERSEDED;supersededByMemoryId=replacement;updatedAt=now;}
    public void delete(Instant now){status=MemoryStatus.DELETED;deletedAt=now;updatedAt=now;}
    public void used(Instant now){lastUsedAt=now;usageCount++;updatedAt=now;}
}
