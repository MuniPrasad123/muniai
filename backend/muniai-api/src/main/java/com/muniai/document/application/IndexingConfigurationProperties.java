package com.muniai.document.application;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("muniai.indexing")
public record IndexingConfigurationProperties(int chunkSize, int chunkOverlap, int batchSize,
        Embedding embedding, Qdrant qdrant) {
    public IndexingConfigurationProperties {
        if(chunkSize<=0) chunkSize=1000;
        if(chunkOverlap<0) chunkOverlap=150;
        if(chunkOverlap>=chunkSize) throw new IllegalArgumentException("Chunk overlap must be smaller than chunk size.");
        if(batchSize<=0) batchSize=8;
    }
    public record Embedding(URI baseUrl,String model,Duration timeout,int dimension){}
    public record Qdrant(String host,int httpPort,int grpcPort,String apiKey,boolean httpsEnabled,
            String collectionName,String distanceMetric,Duration timeout){}
}
