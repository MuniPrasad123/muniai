package com.muniai.document.api;

import com.muniai.document.domain.ExtractionStatus;
import java.time.Instant;
import java.util.UUID;

public final class DocumentDtos {
    private DocumentDtos() {}
    public record DocumentResponse(UUID id, String originalFileName, String contentType, long fileSize,
                                   ExtractionStatus extractionStatus, String extractionError, Integer pageCount,
                                   boolean fileAvailable, Instant createdAt, Instant updatedAt) {}
    public record DocumentTextResponse(UUID id, String text) {}
}
