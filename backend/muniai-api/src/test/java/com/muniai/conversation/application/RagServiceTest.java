package com.muniai.conversation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.*;
import com.muniai.document.application.*;
import com.muniai.shared.exception.DocumentException;
import com.muniai.shared.exception.AiProviderUnavailableException;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.Test;

class RagServiceTest {
    private final DocumentIndexingService indexing=mock(DocumentIndexingService.class);
    private final LanguageModelProvider provider=mock(LanguageModelProvider.class);
    private final RagConfigurationProperties properties=new RagConfigurationProperties(
            5,10,0.45,5,6000,8,"rag-model",0.1,Duration.ofSeconds(30));
    private final RagService service=new RagService(indexing,provider,properties);

    @Test void filtersOrdersDeduplicatesAndMapsCitations(){
        UUID documentId=UUID.randomUUID(), first=UUID.randomUUID(), second=UUID.randomUUID();
        when(indexing.retrieve("question",5,Set.of(documentId))).thenReturn(List.of(
                hit(second,documentId,1,0.70,"second"),
                hit(first,documentId,0,0.90,"first"),
                hit(first,documentId,0,0.85,"duplicate"),
                hit(UUID.randomUUID(),documentId,3,0.20,"weak")));
        when(provider.complete(any())).thenReturn(new ChatCompletion(
                "The first source contains the answer. [Source 2]","rag-model","ollama"));

        var result=service.answer("question",Set.of(documentId),null,null,List.of());

        assertEquals("The first source contains the answer. [Source 1]",result.answer());
        assertEquals(2,result.citations().size());
        assertEquals(first,result.citations().getFirst().chunkId());
        assertEquals(0.90,result.citations().getFirst().similarityScore());
        verify(provider).complete(argThat(request->request.message().contains("[Source 1]")
                && request.message().contains("<retrieved_context>")
                && request.message().contains("Do not write [Source N] labels yourself")));
    }

    @Test void noRelevantContextDoesNotCallChatModel(){
        when(indexing.retrieve(anyString(),anyInt(),anySet())).thenReturn(List.of(
                hit(UUID.randomUUID(),UUID.randomUUID(),0,0.1,"weak")));
        var result=service.answer("question",Set.of(),null,null,List.of());
        assertTrue(result.noRelevantContext());
        assertEquals(RagService.NO_CONTEXT_ANSWER,result.answer());
        assertTrue(result.citations().isEmpty());
        verifyNoInteractions(provider);
    }

    @Test void validatesTopK(){
        assertThrows(DocumentException.class,()->service.answer("question",Set.of(),11,null,List.of()));
        verifyNoInteractions(indexing,provider);
    }

    @Test void chatFailureDoesNotCreateFakeRagResult(){
        when(indexing.retrieve(anyString(),anyInt(),anySet())).thenReturn(List.of(
                hit(UUID.randomUUID(),UUID.randomUUID(),0,0.8,"evidence")));
        when(provider.complete(any())).thenThrow(new AiProviderUnavailableException(new RuntimeException()));
        assertThrows(AiProviderUnavailableException.class,
                ()->service.answer("question",Set.of(),null,null,List.of()));
    }

    private VectorStore.SearchHit hit(UUID chunk,UUID document,int index,double score,String content){
        return new VectorStore.SearchHit(chunk,document,index,score,content,"doc.pdf");
    }
}
