package com.muniai.document.api;

import com.muniai.document.application.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vector-search")
public class VectorSearchController {
    private final DocumentIndexingService indexing;
    public VectorSearchController(DocumentIndexingService indexing){this.indexing=indexing;}
    @PostMapping("/test")
    public SearchResponse search(@Valid @RequestBody SearchRequest request){
        var hits=indexing.search(request.query(),request.limit()==null?5:request.limit(),request.documentId());
        return new SearchResponse(hits.stream().map(hit->new SearchResult(hit.chunkId(),hit.documentId(),hit.chunkIndex(),
                hit.score(),preview(hit.content()),hit.originalFileName())).toList(),
                "Phase 6 diagnostic only. Results are not sent to the chat model.");
    }
    private String preview(String content){return content.length()<=300?content:content.substring(0,300)+"…";}
    public record SearchRequest(@NotBlank @Size(max=2000) String query,@Min(1) @Max(20) Integer limit,UUID documentId){}
    public record SearchResponse(List<SearchResult> results,String notice){}
    public record SearchResult(UUID chunkId,UUID documentId,int chunkIndex,double score,String contentPreview,String originalFileName){}
}
