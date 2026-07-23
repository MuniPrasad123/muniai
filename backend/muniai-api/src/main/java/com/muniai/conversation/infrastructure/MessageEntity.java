package com.muniai.conversation.infrastructure;

import com.muniai.conversation.domain.MessageRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class MessageEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(length = 255)
    private String model;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MessageEntity() {}

    public MessageEntity(UUID id, ConversationEntity conversation, MessageRole role, String content, String model, Instant createdAt) {
        this.id = id;
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.model = model;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getConversationId() { return conversation.getId(); }
    public MessageRole getRole() { return role; }
    public String getContent() { return content; }
    public String getModel() { return model; }
    public Instant getCreatedAt() { return createdAt; }
}
