package com.muniai.document.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.muniai.document.application.*;
import com.muniai.shared.exception.DocumentException;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class OllamaEmbeddingProvider implements EmbeddingProvider {
    private final RestClient client;
    private final IndexingConfigurationProperties.Embedding properties;
    public OllamaEmbeddingProvider(IndexingConfigurationProperties configuration,RestClient.Builder builder){
        this.properties=configuration.embedding();
        SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.timeout()); factory.setReadTimeout(properties.timeout());
        this.client=builder.baseUrl(properties.baseUrl().toString()).requestFactory(factory).build();
    }
    public float[] embed(String text){
        try{
            Response response=client.post().uri("/api/embed").body(new Request(model(),text)).retrieve().body(Response.class);
            return parse(response,dimension());
        }catch(DocumentException exception){throw exception;}
        catch(RestClientException exception){throw new DocumentException("EMBEDDING_PROVIDER_UNAVAILABLE",
                "The local embedding model is unavailable.",exception);}
    }
    static float[] parse(Response response,int dimension){
            if(response==null||response.embeddings()==null||response.embeddings().isEmpty()
                    ||response.embeddings().getFirst()==null||response.embeddings().getFirst().isEmpty())
                throw new DocumentException("EMPTY_EMBEDDING","The embedding model returned an empty vector.");
            List<Double> values=response.embeddings().getFirst();
            if(values.size()!=dimension) throw new DocumentException("EMBEDDING_DIMENSION_MISMATCH",
                    "The embedding vector dimension does not match the configured dimension.");
            float[] vector=new float[values.size()];
            for(int i=0;i<values.size();i++) vector[i]=values.get(i).floatValue();
            return vector;
    }
    public String model(){return properties.model();}
    public int dimension(){return properties.dimension();}
    record Request(String model,String input){}
    @JsonIgnoreProperties(ignoreUnknown=true) record Response(List<List<Double>> embeddings){}
}
