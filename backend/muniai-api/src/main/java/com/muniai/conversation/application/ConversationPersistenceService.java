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
    private final MessageCitationRepository citations;

    public ConversationPersistenceService(ConversationRepository conversations, MessageRepository messages,
                                          MessageCitationRepository citations) {
        this.conversations = conversations;
        this.messages = messages;
        this.citations = citations;
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
    public ConversationMessage saveUserMessage(UUID conversationId, String content, ChatMode mode) {
        ConversationEntity conversation = required(conversationId);
        Instant now = Instant.now();
        if (DEFAULT_TITLE.equals(conversation.getTitle()) && messages.countByConversation_Id(conversationId) == 0) {
            conversation.rename(titleFrom(content), now);
        } else {
            conversation.touch(now);
        }
        return map(messages.save(new MessageEntity(
                UUID.randomUUID(), conversation, MessageRole.USER, content, null, mode, now)));
    }

    @Transactional
    public ConversationMessage saveAssistantMessage(UUID conversationId, String content, String model, ChatMode mode,
                                                     List<MessageCitation> citationValues) {
        ConversationEntity conversation = required(conversationId);
        Instant now = Instant.now();
        conversation.touch(now);
        MessageEntity saved = messages.save(new MessageEntity(
                UUID.randomUUID(), conversation, MessageRole.ASSISTANT, content, model, mode, now));
        List<MessageCitationEntity> savedCitations = citationValues.stream().map(value -> new MessageCitationEntity(
                value.id(), saved, value.citationIndex(), value.documentId(), value.chunkId(), value.chunkIndex(),
                value.originalFileName(), value.pageNumber(), value.similarityScore(), value.contentPreview(), now)).toList();
        citations.saveAll(savedCitations);
        return map(saved, savedCitations.stream().map(this::map).toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationMessage> recentMessages(UUID conversationId, int maximum) {
        List<MessageEntity> values = messages.findByConversation_IdOrderByCreatedAtAscIdAsc(conversationId);
        int start = Math.max(0, values.size() - maximum);
        return values.subList(start, values.size()).stream().map(this::map).toList();
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
        return map(entity, citations.findByMessage_IdOrderByCitationIndex(entity.getId()).stream().map(this::map).toList());
    }

    private ConversationMessage map(MessageEntity entity, List<MessageCitation> citationValues) {
        return new ConversationMessage(entity.getId(), entity.getConversationId(), entity.getRole(), entity.getContent(),
                entity.getModel(), entity.getChatMode(), citationValues, entity.getCreatedAt());
    }

    private MessageCitation map(MessageCitationEntity value) {
        return new MessageCitation(value.getId(), value.getCitationIndex(), value.getDocumentId(),
                value.getOriginalFileName(), value.getChunkId(), value.getChunkIndex(), value.getPageNumber(),
                value.getSimilarityScore(), value.getContentPreview(), value.getCreatedAt());
    }
}
