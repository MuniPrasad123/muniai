package com.muniai.document.application;

import java.time.Instant;
import java.util.*;

public interface VectorStore {
    void ensureCollection();
    void upsert(List<VectorPoint> points);
    void deleteByDocument(UUID documentId);
    long countByDocument(UUID documentId);
    List<SearchHit> search(float[] vector,int limit,Set<UUID> documentIds);
    String collectionName();
    record VectorPoint(UUID pointId,float[] vector,UUID documentId,UUID chunkId,int chunkIndex,String originalFileName,
        String contentType,String content,String contentHash,int characterStart,int characterEnd,int tokenCountEstimate,
        String embeddingModel,Instant createdAt){}
    record SearchHit(UUID chunkId,UUID documentId,int chunkIndex,double score,String content,String originalFileName){}
}
