package com.muniai.observability;
import com.muniai.ai.application.LanguageModelProvider; import com.muniai.ai.domain.ProviderHealth; import java.time.Instant; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/system") public class SystemHealthController {
 private final LanguageModelProvider provider; public SystemHealthController(LanguageModelProvider provider){this.provider=provider;}
 @GetMapping("/health") public SystemHealthResponse health(){ProviderHealth h=provider.health();return new SystemHealthResponse("UP",h.status().name(),h.model(),Instant.now());}
 public record SystemHealthResponse(String applicationStatus,String ollamaStatus,String model,Instant timestamp){}
}
