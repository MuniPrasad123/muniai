package com.muniai.document.application;

import com.muniai.document.domain.*;
import com.muniai.document.infrastructure.*;
import com.muniai.shared.exception.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class DocumentIndexingService {
    private final DocumentRepository documents; private final DocumentChunkRepository chunks;
    private final DocumentChunker chunker; private final EmbeddingProvider embeddings; private final VectorStore vectors;
    private final IndexingConfigurationProperties properties;
    public DocumentIndexingService(DocumentRepository documents,DocumentChunkRepository chunks,DocumentChunker chunker,
            EmbeddingProvider embeddings,VectorStore vectors,IndexingConfigurationProperties properties){
        this.documents=documents;this.chunks=chunks;this.chunker=chunker;this.embeddings=embeddings;this.vectors=vectors;this.properties=properties;
    }
    public DocumentEntity index(UUID id){return replaceIndex(id);}
    public DocumentEntity reindex(UUID id){return replaceIndex(id);}
    public DocumentEntity status(UUID id){return get(id);}
    public void removeIndex(UUID id){
        DocumentEntity document=get(id);
        vectors.ensureCollection();vectors.deleteByDocument(id);chunks.deleteByDocumentId(id);
        document.indexRemoved(Instant.now());documents.saveAndFlush(document);
    }
    public void cleanupBeforeDocumentDelete(DocumentEntity document){
        if(document.getIndexingStatus()!=IndexingStatus.NOT_INDEXED||chunks.countByDocumentId(document.getId())>0){
            vectors.ensureCollection();vectors.deleteByDocument(document.getId());chunks.deleteByDocumentId(document.getId());
            document.indexRemoved(Instant.now());documents.saveAndFlush(document);
        }
    }
    public List<VectorStore.SearchHit> search(String query,int limit,UUID documentId){
        if(query==null||query.isBlank()) throw new DocumentException("INVALID_VECTOR_SEARCH","A non-empty diagnostic query is required.");
        if(limit<1||limit>20) throw new DocumentException("INVALID_VECTOR_SEARCH","The result limit must be between 1 and 20.");
        Set<UUID> ids=documentId==null?Set.of():Set.of(documentId);
        validateIndexed(ids);
        vectors.ensureCollection();return vectors.search(embeddings.embed(query.strip()),limit,ids);
    }
    public List<VectorStore.SearchHit> retrieve(String query,int limit,Set<UUID> documentIds){
        if(query==null||query.isBlank()) throw new DocumentException("INVALID_RAG_REQUEST","A non-empty question is required.");
        validateIndexed(documentIds);
        if(documentIds.isEmpty()&&documents.findByIndexingStatus(IndexingStatus.COMPLETED).isEmpty()) return List.of();
        vectors.ensureCollection();
        return vectors.search(embeddings.embed(query.strip()),limit,documentIds);
    }
    private void validateIndexed(Set<UUID> documentIds){
        for(UUID id:documentIds){
            DocumentEntity document=get(id);
            if(document.getIndexingStatus()!=IndexingStatus.COMPLETED)
                throw new DocumentException("DOCUMENT_NOT_INDEXED","Only documents with completed indexing can be used for document chat.");
        }
    }
    private DocumentEntity replaceIndex(UUID id){
        DocumentEntity document=get(id);
        if(document.getExtractionStatus()!=ExtractionStatus.COMPLETED)
            throw new DocumentException("DOCUMENT_NOT_EXTRACTED","Only documents with completed extraction can be indexed.");
        if(document.getExtractedText()==null||document.getExtractedText().isBlank())
            throw new DocumentException("DOCUMENT_TEXT_EMPTY","The extracted document text is empty.");
        document.indexingStarted(Instant.now());documents.saveAndFlush(document);
        try{
            vectors.ensureCollection();vectors.deleteByDocument(id);chunks.deleteByDocumentId(id);
            List<DocumentChunker.Chunk> generated=chunker.chunk(document.getExtractedText());
            if(generated.isEmpty()) throw new DocumentException("DOCUMENT_CHUNKING_FAILED","No non-empty document chunks were created.");
            Instant now=Instant.now();List<DocumentChunkEntity> entities=new ArrayList<>();
            for(var chunk:generated){
                UUID chunkId=UUID.randomUUID(),pointId=UUID.randomUUID();
                entities.add(new DocumentChunkEntity(chunkId,id,chunk.index(),chunk.content(),chunk.contentHash(),
                        chunk.characterStart(),chunk.characterEnd(),chunk.tokenCountEstimate(),pointId,embeddings.model(),now));
            }
            chunks.saveAllAndFlush(entities);
            int batchSize=properties.batchSize();
            for(int offset=0;offset<entities.size();offset+=batchSize){
                List<VectorStore.VectorPoint> batch=new ArrayList<>();
                for(DocumentChunkEntity chunk:entities.subList(offset,Math.min(entities.size(),offset+batchSize))){
                    float[] vector=embeddings.embed(chunk.getContent());
                    batch.add(new VectorStore.VectorPoint(chunk.getQdrantPointId(),vector,id,chunk.getId(),chunk.getChunkIndex(),
                            document.getOriginalFileName(),document.getContentType(),chunk.getContent(),chunk.getContentHash(),
                            chunk.getCharacterStart(),chunk.getCharacterEnd(),chunk.getTokenCountEstimate(),
                            embeddings.model(),chunk.getCreatedAt()));
                }
                vectors.upsert(batch);
            }
            long actual=vectors.countByDocument(id);
            if(actual!=entities.size()) throw new DocumentException("QDRANT_POINT_COUNT_MISMATCH",
                    "Qdrant point count does not match the generated chunk count.");
            document.indexingCompleted(entities.size(),embeddings.model(),embeddings.dimension(),vectors.collectionName(),Instant.now());
            return documents.saveAndFlush(document);
        }catch(RuntimeException exception){
            try{vectors.deleteByDocument(id);}catch(RuntimeException cleanup){exception.addSuppressed(cleanup);}
            try{chunks.deleteByDocumentId(id);}catch(RuntimeException cleanup){exception.addSuppressed(cleanup);}
            document.indexingFailed(safeError(exception),Instant.now());documents.saveAndFlush(document);
            throw exception;
        }
    }
    private DocumentEntity get(UUID id){return documents.findById(id).orElseThrow(DocumentNotFoundException::new);}
    private String safeError(RuntimeException exception){
        if(exception instanceof DocumentException && exception.getMessage()!=null) return exception.getMessage();
        return "The document could not be indexed. Retry after checking local embedding and Qdrant services.";
    }
}
