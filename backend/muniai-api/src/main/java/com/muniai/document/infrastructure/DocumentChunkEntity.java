package com.muniai.document.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="document_chunks")
public class DocumentChunkEntity {
    @Id private UUID id;
    @Column(name="document_id",nullable=false) private UUID documentId;
    @Column(name="chunk_index",nullable=false) private int chunkIndex;
    @Column(nullable=false,columnDefinition="TEXT") private String content;
    @Column(name="content_hash",nullable=false,length=64) private String contentHash;
    @Column(name="character_start",nullable=false) private int characterStart;
    @Column(name="character_end",nullable=false) private int characterEnd;
    @Column(name="token_count_estimate",nullable=false) private int tokenCountEstimate;
    @Column(name="qdrant_point_id",nullable=false,unique=true) private UUID qdrantPointId;
    @Column(name="embedding_model",nullable=false,length=255) private String embeddingModel;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected DocumentChunkEntity(){}
    public DocumentChunkEntity(UUID id,UUID documentId,int chunkIndex,String content,String contentHash,
            int characterStart,int characterEnd,int tokenCountEstimate,UUID qdrantPointId,String embeddingModel,Instant now){
        this.id=id;this.documentId=documentId;this.chunkIndex=chunkIndex;this.content=content;this.contentHash=contentHash;
        this.characterStart=characterStart;this.characterEnd=characterEnd;this.tokenCountEstimate=tokenCountEstimate;
        this.qdrantPointId=qdrantPointId;this.embeddingModel=embeddingModel;this.createdAt=now;this.updatedAt=now;
    }
    public UUID getId(){return id;} public UUID getDocumentId(){return documentId;} public int getChunkIndex(){return chunkIndex;}
    public String getContent(){return content;} public String getContentHash(){return contentHash;}
    public int getCharacterStart(){return characterStart;} public int getCharacterEnd(){return characterEnd;}
    public int getTokenCountEstimate(){return tokenCountEstimate;} public UUID getQdrantPointId(){return qdrantPointId;}
    public String getEmbeddingModel(){return embeddingModel;} public Instant getCreatedAt(){return createdAt;}
}
