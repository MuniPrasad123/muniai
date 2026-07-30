package com.muniai.conversation.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_citations")
public class MessageCitationEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;
    @Column(name = "citation_index", nullable = false) private int citationIndex;
    @Column(name = "document_id", nullable = false) private UUID documentId;
    @Column(name = "chunk_id", nullable = false) private UUID chunkId;
    @Column(name = "chunk_index", nullable = false) private int chunkIndex;
    @Column(name = "original_file_name", nullable = false, length = 255) private String originalFileName;
    @Column(name = "page_number") private Integer pageNumber;
    @Column(name = "similarity_score", nullable = false) private double similarityScore;
    @Column(name = "content_preview", nullable = false, length = 500) private String contentPreview;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected MessageCitationEntity() {}
    public MessageCitationEntity(UUID id, MessageEntity message, int citationIndex, UUID documentId, UUID chunkId,
            int chunkIndex, String originalFileName, Integer pageNumber, double similarityScore,
            String contentPreview, Instant createdAt) {
        this.id=id; this.message=message; this.citationIndex=citationIndex; this.documentId=documentId;
        this.chunkId=chunkId; this.chunkIndex=chunkIndex; this.originalFileName=originalFileName;
        this.pageNumber=pageNumber; this.similarityScore=similarityScore;
        this.contentPreview=contentPreview; this.createdAt=createdAt;
    }
    public UUID getId(){return id;} public int getCitationIndex(){return citationIndex;}
    public UUID getDocumentId(){return documentId;} public UUID getChunkId(){return chunkId;}
    public int getChunkIndex(){return chunkIndex;} public String getOriginalFileName(){return originalFileName;}
    public Integer getPageNumber(){return pageNumber;} public double getSimilarityScore(){return similarityScore;}
    public String getContentPreview(){return contentPreview;} public Instant getCreatedAt(){return createdAt;}
}
