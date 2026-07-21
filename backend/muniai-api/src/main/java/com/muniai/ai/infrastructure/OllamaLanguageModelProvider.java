package com.muniai.ai.infrastructure;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.ChatCompletion;
import com.muniai.ai.domain.ChatCompletionRequest;
import com.muniai.ai.domain.ProviderHealth;
import com.muniai.shared.exception.AiProviderTimeoutException;
import com.muniai.shared.exception.AiProviderUnavailableException;
import com.muniai.shared.exception.MalformedProviderResponseException;
import com.muniai.shared.exception.MissingModelException;
import java.util.concurrent.TimeoutException;
import java.util.List;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.Exceptions;

@Component
public class OllamaLanguageModelProvider implements LanguageModelProvider {
    private static final String PROVIDER = "ollama";
    private final WebClient webClient;
    private final OllamaConfigurationProperties properties;

    public OllamaLanguageModelProvider(WebClient ollamaWebClient, OllamaConfigurationProperties properties) {
        this.webClient = ollamaWebClient;
        this.properties = properties;
    }

    @Override
    public ChatCompletion complete(ChatCompletionRequest request) {
        String model = requiredModel();
        try {
            OllamaChatResponse response = webClient.post()
                    .uri("/api/chat")
                    .bodyValue(new OllamaChatRequest(model, List.of(new OllamaMessage("user", request.message())), false))
                    .retrieve()
                    .bodyToMono(OllamaChatResponse.class)
                    .timeout(properties.timeout())
                    .block();
            if (response == null || response.message() == null || response.message().content() == null
                    || response.message().content().isBlank()) {
                throw new MalformedProviderResponseException();
            }
            return new ChatCompletion(response.message().content(),
                    response.model() == null || response.model().isBlank() ? model : response.model(), PROVIDER);
        } catch (MalformedProviderResponseException exception) {
            throw exception;
        } catch (DecodingException exception) {
            throw new MalformedProviderResponseException(exception);
        } catch (RuntimeException exception) {
            Throwable cause = Exceptions.unwrap(exception);
            if (cause instanceof TimeoutException || hasCause(exception, TimeoutException.class)) {
                throw new AiProviderTimeoutException(exception);
            }
            if (exception instanceof WebClientException || hasCause(exception, WebClientException.class)) {
                throw new AiProviderUnavailableException(exception);
            }
            throw exception;
        }
    }

    @Override
    public ProviderHealth health() {
        String model = properties.model();
        if (model == null || model.isBlank()) {
            return new ProviderHealth(ProviderHealth.Status.DOWN, PROVIDER, model);
        }
        try {
            OllamaTagsResponse response = webClient.get().uri("/api/tags").retrieve()
                    .bodyToMono(OllamaTagsResponse.class).timeout(properties.timeout()).block();
            boolean present = response != null && response.models() != null
                    && response.models().stream().anyMatch(item -> model.equals(item.name()));
            return new ProviderHealth(present ? ProviderHealth.Status.UP : ProviderHealth.Status.DOWN, PROVIDER, model);
        } catch (RuntimeException exception) {
            return new ProviderHealth(ProviderHealth.Status.DOWN, PROVIDER, model);
        }
    }

    private String requiredModel() {
        if (properties.model() == null || properties.model().isBlank()) {
            throw new MissingModelException();
        }
        return properties.model();
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) return true;
            current = current.getCause();
        }
        return false;
    }

    record OllamaChatRequest(String model, List<OllamaMessage> messages, boolean stream) {}
    record OllamaMessage(String role, String content) {}
    record OllamaChatResponse(String model, OllamaMessage message) {}
    record OllamaTagsResponse(List<OllamaModel> models) {}
    record OllamaModel(String name) {}
}
