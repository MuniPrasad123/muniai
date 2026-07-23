package com.muniai.document.infrastructure;

import com.muniai.document.domain.ExtractionStatus;
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
    public UUID getId(){return id;} public String getOriginalFileName(){return originalFileName;}
    public String getStoredFileName(){return storedFileName;} public String getContentType(){return contentType;}
    public long getFileSize(){return fileSize;} public String getStoragePath(){return storagePath;}
    public ExtractionStatus getExtractionStatus(){return extractionStatus;} public String getExtractedText(){return extractedText;}
    public String getExtractionError(){return extractionError;} public Integer getPageCount(){return pageCount;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
