package com.muniai.ai.infrastructure;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class OllamaWebClientConfiguration {
    @Bean
    WebClient ollamaWebClient(OllamaConfigurationProperties properties, WebClient.Builder builder) {
        Duration timeout = properties.timeout();
        HttpClient client = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(timeout.toMillis()))
                .responseTimeout(timeout);
        return builder.baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(client))
                .build();
    }
}
