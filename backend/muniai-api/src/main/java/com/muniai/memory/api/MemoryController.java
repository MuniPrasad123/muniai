package com.muniai.memory.api;

import static com.muniai.memory.api.MemoryDtos.*;
import com.muniai.memory.application.MemoryApplicationService;
import com.muniai.memory.domain.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/memories")
public class MemoryController {
    private final MemoryApplicationService service;
    public MemoryController(MemoryApplicationService service){this.service=service;}
    @PostMapping("/proposals") public ResponseEntity<MemoryResponse> propose(@Valid @RequestBody ProposalRequest request){
        UserMemory created=service.propose(request.category(),request.key(),request.value(),request.displayLabel(),
                request.sourceType()==null?MemorySourceType.MANUAL_ENTRY:request.sourceType(),request.sourceConversationId(),
                request.sourceMessageId(),request.confidence(),request.expiresAt());
        return ResponseEntity.created(URI.create("/api/v1/memories/"+created.id())).body(map(created));}
    @GetMapping public List<MemoryResponse> list(@RequestParam(required=false) MemoryStatus status){return (status==null?service.list():service.list(status)).stream().map(this::map).toList();}
    @GetMapping("/active") public List<MemoryResponse> active(){return service.active().stream().map(this::map).toList();}
    @GetMapping("/proposed") public List<MemoryResponse> proposed(){return service.list(MemoryStatus.PROPOSED).stream().map(this::map).toList();}
    @GetMapping("/disabled") public List<MemoryResponse> disabled(){return service.list(MemoryStatus.DISABLED).stream().map(this::map).toList();}
    @GetMapping("/{id}") public MemoryResponse get(@PathVariable UUID id){return map(service.get(id));}
    @PatchMapping("/{id}") public MemoryResponse edit(@PathVariable UUID id,@Valid @RequestBody UpdateRequest request){return map(service.edit(id,request.category(),request.key(),request.value(),request.displayLabel(),request.expiresAt()));}
    @PostMapping("/{id}/approve") public MemoryResponse approve(@PathVariable UUID id){return map(service.approve(id));}
    @PostMapping("/{id}/reject") public MemoryResponse reject(@PathVariable UUID id){return map(service.reject(id));}
    @PostMapping("/{id}/disable") public MemoryResponse disable(@PathVariable UUID id){return map(service.disable(id));}
    @PostMapping("/{id}/enable") public MemoryResponse enable(@PathVariable UUID id){return map(service.enable(id));}
    @PostMapping("/{id}/supersede") public MemoryResponse supersede(@PathVariable UUID id,@Valid @RequestBody SupersedeRequest request){return map(service.supersede(id,request.replacementMemoryId()));}
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id){service.delete(id);return ResponseEntity.noContent().build();}
    @DeleteMapping public ResponseEntity<Void> deleteAll(){service.deleteAll();return ResponseEntity.noContent().build();}
    private MemoryResponse map(UserMemory m){return new MemoryResponse(m.id(),m.category(),m.key(),m.value(),m.displayLabel(),m.status(),m.sourceType(),m.sourceConversationId(),m.sourceMessageId(),m.approvedAt(),m.expiresAt(),m.supersededByMemoryId(),m.lastUsedAt(),m.usageCount(),m.createdAt(),m.updatedAt());}
}
