package com.muniai.conversation.application;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.ChatCompletion;
import com.muniai.ai.domain.ChatCompletionRequest;
import com.muniai.conversation.domain.ConversationMessage;
import com.muniai.conversation.domain.MessageCitation;
import com.muniai.document.application.DocumentIndexingService;
import com.muniai.document.application.VectorStore;
import com.muniai.shared.exception.DocumentException;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class RagService {
    public static final String NO_CONTEXT_ANSWER =
            "I could not find enough relevant information in the selected documents.";
    private final DocumentIndexingService indexing;
    private final LanguageModelProvider provider;
    private final RagConfigurationProperties properties;
    private final RagPromptBuilder prompts = new RagPromptBuilder();
    private final CitationAligner citationAligner = new CitationAligner();

    public RagService(DocumentIndexingService indexing, LanguageModelProvider provider,
                      RagConfigurationProperties properties) {
        this.indexing=indexing; this.provider=provider; this.properties=properties;
    }

    public RagResult answer(String question, Set<UUID> documentIds, Integer requestedTopK,
                            Double requestedThreshold, List<ConversationMessage> history) {
        int topK=requestedTopK==null?properties.topK():requestedTopK;
        if(topK<1||topK>properties.maximumTopK())
            throw new DocumentException("INVALID_RAG_TOP_K",
                    "The RAG result limit must be between 1 and "+properties.maximumTopK()+".");
        double threshold=requestedThreshold==null?properties.similarityThreshold():requestedThreshold;
        if(threshold<0||threshold>1)
            throw new DocumentException("INVALID_RAG_THRESHOLD","The RAG similarity threshold must be between 0 and 1.");

        List<VectorStore.SearchHit> retrieved=indexing.retrieve(question,topK,documentIds);
        List<VectorStore.SearchHit> filtered=retrieved.stream()
                .filter(hit->hit.score()>=threshold)
                .sorted(Comparator.comparingDouble(VectorStore.SearchHit::score).reversed())
                .filter(distinctByChunk())
                .limit(Math.min(topK,properties.maxContextChunks()))
                .toList();
        if(filtered.isEmpty()) return new RagResult(NO_CONTEXT_ANSWER,List.of(),null);

        RagPromptBuilder.RagPrompt prompt=prompts.build(question,history,filtered,properties.maxContextCharacters());
        ChatCompletion completion=provider.complete(new ChatCompletionRequest(prompt.prompt(),properties.chatModel(),
                properties.temperature(),properties.requestTimeout()));
        CitationAligner.Alignment alignment=citationAligner.align(completion.answer(),prompt.includedHits());
        List<MessageCitation> citations=new ArrayList<>();
        int index=1;
        for(VectorStore.SearchHit hit:prompt.includedHits()){
            citations.add(new MessageCitation(UUID.randomUUID(),index++,hit.documentId(),hit.originalFileName(),
                    hit.chunkId(),hit.chunkIndex(),null,hit.score(),preview(hit.content()),Instant.now()));
        }
        return new RagResult(alignment.answer(),List.copyOf(citations),completion);
    }

    private java.util.function.Predicate<VectorStore.SearchHit> distinctByChunk(){
        Set<UUID> seen=new HashSet<>();
        return hit->seen.add(hit.chunkId());
    }
    private String preview(String value){
        return RagPromptBuilder.truncate(value.replaceAll("\\s+"," ").trim(),400);
    }
    public record RagResult(String answer,List<MessageCitation> citations,ChatCompletion completion){
        public boolean noRelevantContext(){return completion==null;}
    }
}
