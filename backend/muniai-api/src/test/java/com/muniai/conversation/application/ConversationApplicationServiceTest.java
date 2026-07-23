package com.muniai.conversation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.*;
import com.muniai.chat.application.ChatConfigurationProperties;
import com.muniai.conversation.domain.*;
import com.muniai.shared.exception.AiProviderUnavailableException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConversationApplicationServiceTest {
    private final ConversationPersistenceService persistence = mock(ConversationPersistenceService.class);
    private final LanguageModelProvider provider = mock(LanguageModelProvider.class);
    private final ConversationApplicationService service = new ConversationApplicationService(
            persistence, provider, new ChatConfigurationProperties(100));

    @Test
    void savesUserThenAssistantAroundProviderCall() {
        UUID conversationId = UUID.randomUUID();
        ConversationMessage user = message(conversationId, MessageRole.USER, "hello", null);
        ConversationMessage assistant = message(conversationId, MessageRole.ASSISTANT, "answer", "model");
        when(persistence.saveUserMessage(conversationId, "hello")).thenReturn(user);
        when(provider.complete(new ChatCompletionRequest("hello"))).thenReturn(new ChatCompletion("answer", "model", "ollama"));
        when(persistence.saveAssistantMessage(conversationId, "answer", "model")).thenReturn(assistant);

        ConversationApplicationService.SendMessageResult result = service.send(conversationId, "hello");

        assertEquals(assistant, result.assistantMessage());
        var ordered = inOrder(persistence, provider);
        ordered.verify(persistence).saveUserMessage(conversationId, "hello");
        ordered.verify(provider).complete(new ChatCompletionRequest("hello"));
        ordered.verify(persistence).saveAssistantMessage(conversationId, "answer", "model");
    }

    @Test
    void preservesUserAndDoesNotCreateAssistantWhenProviderFails() {
        UUID conversationId = UUID.randomUUID();
        when(persistence.saveUserMessage(conversationId, "hello")).thenReturn(message(conversationId, MessageRole.USER, "hello", null));
        when(provider.complete(any())).thenThrow(new AiProviderUnavailableException(new RuntimeException()));

        assertThrows(AiProviderUnavailableException.class, () -> service.send(conversationId, "hello"));
        verify(persistence).saveUserMessage(conversationId, "hello");
        verify(persistence, never()).saveAssistantMessage(any(), any(), any());
    }

    private ConversationMessage message(UUID conversationId, MessageRole role, String content, String model) {
        return new ConversationMessage(UUID.randomUUID(), conversationId, role, content, model, Instant.now());
    }
}
