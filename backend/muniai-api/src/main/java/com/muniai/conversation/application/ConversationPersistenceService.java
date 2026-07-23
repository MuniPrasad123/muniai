package com.muniai.conversation.application;

import com.muniai.conversation.domain.*;
import com.muniai.conversation.infrastructure.*;
import com.muniai.shared.exception.ConversationNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationPersistenceService {
    static final String DEFAULT_TITLE = "New conversation";
    private final ConversationRepository conversations;
    private final MessageRepository messages;

    public ConversationPersistenceService(ConversationRepository conversations, MessageRepository messages) {
        this.conversations = conversations;
        this.messages = messages;
    }

    @Transactional
    public Conversation create() {
        Instant now = Instant.now();
        return map(conversations.save(new ConversationEntity(UUID.randomUUID(), DEFAULT_TITLE, now, now)), List.of());
    }

    @Transactional(readOnly = true)
    public List<Conversation> list() {
        return conversations.findAllByOrderByUpdatedAtDesc().stream().map(entity -> map(entity, List.of())).toList();
    }

    @Transactional(readOnly = true)
    public Conversation get(UUID id) {
        ConversationEntity entity = required(id);
        return map(entity, messages.findByConversation_IdOrderByCreatedAtAscIdAsc(id).stream().map(this::map).toList());
    }

    @Transactional
    public Conversation rename(UUID id, String title) {
        ConversationEntity entity = required(id);
        entity.rename(title.trim(), Instant.now());
        return map(entity, List.of());
    }

    @Transactional
    public void delete(UUID id) {
        ConversationEntity entity = required(id);
        conversations.delete(entity);
    }

    @Transactional
    public void deleteAll() {
        conversations.deleteAllInBatch();
    }

    @Transactional
    public ConversationMessage saveUserMessage(UUID conversationId, String content) {
        ConversationEntity conversation = required(conversationId);
        Instant now = Instant.now();
        if (DEFAULT_TITLE.equals(conversation.getTitle()) && messages.countByConversation_Id(conversationId) == 0) {
            conversation.rename(titleFrom(content), now);
        } else {
            conversation.touch(now);
        }
        return map(messages.save(new MessageEntity(UUID.randomUUID(), conversation, MessageRole.USER, content, null, now)));
    }

    @Transactional
    public ConversationMessage saveAssistantMessage(UUID conversationId, String content, String model) {
        ConversationEntity conversation = required(conversationId);
        Instant now = Instant.now();
        conversation.touch(now);
        return map(messages.save(new MessageEntity(UUID.randomUUID(), conversation, MessageRole.ASSISTANT, content, model, now)));
    }

    private ConversationEntity required(UUID id) {
        return conversations.findById(id).orElseThrow(() -> new ConversationNotFoundException(id));
    }

    private String titleFrom(String content) {
        String normalized = content.trim().replaceAll("\\s+", " ");
        return normalized.length() <= 60 ? normalized : normalized.substring(0, 57) + "...";
    }

    private Conversation map(ConversationEntity entity, List<ConversationMessage> messageList) {
        return new Conversation(entity.getId(), entity.getTitle(), entity.getCreatedAt(), entity.getUpdatedAt(), messageList);
    }

    private ConversationMessage map(MessageEntity entity) {
        return new ConversationMessage(entity.getId(), entity.getConversationId(), entity.getRole(), entity.getContent(), entity.getModel(), entity.getCreatedAt());
    }
}
