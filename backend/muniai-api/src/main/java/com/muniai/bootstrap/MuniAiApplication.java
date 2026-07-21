package com.muniai.bootstrap;

import com.muniai.ai.infrastructure.OllamaConfigurationProperties;
import com.muniai.chat.application.ChatConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.muniai")
@EnableConfigurationProperties({OllamaConfigurationProperties.class, ChatConfigurationProperties.class})
public class MuniAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MuniAiApplication.class, args);
    }
}
