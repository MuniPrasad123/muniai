package com.muniai.chat.api;
import com.muniai.ai.domain.ChatCompletion;
import com.muniai.chat.application.ChatApplicationService;
import com.muniai.observability.CorrelationId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/chat")
public class ChatController {
 private final ChatApplicationService service;
 public ChatController(ChatApplicationService service) { this.service = service; }
 @PostMapping public ResponseEntity<ChatResponseDto> chat(@Valid @RequestBody ChatRequestDto request) {
  ChatCompletion value = service.chat(request.message());
  return ResponseEntity.ok(new ChatResponseDto(value.answer(), value.model(), value.provider(), CorrelationId.current()));
 }
}
