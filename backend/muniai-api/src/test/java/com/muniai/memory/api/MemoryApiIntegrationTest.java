package com.muniai.memory.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.*;
import com.muniai.bootstrap.MuniAiApplication;
import com.muniai.conversation.infrastructure.ConversationRepository;
import com.muniai.document.application.DocumentIndexingService;
import com.muniai.memory.infrastructure.UserMemoryRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes=MuniAiApplication.class) @AutoConfigureMockMvc @ActiveProfiles("test")
class MemoryApiIntegrationTest {
    @Autowired MockMvc mvc; @Autowired UserMemoryRepository memories; @Autowired ConversationRepository conversations;
    @MockBean LanguageModelProvider provider; @MockBean DocumentIndexingService indexing;
    private final ObjectMapper json=new ObjectMapper();
    @BeforeEach void clean(){conversations.deleteAllInBatch();memories.deleteAllInBatch();reset(provider,indexing);}

    @Test void lifecycleEditDisableEnableAndSoftDelete() throws Exception {
        String id=proposal("PROFILE","preferred_name","Muni","Preferred name");
        mvc.perform(post("/api/v1/memories/{id}/approve",id)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"));
        mvc.perform(patch("/api/v1/memories/{id}",id).contentType("application/json").content("""
                {"category":"PROFILE","key":"preferred_name","value":"Muni P","displayLabel":"Preferred name"}
                """)).andExpect(jsonPath("$.value").value("Muni P"));
        mvc.perform(post("/api/v1/memories/{id}/disable",id)).andExpect(jsonPath("$.status").value("DISABLED"));
        mvc.perform(get("/api/v1/memories/active")).andExpect(jsonPath("$").isEmpty());
        mvc.perform(post("/api/v1/memories/{id}/enable",id)).andExpect(jsonPath("$.status").value("ACTIVE"));
        mvc.perform(delete("/api/v1/memories/{id}",id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/memories/{id}",id)).andExpect(jsonPath("$.status").value("DELETED"));
    }
    @Test void sensitiveValueIsRejectedWithoutProposal() throws Exception {
        mvc.perform(post("/api/v1/memories/proposals").contentType("application/json").content("""
                {"category":"CUSTOM","key":"password","value":"Example123","displayLabel":"Password","sourceType":"MANUAL_ENTRY"}
                """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SENSITIVE_MEMORY_REJECTED"));
        Assertions.assertEquals(0,memories.count());
    }
    @Test void singularConflictRequiresExplicitSupersede() throws Exception {
        String oldId=proposal("GOAL","target_role","AI Engineer","Target role");
        mvc.perform(post("/api/v1/memories/{id}/approve",oldId)).andExpect(status().isOk());
        String replacement=proposal("GOAL","target_role","AI Architect","Target role");
        mvc.perform(post("/api/v1/memories/{id}/approve",replacement)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMORY_CONFLICT"));
        mvc.perform(post("/api/v1/memories/{id}/supersede",oldId).contentType("application/json")
                .content("{\"replacementMemoryId\":\""+replacement+"\"}"))
                .andExpect(jsonPath("$.status").value("ACTIVE")).andExpect(jsonPath("$.value").value("AI Architect"));
        mvc.perform(get("/api/v1/memories/{id}",oldId)).andExpect(jsonPath("$.status").value("SUPERSEDED"));
    }
    @Test void explicitProposalIsApprovedAndUsedAcrossConversations() throws Exception {
        String first=createConversation();
        String body=mvc.perform(post("/api/v1/conversations/{id}/messages",first).contentType("application/json")
                .content("{\"message\":\"Remember my target role is AI Engineer.\",\"mode\":\"NORMAL\"}"))
                .andExpect(jsonPath("$.memoryProposal.status").value("PROPOSED"))
                .andExpect(jsonPath("$.assistantMessage.usedMemories").isEmpty()).andReturn().getResponse().getContentAsString();
        String memoryId=json.readTree(body).get("memoryProposal").get("id").asText();
        mvc.perform(post("/api/v1/memories/{id}/approve",memoryId)).andExpect(jsonPath("$.status").value("ACTIVE"));
        String second=createConversation();when(provider.complete(any())).thenReturn(new ChatCompletion("Learn machine learning fundamentals.","model","ollama"));
        mvc.perform(post("/api/v1/conversations/{id}/messages",second).contentType("application/json")
                .content("{\"message\":\"What should I learn next?\",\"mode\":\"NORMAL\"}"))
                .andExpect(jsonPath("$.assistantMessage.usedMemories[0].key").value("target_role"));
        verify(provider).complete(argThat(request->request.message().contains("<approved_memories>")&&request.message().contains("AI Engineer")&&request.message().contains("untrusted user-provided reference data")));
        mvc.perform(get("/api/v1/memories/{id}",memoryId)).andExpect(jsonPath("$.usageCount").value(1));
    }
    private String proposal(String category,String key,String value,String label)throws Exception{
        String body=mvc.perform(post("/api/v1/memories/proposals").contentType("application/json").content(
                "{\"category\":\""+category+"\",\"key\":\""+key+"\",\"value\":\""+value+"\",\"displayLabel\":\""+label+"\",\"sourceType\":\"MANUAL_ENTRY\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();return json.readTree(body).get("id").asText();}
    private String createConversation()throws Exception{return json.readTree(mvc.perform(post("/api/v1/conversations")).andReturn().getResponse().getContentAsString()).get("id").asText();}
}
