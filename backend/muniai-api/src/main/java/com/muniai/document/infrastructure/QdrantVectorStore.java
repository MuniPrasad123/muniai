package com.muniai.document.infrastructure;

import com.fasterxml.jackson.databind.*;
import com.muniai.document.application.*;
import com.muniai.shared.exception.DocumentException;
import java.util.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class QdrantVectorStore implements VectorStore {
    private final RestClient client; private final ObjectMapper mapper;
    private final IndexingConfigurationProperties properties;
    public QdrantVectorStore(IndexingConfigurationProperties properties,RestClient.Builder builder,ObjectMapper mapper){
        this.properties=properties;this.mapper=mapper;
        var q=properties.qdrant(); var factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(q.timeout());factory.setReadTimeout(q.timeout());
        RestClient.Builder configured=builder.baseUrl((q.httpsEnabled()?"https":"http")+"://"+q.host()+":"+q.httpPort())
                .requestFactory(factory);
        if(q.apiKey()!=null&&!q.apiKey().isBlank()) configured.defaultHeader("api-key",q.apiKey());
        this.client=configured.build();
    }
    public void ensureCollection(){
        try{
            JsonNode response=client.get().uri("/collections/{name}",collectionName()).retrieve().body(JsonNode.class);
            JsonNode vectors=response==null?null:response.path("result").path("config").path("params").path("vectors");
            int size=vectors==null?0:vectors.path("size").asInt();
            String distance=vectors==null?"":vectors.path("distance").asText();
            if(size!=properties.embedding().dimension()||!distance.equalsIgnoreCase(properties.qdrant().distanceMetric()))
                throw new DocumentException("QDRANT_COLLECTION_MISMATCH",
                        "The Qdrant collection vector size or distance metric is incompatible.");
        }catch(HttpClientErrorException.NotFound missing){
            request("QDRANT_COLLECTION_CREATE_FAILED",()->client.put().uri("/collections/{name}",collectionName())
                    .body(collectionConfiguration()).retrieve().toBodilessEntity());
        }catch(DocumentException exception){throw exception;}
        catch(RestClientException exception){throw unavailable(exception);}
    }
    public void upsert(List<VectorPoint> points){
        List<Map<String,Object>> values=points.stream().map(point->Map.<String,Object>of(
                "id",point.pointId().toString(),"vector",point.vector(),"payload",payload(point))).toList();
        request("QDRANT_UPSERT_FAILED",()->client.put().uri("/collections/{name}/points?wait=true",collectionName())
                .body(Map.of("points",values)).retrieve().toBodilessEntity());
    }
    public void deleteByDocument(UUID documentId){
        request("QDRANT_DELETE_FAILED",()->client.post().uri("/collections/{name}/points/delete?wait=true",collectionName())
                .body(Map.of("filter",filter(documentId))).retrieve().toBodilessEntity());
    }
    public long countByDocument(UUID documentId){
        try{
            JsonNode response=client.post().uri("/collections/{name}/points/count",collectionName())
                    .body(Map.of("filter",filter(documentId),"exact",true)).retrieve().body(JsonNode.class);
            return response==null?0:response.path("result").path("count").asLong();
        }catch(RestClientException exception){throw unavailable(exception);}
    }
    public List<SearchHit> search(float[] vector,int limit,UUID documentId){
        Map<String,Object> body=new LinkedHashMap<>();body.put("vector",vector);body.put("limit",limit);body.put("with_payload",true);
        if(documentId!=null) body.put("filter",filter(documentId));
        try{
            JsonNode response=client.post().uri("/collections/{name}/points/search",collectionName())
                    .body(body).retrieve().body(JsonNode.class);
            List<SearchHit> hits=new ArrayList<>();
            if(response!=null) for(JsonNode node:response.path("result")){
                JsonNode p=node.path("payload");
                hits.add(new SearchHit(UUID.fromString(p.path("chunkId").asText()),UUID.fromString(p.path("documentId").asText()),
                        p.path("chunkIndex").asInt(),node.path("score").asDouble(),p.path("content").asText(),
                        p.path("originalFileName").asText()));
            }
            return hits;
        }catch(RestClientException exception){throw unavailable(exception);}
    }
    public String collectionName(){return properties.qdrant().collectionName();}
    Map<String,Object> collectionConfiguration(){
        String configured=properties.qdrant().distanceMetric().toUpperCase(Locale.ROOT);
        String distance=switch(configured){
            case "COSINE" -> "Cosine";
            case "DOT" -> "Dot";
            case "EUCLID" -> "Euclid";
            case "MANHATTAN" -> "Manhattan";
            default -> throw new DocumentException("QDRANT_DISTANCE_METRIC_INVALID",
                    "The configured Qdrant distance metric is unsupported.");
        };
        return Map.of("vectors",Map.of("size",properties.embedding().dimension(),"distance",distance));
    }
    Map<String,Object> payload(VectorPoint p){
        Map<String,Object> value=new LinkedHashMap<>();
        value.put("documentId",p.documentId().toString());value.put("chunkId",p.chunkId().toString());
        value.put("chunkIndex",p.chunkIndex());value.put("originalFileName",p.originalFileName());
        value.put("contentType",p.contentType());value.put("content",p.content());value.put("contentHash",p.contentHash());
        value.put("characterStart",p.characterStart());value.put("characterEnd",p.characterEnd());
        value.put("tokenCountEstimate",p.tokenCountEstimate());value.put("embeddingModel",p.embeddingModel());
        value.put("createdAt",p.createdAt().toString());return value;
    }
    private Map<String,Object> filter(UUID id){return Map.of("must",List.of(Map.of("key","documentId","match",Map.of("value",id.toString()))));}
    private void request(String code,Runnable action){try{action.run();}catch(RestClientException exception){throw new DocumentException(code,"Qdrant could not complete the vector operation.",exception);}}
    private DocumentException unavailable(Exception cause){return new DocumentException("QDRANT_UNAVAILABLE","The local Qdrant vector database is unavailable.",cause);}
}
