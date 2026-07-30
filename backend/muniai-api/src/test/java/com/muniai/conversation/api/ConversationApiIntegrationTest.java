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
import com.muniai.document.application.DocumentIndexingService;
import com.muniai.document.application.VectorStore;
import java.util.List;
import java.util.UUID;
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
    @MockBean DocumentIndexingService indexing;

    @BeforeEach
    void clean() {
        conversations.deleteAllInBatch();
        reset(provider);
        reset(indexing);
    }

    @Test
    void documentRagPersistsStructuredCitations() throws Exception {
        String createBody=mvc.perform(post("/api/v1/conversations")).andReturn().getResponse().getContentAsString();
        String id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(createBody).get("id").asText();
        UUID documentId=UUID.randomUUID(),chunkId=UUID.randomUUID();
        when(indexing.retrieve(eq("How is traffic routed?"),eq(5),eq(java.util.Set.of(documentId))))
                .thenReturn(List.of(new VectorStore.SearchHit(chunkId,documentId,7,0.82,
                        "The load balancer forwards traffic to a target group.","architecture.pdf")));
        when(provider.complete(any())).thenReturn(new ChatCompletion(
                "Traffic is forwarded through the load balancer. [Source 1]","llama3.2:3b","ollama"));

        mvc.perform(post("/api/v1/conversations/{id}/messages",id).contentType("application/json")
                        .content("{\"message\":\"How is traffic routed?\",\"mode\":\"DOCUMENT_RAG\","
                                +"\"documentIds\":[\""+documentId+"\"],\"topK\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistantMessage.mode").value("DOCUMENT_RAG"))
                .andExpect(jsonPath("$.assistantMessage.citations[0].documentId").value(documentId.toString()))
                .andExpect(jsonPath("$.assistantMessage.citations[0].chunkIndex").value(7))
                .andExpect(jsonPath("$.assistantMessage.citations[0].similarityScore").value(0.82));

        mvc.perform(get("/api/v1/conversations/{id}",id))
                .andExpect(jsonPath("$.messages[1].citations.length()").value(1))
                .andExpect(jsonPath("$.messages[1].citations[0].originalFileName").value("architecture.pdf"));
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
