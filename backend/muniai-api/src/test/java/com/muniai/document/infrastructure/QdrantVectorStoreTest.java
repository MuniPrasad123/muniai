package com.muniai.document.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muniai.document.application.*;
import java.net.URI;
import java.time.*;
import java.util.UUID;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class QdrantVectorStoreTest {
    @Test void buildsMultiDocumentFilter(){
        var embedding=new IndexingConfigurationProperties.Embedding(URI.create("http://localhost"),"embed",Duration.ofSeconds(1),3);
        var qdrant=new IndexingConfigurationProperties.Qdrant("localhost",6333,6334,"",false,"chunks","COSINE",Duration.ofSeconds(1));
        var store=new QdrantVectorStore(new IndexingConfigurationProperties(100,10,2,embedding,qdrant),RestClient.builder(),new ObjectMapper());
        UUID one=UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID two=UUID.fromString("00000000-0000-0000-0000-000000000002");
        String json=new ObjectMapper().valueToTree(store.filter(Set.of(two,one))).toString();
        assertTrue(json.contains("\"any\":[\""+one+"\",\""+two+"\"]"));
    }
    @Test void mapsCollectionConfigurationToQdrantWireFormat() throws Exception {
        var embedding=new IndexingConfigurationProperties.Embedding(URI.create("http://localhost"),"embed",Duration.ofSeconds(1),768);
        var qdrant=new IndexingConfigurationProperties.Qdrant("localhost",6333,6334,"",false,"chunks","COSINE",Duration.ofSeconds(1));
        var mapper=new ObjectMapper();
        var store=new QdrantVectorStore(new IndexingConfigurationProperties(100,10,2,embedding,qdrant),RestClient.builder(),mapper);

        var json=mapper.valueToTree(store.collectionConfiguration());
        assertEquals(768,json.path("vectors").path("size").asInt());
        assertEquals("Cosine",json.path("vectors").path("distance").asText());
    }

    @Test void mapsSafeTraceablePayloadWithoutFilesystemPath(){
        var embedding=new IndexingConfigurationProperties.Embedding(URI.create("http://localhost"),"embed",Duration.ofSeconds(1),3);
        var qdrant=new IndexingConfigurationProperties.Qdrant("localhost",6333,6334,"",false,"chunks","COSINE",Duration.ofSeconds(1));
        var store=new QdrantVectorStore(new IndexingConfigurationProperties(100,10,2,embedding,qdrant),RestClient.builder(),new ObjectMapper());
        UUID documentId=UUID.randomUUID(),chunkId=UUID.randomUUID();
        var point=new VectorStore.VectorPoint(UUID.randomUUID(),new float[]{1,0,0},documentId,chunkId,2,
                "safe.txt","text/plain","synthetic content","hash",20,37,5,"embed",Instant.parse("2026-07-23T00:00:00Z"));
        var payload=store.payload(point);
        assertEquals(documentId.toString(),payload.get("documentId"));assertEquals(chunkId.toString(),payload.get("chunkId"));
        assertEquals("synthetic content",payload.get("content"));assertFalse(payload.containsKey("storagePath"));
    }
}
