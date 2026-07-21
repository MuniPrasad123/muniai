package com.muniai.observability;
import com.muniai.ai.application.LanguageModelProvider; import com.muniai.ai.domain.ProviderHealth;
import org.springframework.boot.actuate.health.*; import org.springframework.stereotype.Component;
@Component("ollama") public class OllamaHealthIndicator implements HealthIndicator {
 private final LanguageModelProvider provider; public OllamaHealthIndicator(LanguageModelProvider provider){this.provider=provider;}
 public Health health(){ProviderHealth h=provider.health(); Health.Builder b=h.status()==ProviderHealth.Status.UP?Health.up():Health.down(); return b.withDetail("provider",h.provider()).withDetail("model",h.model()).build();}
}
