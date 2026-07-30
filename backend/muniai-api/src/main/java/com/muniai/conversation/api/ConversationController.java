package com.muniai.conversation.api;

import static com.muniai.conversation.api.ConversationDtos.*;

import com.muniai.conversation.application.ConversationApplicationService;
import com.muniai.conversation.domain.*;
import com.muniai.observability.CorrelationId;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {
    private final ConversationApplicationService service;

    public ConversationController(ConversationApplicationService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ConversationResponse> create() {
        Conversation created = service.create();
        return ResponseEntity.created(URI.create("/api/v1/conversations/" + created.id())).body(map(created));
    }

    @GetMapping
    public List<ConversationResponse> list() { return service.list().stream().map(this::map).toList(); }

    @GetMapping("/{id}")
    public ConversationResponse get(@PathVariable UUID id) { return map(service.get(id)); }

    @PatchMapping("/{id}")
    public ConversationResponse rename(@PathVariable UUID id, @Valid @RequestBody RenameRequest request) {
        return map(service.rename(id, request.title()));
    }

    @PostMapping("/{id}/messages")
    public SendMessageResponse send(@PathVariable UUID id, @Valid @RequestBody SendMessageRequest request) {
        ConversationApplicationService.SendMessageResult result = service.send(id, request.message(),
                request.effectiveMode(),request.selectedDocuments(),request.topK(),request.similarityThreshold());
        return new SendMessageResponse(result.conversationId(), map(result.userMessage()), map(result.assistantMessage()),
                result.provider(), CorrelationId.current(), result.noRelevantContext());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }

    private ConversationResponse map(Conversation value) {
        return new ConversationResponse(value.id(), value.title(), value.createdAt(), value.updatedAt(),
                value.messages().stream().map(this::map).toList());
    }

    private MessageResponse map(ConversationMessage value) {
        return new MessageResponse(value.id(), value.conversationId(), value.role(), value.content(), value.model(),
                value.mode(),value.citations().stream().map(citation->new CitationResponse(citation.id(),
                        citation.citationIndex(),citation.documentId(),citation.originalFileName(),citation.chunkId(),
                        citation.chunkIndex(),citation.pageNumber(),citation.similarityScore(),citation.contentPreview())).toList(),
                value.createdAt());
    }
}
