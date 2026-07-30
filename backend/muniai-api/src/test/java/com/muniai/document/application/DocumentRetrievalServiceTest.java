package com.muniai.document.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.muniai.document.domain.IndexingStatus;
import com.muniai.document.infrastructure.*;
import com.muniai.shared.exception.DocumentException;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class DocumentRetrievalServiceTest {
    private final DocumentRepository documents=mock(DocumentRepository.class);
    private final DocumentChunkRepository chunks=mock(DocumentChunkRepository.class);
    private final EmbeddingProvider embeddings=mock(EmbeddingProvider.class);
    private final VectorStore vectors=mock(VectorStore.class);
    private final IndexingConfigurationProperties properties=new IndexingConfigurationProperties(100,10,2,
            new IndexingConfigurationProperties.Embedding(java.net.URI.create("http://localhost"),"embed",Duration.ofSeconds(1),3),
            new IndexingConfigurationProperties.Qdrant("localhost",6333,6334,"",false,"chunks","COSINE",Duration.ofSeconds(1)));
    private final DocumentIndexingService service=new DocumentIndexingService(
            documents,chunks,new DocumentChunker(properties),embeddings,vectors,properties);

    @Test void rejectsSelectedDocumentThatIsNotIndexed(){
        UUID id=UUID.randomUUID();
        when(documents.findById(id)).thenReturn(Optional.of(document(id)));
        DocumentException error=assertThrows(DocumentException.class,()->service.retrieve("question",5,Set.of(id)));
        assertEquals("DOCUMENT_NOT_INDEXED",error.code());
        verifyNoInteractions(embeddings,vectors);
    }

    @Test void filtersQdrantBySelectedDocuments(){
        UUID id=UUID.randomUUID();DocumentEntity document=document(id);
        document.processing(Instant.now());document.completed("text",null,Instant.now());
        document.indexingStarted(Instant.now());document.indexingCompleted(1,"embed",3,"chunks",Instant.now());
        when(documents.findById(id)).thenReturn(Optional.of(document));
        when(embeddings.embed("question")).thenReturn(new float[]{1,0,0});
        service.retrieve("question",5,Set.of(id));
        verify(vectors).search(any(float[].class),eq(5),eq(Set.of(id)));
    }

    @Test void returnsNoContextWithoutCallingQdrantWhenNothingIsIndexed(){
        when(documents.findByIndexingStatus(IndexingStatus.COMPLETED)).thenReturn(List.of());
        assertTrue(service.retrieve("question",5,Set.of()).isEmpty());
        verifyNoInteractions(embeddings,vectors);
    }

    private DocumentEntity document(UUID id){
        return new DocumentEntity(id,"safe.txt","stored.txt","text/plain",10,"ignored",Instant.now());
    }
}
