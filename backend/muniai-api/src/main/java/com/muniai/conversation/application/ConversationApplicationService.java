package com.muniai.conversation.application;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.ChatCompletion;
import com.muniai.ai.domain.ChatCompletionRequest;
import com.muniai.chat.application.ChatConfigurationProperties;
import com.muniai.conversation.domain.*;
import com.muniai.shared.exception.MessageTooLongException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConversationApplicationService {
    private final ConversationPersistenceService persistence;
    private final LanguageModelProvider provider;
    private final ChatConfigurationProperties chatProperties;

    public ConversationApplicationService(ConversationPersistenceService persistence, LanguageModelProvider provider,
                                          ChatConfigurationProperties chatProperties) {
        this.persistence = persistence;
        this.provider = provider;
        this.chatProperties = chatProperties;
    }

    public Conversation create() { return persistence.create(); }
    public List<Conversation> list() { return persistence.list(); }
    public Conversation get(UUID id) { return persistence.get(id); }
    public Conversation rename(UUID id, String title) { return persistence.rename(id, title); }
    public void delete(UUID id) { persistence.delete(id); }
    public void deleteAll() { persistence.deleteAll(); }

    public SendMessageResult send(UUID conversationId, String content) {
        if (content.length() > chatProperties.maxMessageLength()) {
            throw new MessageTooLongException(chatProperties.maxMessageLength());
        }
        ConversationMessage userMessage = persistence.saveUserMessage(conversationId, content);
        ChatCompletion completion = provider.complete(new ChatCompletionRequest(content));
        ConversationMessage assistantMessage = persistence.saveAssistantMessage(conversationId, completion.answer(), completion.model());
        return new SendMessageResult(conversationId, userMessage, assistantMessage, completion.provider());
    }

    public record SendMessageResult(UUID conversationId, ConversationMessage userMessage,
                                    ConversationMessage assistantMessage, String provider) {}
}
