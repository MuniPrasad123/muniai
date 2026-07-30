package com.muniai.conversation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.*;
import com.muniai.chat.application.ChatConfigurationProperties;
import com.muniai.conversation.domain.*;
import com.muniai.shared.exception.AiProviderUnavailableException;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConversationApplicationServiceTest {
    private final ConversationPersistenceService persistence = mock(ConversationPersistenceService.class);
    private final LanguageModelProvider provider = mock(LanguageModelProvider.class);
    private final RagService rag = mock(RagService.class);
    private final RagConfigurationProperties ragProperties = new RagConfigurationProperties(
            5,10,0.45,5,6000,8,"",0.1,Duration.ofSeconds(10));
    private final ConversationApplicationService service = new ConversationApplicationService(
            persistence, provider, new ChatConfigurationProperties(100),rag,ragProperties);

    @Test
    void savesUserThenAssistantAroundProviderCall() {
        UUID conversationId = UUID.randomUUID();
        ConversationMessage user = message(conversationId, MessageRole.USER, "hello", null);
        ConversationMessage assistant = message(conversationId, MessageRole.ASSISTANT, "answer", "model");
        when(persistence.saveUserMessage(conversationId, "hello",ChatMode.NORMAL)).thenReturn(user);
        when(provider.complete(new ChatCompletionRequest("hello"))).thenReturn(new ChatCompletion("answer", "model", "ollama"));
        when(persistence.saveAssistantMessage(conversationId,"answer","model",ChatMode.NORMAL,List.of())).thenReturn(assistant);

        ConversationApplicationService.SendMessageResult result = service.send(
                conversationId,"hello",ChatMode.NORMAL,Set.of(),null,null);

        assertEquals(assistant, result.assistantMessage());
        var ordered = inOrder(persistence, provider);
        ordered.verify(persistence).saveUserMessage(conversationId,"hello",ChatMode.NORMAL);
        ordered.verify(provider).complete(new ChatCompletionRequest("hello"));
        ordered.verify(persistence).saveAssistantMessage(conversationId,"answer","model",ChatMode.NORMAL,List.of());
        verifyNoInteractions(rag);
    }

    @Test
    void preservesUserAndDoesNotCreateAssistantWhenProviderFails() {
        UUID conversationId = UUID.randomUUID();
        when(persistence.saveUserMessage(conversationId,"hello",ChatMode.NORMAL))
                .thenReturn(message(conversationId,MessageRole.USER,"hello",null));
        when(provider.complete(any())).thenThrow(new AiProviderUnavailableException(new RuntimeException()));

        assertThrows(AiProviderUnavailableException.class,()->service.send(
                conversationId,"hello",ChatMode.NORMAL,Set.of(),null,null));
        verify(persistence).saveUserMessage(conversationId,"hello",ChatMode.NORMAL);
        verify(persistence,never()).saveAssistantMessage(any(),any(),any(),any(),any());
    }

    @Test
    void documentModeInvokesRagAndPersistsCitations() {
        UUID conversationId=UUID.randomUUID(),documentId=UUID.randomUUID();
        ConversationMessage user=message(conversationId,MessageRole.USER,"question",null);
        ConversationMessage assistant=new ConversationMessage(UUID.randomUUID(),conversationId,MessageRole.ASSISTANT,
                "grounded","model",ChatMode.DOCUMENT_RAG,List.of(),Instant.now());
        when(persistence.saveUserMessage(conversationId,"question",ChatMode.DOCUMENT_RAG)).thenReturn(user);
        when(persistence.recentMessages(conversationId,9)).thenReturn(List.of(user));
        when(rag.answer("question",Set.of(documentId),5,null,List.of())).thenReturn(
                new RagService.RagResult("grounded",List.of(),new ChatCompletion("grounded","model","ollama")));
        when(persistence.saveAssistantMessage(conversationId,"grounded","model",ChatMode.DOCUMENT_RAG,List.of()))
                .thenReturn(assistant);

        var result=service.send(conversationId,"question",ChatMode.DOCUMENT_RAG,Set.of(documentId),5,null);

        assertEquals(assistant,result.assistantMessage());
        verify(rag).answer("question",Set.of(documentId),5,null,List.of());
        verifyNoInteractions(provider);
    }

    private ConversationMessage message(UUID conversationId, MessageRole role, String content, String model) {
        return new ConversationMessage(UUID.randomUUID(),conversationId,role,content,model,ChatMode.NORMAL,List.of(),Instant.now());
    }
}
