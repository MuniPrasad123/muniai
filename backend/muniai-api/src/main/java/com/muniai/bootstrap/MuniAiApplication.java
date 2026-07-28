package com.muniai.bootstrap;

import com.muniai.ai.infrastructure.OllamaConfigurationProperties;
import com.muniai.chat.application.ChatConfigurationProperties;
import com.muniai.document.application.DocumentConfigurationProperties;
import com.muniai.document.application.IndexingConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.muniai")
@EnableConfigurationProperties({OllamaConfigurationProperties.class, ChatConfigurationProperties.class,
        DocumentConfigurationProperties.class, IndexingConfigurationProperties.class})
@EntityScan("com.muniai")
@EnableJpaRepositories("com.muniai")
public class MuniAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MuniAiApplication.class, args);
    }
}
