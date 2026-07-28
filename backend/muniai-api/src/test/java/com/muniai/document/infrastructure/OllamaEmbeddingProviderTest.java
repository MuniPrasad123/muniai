package com.muniai.document.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import com.muniai.shared.exception.DocumentException;
import java.util.List;
import org.junit.jupiter.api.Test;

class OllamaEmbeddingProviderTest {
    @Test void parsesEmbeddingResponse(){
        var response=new OllamaEmbeddingProvider.Response(List.of(List.of(.1,-.2,.3)));
        assertArrayEquals(new float[]{.1f,-.2f,.3f},OllamaEmbeddingProvider.parse(response,3));
    }
    @Test void rejectsEmptyVector(){
        DocumentException error=assertThrows(DocumentException.class,
                ()->OllamaEmbeddingProvider.parse(new OllamaEmbeddingProvider.Response(List.of()),3));
        assertEquals("EMPTY_EMBEDDING",error.code());
    }
    @Test void rejectsDimensionMismatch(){
        var response=new OllamaEmbeddingProvider.Response(List.of(List.of(.1,.2)));
        DocumentException error=assertThrows(DocumentException.class,()->OllamaEmbeddingProvider.parse(response,3));
        assertEquals("EMBEDDING_DIMENSION_MISMATCH",error.code());
    }
}
