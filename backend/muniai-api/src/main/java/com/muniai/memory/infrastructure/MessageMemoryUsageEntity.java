package com.muniai.memory.infrastructure;

import com.muniai.conversation.infrastructure.MessageEntity;
import com.muniai.memory.domain.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="message_memory_usage")
public class MessageMemoryUsageEntity {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="message_id",nullable=false) private MessageEntity message;
    @Column(name="memory_id") private UUID memoryId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private MemoryCategory category;
    @Column(name="memory_key",nullable=false,length=100) private String memoryKey;
    @Column(name="display_label",nullable=false,length=150) private String displayLabel;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected MessageMemoryUsageEntity(){}
    public MessageMemoryUsageEntity(UUID id,MessageEntity message,UsedMemory used,Instant now){this.id=id;this.message=message;this.memoryId=used.memoryId();this.category=used.category();this.memoryKey=used.key();this.displayLabel=used.displayLabel();this.createdAt=now;}
    public UUID getMemoryId(){return memoryId;} public MemoryCategory getCategory(){return category;} public String getMemoryKey(){return memoryKey;} public String getDisplayLabel(){return displayLabel;}
}
