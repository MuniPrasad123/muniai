package com.muniai.conversation.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.*;
import com.muniai.bootstrap.MuniAiApplication;
import com.muniai.conversation.infrastructure.ConversationRepository;
import com.muniai.conversation.infrastructure.MessageRepository;
import com.muniai.shared.exception.AiProviderUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = MuniAiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConversationApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ConversationRepository conversations;
    @Autowired MessageRepository messages;
    @MockBean LanguageModelProvider provider;

    @BeforeEach
    void clean() {
        conversations.deleteAllInBatch();
        reset(provider);
    }

    @Test
    void createSendLoadRenameAndDeleteConversation() throws Exception {
        String createBody = mvc.perform(post("/api/v1/conversations"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("New conversation"))
                .andReturn().getResponse().getContentAsString();
        String id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(createBody).get("id").asText();
        when(provider.complete(any())).thenReturn(new ChatCompletion("assistant answer", "llama3.2:3b", "ollama"));

        mvc.perform(post("/api/v1/conversations/{id}/messages", id).contentType("application/json")
                        .content("{\"message\":\"A first question with a useful title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userMessage.role").value("USER"))
                .andExpect(jsonPath("$.assistantMessage.role").value("ASSISTANT"))
                .andExpect(jsonPath("$.assistantMessage.model").value("llama3.2:3b"));

        mvc.perform(get("/api/v1/conversations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("A first question with a useful title"))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[1].role").value("ASSISTANT"));

        mvc.perform(patch("/api/v1/conversations/{id}", id).contentType("application/json")
                        .content("{\"title\":\"Renamed chat\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Renamed chat"));

        mvc.perform(get("/api/v1/conversations"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(delete("/api/v1/conversations/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/conversations/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONVERSATION_NOT_FOUND"));
    }

    @Test
    void deleteAllRemovesEveryConversation() throws Exception {
        mvc.perform(post("/api/v1/conversations"));
        mvc.perform(post("/api/v1/conversations"));
        mvc.perform(delete("/api/v1/conversations")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/conversations")).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void providerFailurePreservesUserWithoutFakeAssistant() throws Exception {
        String body = mvc.perform(post("/api/v1/conversations")).andReturn().getResponse().getContentAsString();
        String id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("id").asText();
        when(provider.complete(any())).thenThrow(new AiProviderUnavailableException(new RuntimeException()));

        mvc.perform(post("/api/v1/conversations/{id}/messages", id).contentType("application/json")
                        .content("{\"message\":\"save me\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_PROVIDER_UNAVAILABLE"));

        mvc.perform(get("/api/v1/conversations/{id}", id))
                .andExpect(jsonPath("$.messages.length()").value(1))
                .andExpect(jsonPath("$.messages[0].role").value("USER"));
    }
}
