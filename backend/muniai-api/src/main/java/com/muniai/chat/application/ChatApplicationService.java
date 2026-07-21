package com.muniai.chat.application;
import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.ChatCompletion;
import com.muniai.ai.domain.ChatCompletionRequest;
import com.muniai.shared.exception.MessageTooLongException;
import org.springframework.stereotype.Service;
@Service
public class ChatApplicationService {
    private final LanguageModelProvider provider;
    private final ChatConfigurationProperties properties;
    public ChatApplicationService(LanguageModelProvider provider, ChatConfigurationProperties properties) {
        this.provider = provider; this.properties = properties;
    }
    public ChatCompletion chat(String message) {
        if (message.length() > properties.maxMessageLength()) throw new MessageTooLongException(properties.maxMessageLength());
        return provider.complete(new ChatCompletionRequest(message));
    }
}
