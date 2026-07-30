package com.muniai.conversation.domain;

import java.time.Instant;
import java.util.UUID;

public record MessageCitation(UUID id, int citationIndex, UUID documentId, String originalFileName,
                              UUID chunkId, int chunkIndex, Integer pageNumber, double similarityScore,
                              String contentPreview, Instant createdAt) {}
