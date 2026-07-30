package com.muniai.conversation.application;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.ChatCompletion;
import com.muniai.ai.domain.ChatCompletionRequest;
import com.muniai.chat.application.ChatConfigurationProperties;
import com.muniai.conversation.domain.*;
import com.muniai.shared.exception.MessageTooLongException;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConversationApplicationService {
    private final ConversationPersistenceService persistence;
    private final LanguageModelProvider provider;
    private final ChatConfigurationProperties chatProperties;
    private final RagService rag;
    private final RagConfigurationProperties ragProperties;

    public ConversationApplicationService(ConversationPersistenceService persistence, LanguageModelProvider provider,
                                          ChatConfigurationProperties chatProperties, RagService rag,
                                          RagConfigurationProperties ragProperties) {
        this.persistence = persistence;
        this.provider = provider;
        this.chatProperties = chatProperties;
        this.rag = rag;
        this.ragProperties = ragProperties;
    }

    public Conversation create() { return persistence.create(); }
    public List<Conversation> list() { return persistence.list(); }
    public Conversation get(UUID id) { return persistence.get(id); }
    public Conversation rename(UUID id, String title) { return persistence.rename(id, title); }
    public void delete(UUID id) { persistence.delete(id); }
    public void deleteAll() { persistence.deleteAll(); }

    public SendMessageResult send(UUID conversationId, String content, ChatMode mode, Set<UUID> documentIds,
                                  Integer topK, Double similarityThreshold) {
        if (content.length() > chatProperties.maxMessageLength()) {
            throw new MessageTooLongException(chatProperties.maxMessageLength());
        }
        ConversationMessage userMessage = persistence.saveUserMessage(conversationId, content, mode);
        if(mode==ChatMode.NORMAL){
            ChatCompletion completion = provider.complete(new ChatCompletionRequest(content));
            ConversationMessage assistantMessage = persistence.saveAssistantMessage(
                    conversationId, completion.answer(), completion.model(), mode, List.of());
            return new SendMessageResult(conversationId,userMessage,assistantMessage,completion.provider(),false);
        }
        List<ConversationMessage> history=persistence.recentMessages(conversationId,ragProperties.maxHistoryMessages()+1)
                .stream().filter(message->!message.id().equals(userMessage.id())).toList();
        RagService.RagResult result=rag.answer(content,documentIds,topK,similarityThreshold,history);
        String model=result.noRelevantContext()?"retrieval":result.completion().model();
        String providerName=result.noRelevantContext()?"local-retrieval":result.completion().provider();
        ConversationMessage assistantMessage=persistence.saveAssistantMessage(
                conversationId,result.answer(),model,mode,result.citations());
        return new SendMessageResult(conversationId,userMessage,assistantMessage,providerName,result.noRelevantContext());
    }

    public record SendMessageResult(UUID conversationId, ConversationMessage userMessage,
                                    ConversationMessage assistantMessage, String provider, boolean noRelevantContext) {}
}
