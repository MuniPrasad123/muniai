package com.muniai.document.application;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class DocumentChunkerTest {
    private final DocumentChunker chunker=new DocumentChunker(new IndexingConfigurationProperties(45,12,2,
            new IndexingConfigurationProperties.Embedding(URI.create("http://localhost"),"embed",Duration.ofSeconds(1),3),
            new IndexingConfigurationProperties.Qdrant("localhost",6333,6334,"",false,"chunks","COSINE",Duration.ofSeconds(1))));
    @Test void createsOrderedOverlappingStableChunks(){
        String text="Paragraph one has useful words.\n\nParagraph two continues the subject.\n\nParagraph three finishes it.";
        var chunks=chunker.chunk(text);
        assertTrue(chunks.size()>=2);
        for(int i=0;i<chunks.size();i++){
            assertEquals(i,chunks.get(i).index());assertFalse(chunks.get(i).content().isBlank());
            assertEquals(64,chunks.get(i).contentHash().length());
            if(i>0) assertTrue(chunks.get(i).characterStart()<chunks.get(i-1).characterEnd());
        }
        assertEquals(chunks,chunker.chunk(text));
    }
    @Test void normalizesWhitespaceAndHandlesEmptyText(){
        assertEquals("First paragraph.\n\nSecond line.",chunker.normalize(" First   paragraph. \n\n Second\tline. "));
        assertTrue(chunker.chunk(" \n\t ").isEmpty());
    }
    @Test void rejectsOverlapEqualToSize(){
        assertThrows(IllegalArgumentException.class,()->new IndexingConfigurationProperties(10,10,1,null,null));
    }
}
