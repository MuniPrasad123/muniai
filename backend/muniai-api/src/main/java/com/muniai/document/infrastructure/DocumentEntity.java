package com.muniai.document.infrastructure;

import com.muniai.document.domain.ExtractionStatus;
import com.muniai.document.domain.IndexingStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id private UUID id;
    @Column(name = "original_file_name", nullable = false, length = 255) private String originalFileName;
    @Column(name = "stored_file_name", nullable = false, unique = true, length = 255) private String storedFileName;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "storage_path", nullable = false, length = 500) private String storagePath;
    @Enumerated(EnumType.STRING) @Column(name = "extraction_status", nullable = false, length = 20) private ExtractionStatus extractionStatus;
    @Column(name = "extracted_text", columnDefinition = "TEXT") private String extractedText;
    @Column(name = "extraction_error", length = 500) private String extractionError;
    @Column(name = "page_count") private Integer pageCount;
    @Enumerated(EnumType.STRING) @Column(name = "indexing_status", nullable = false, length = 20) private IndexingStatus indexingStatus = IndexingStatus.NOT_INDEXED;
    @Column(name = "indexing_started_at") private Instant indexingStartedAt;
    @Column(name = "indexing_completed_at") private Instant indexingCompletedAt;
    @Column(name = "indexing_error", length = 500) private String indexingError;
    @Column(name = "chunk_count", nullable = false) private int chunkCount;
    @Column(name = "embedding_model", length = 255) private String embeddingModel;
    @Column(name = "embedding_dimension") private Integer embeddingDimension;
    @Column(name = "qdrant_collection_name", length = 255) private String qdrantCollectionName;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected DocumentEntity() {}
    public DocumentEntity(UUID id, String originalFileName, String storedFileName, String contentType,
                          long fileSize, String storagePath, Instant now) {
        this.id=id; this.originalFileName=originalFileName; this.storedFileName=storedFileName;
        this.contentType=contentType; this.fileSize=fileSize; this.storagePath=storagePath;
        this.extractionStatus=ExtractionStatus.UPLOADED; this.createdAt=now; this.updatedAt=now;
    }
    public void processing(Instant now) { extractionStatus=ExtractionStatus.PROCESSING; updatedAt=now; extractionError=null; }
    public void completed(String text, Integer pages, Instant now) {
        extractionStatus=ExtractionStatus.COMPLETED; extractedText=text; pageCount=pages;
        extractionError=null; updatedAt=now;
    }
    public void failed(String error, Instant now) {
        extractionStatus=ExtractionStatus.FAILED; extractedText=null; pageCount=null;
        extractionError=error; updatedAt=now;
    }
    public void indexingStarted(Instant now) {
        indexingStatus=IndexingStatus.PROCESSING; indexingStartedAt=now; indexingCompletedAt=null;
        indexingError=null; chunkCount=0; embeddingModel=null; embeddingDimension=null; qdrantCollectionName=null; updatedAt=now;
    }
    public void indexingCompleted(int chunks, String model, int dimension, String collection, Instant now) {
        indexingStatus=IndexingStatus.COMPLETED; chunkCount=chunks; embeddingModel=model;
        embeddingDimension=dimension; qdrantCollectionName=collection; indexingCompletedAt=now; indexingError=null; updatedAt=now;
    }
    public void indexingFailed(String error, Instant now) {
        indexingStatus=IndexingStatus.FAILED; indexingError=error; indexingCompletedAt=null; chunkCount=0; updatedAt=now;
    }
    public void indexRemoved(Instant now) {
        indexingStatus=IndexingStatus.NOT_INDEXED; indexingStartedAt=null; indexingCompletedAt=null;
        indexingError=null; chunkCount=0; embeddingModel=null; embeddingDimension=null; qdrantCollectionName=null; updatedAt=now;
    }
    public UUID getId(){return id;} public String getOriginalFileName(){return originalFileName;}
    public String getStoredFileName(){return storedFileName;} public String getContentType(){return contentType;}
    public long getFileSize(){return fileSize;} public String getStoragePath(){return storagePath;}
    public ExtractionStatus getExtractionStatus(){return extractionStatus;} public String getExtractedText(){return extractedText;}
    public String getExtractionError(){return extractionError;} public Integer getPageCount(){return pageCount;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public IndexingStatus getIndexingStatus(){return indexingStatus;} public Instant getIndexingStartedAt(){return indexingStartedAt;}
    public Instant getIndexingCompletedAt(){return indexingCompletedAt;} public String getIndexingError(){return indexingError;}
    public int getChunkCount(){return chunkCount;} public String getEmbeddingModel(){return embeddingModel;}
    public Integer getEmbeddingDimension(){return embeddingDimension;} public String getQdrantCollectionName(){return qdrantCollectionName;}
}
