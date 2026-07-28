package com.muniai.document.api;

import com.muniai.document.domain.ExtractionStatus;
import com.muniai.document.domain.IndexingStatus;
import java.time.Instant;
import java.util.UUID;

public final class DocumentDtos {
    private DocumentDtos() {}
    public record DocumentResponse(UUID id, String originalFileName, String contentType, long fileSize,
                                   ExtractionStatus extractionStatus, String extractionError, Integer pageCount,
                                   boolean fileAvailable, IndexingStatus indexingStatus, Instant indexingStartedAt,
                                   Instant indexingCompletedAt, String indexingError, int chunkCount,
                                   String embeddingModel, Integer embeddingDimension, String qdrantCollectionName,
                                   Instant createdAt, Instant updatedAt) {}
    public record DocumentTextResponse(UUID id, String text) {}
    public record IndexStatusResponse(UUID id,IndexingStatus indexingStatus,Instant indexingStartedAt,
            Instant indexingCompletedAt,String indexingError,int chunkCount,String embeddingModel,
            Integer embeddingDimension,String qdrantCollectionName){}
}
