package com.muniai.document.api;

import com.muniai.document.application.DocumentApplicationService;
import com.muniai.document.infrastructure.DocumentEntity;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentApplicationService service;
    public DocumentController(DocumentApplicationService service) { this.service=service; }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentDtos.DocumentResponse> upload(@RequestPart("file") MultipartFile file) {
        DocumentEntity created = service.upload(file);
        return ResponseEntity.created(URI.create("/api/v1/documents/" + created.getId())).body(map(created));
    }
    @GetMapping public List<DocumentDtos.DocumentResponse> list() { return service.list().stream().map(this::map).toList(); }
    @GetMapping("/{id}") public DocumentDtos.DocumentResponse get(@PathVariable UUID id) { return map(service.get(id)); }
    @GetMapping("/{id}/text") public DocumentDtos.DocumentTextResponse text(@PathVariable UUID id) {
        return new DocumentDtos.DocumentTextResponse(id, service.text(id));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
    private DocumentDtos.DocumentResponse map(DocumentEntity value) {
        return new DocumentDtos.DocumentResponse(value.getId(), value.getOriginalFileName(), value.getContentType(),
                value.getFileSize(), value.getExtractionStatus(), value.getExtractionError(), value.getPageCount(),
                service.fileAvailable(value), value.getCreatedAt(), value.getUpdatedAt());
    }
}
