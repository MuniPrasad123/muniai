package com.muniai.ai.application;

import com.muniai.ai.domain.ChatCompletion;
import com.muniai.ai.domain.ChatCompletionRequest;
import com.muniai.ai.domain.ProviderHealth;

public interface LanguageModelProvider {
    ChatCompletion complete(ChatCompletionRequest request);
    ProviderHealth health();
}
