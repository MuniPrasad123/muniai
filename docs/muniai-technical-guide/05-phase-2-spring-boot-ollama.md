# Phase 2 — Spring Boot to Ollama

## Goal

Introduce a minimal backend API that accepts chat text and sends it to a local Ollama chat model.

## Key classes and files

- [backend/muniai-api/src/main/java/com/muniai/chat/api/ChatController.java](../../backend/muniai-api/src/main/java/com/muniai/chat/api/ChatController.java) — HTTP entry point.
- [backend/muniai-api/src/main/java/com/muniai/chat/application/ChatApplicationService.java](../../backend/muniai-api/src/main/java/com/muniai/chat/application/ChatApplicationService.java) — validation and orchestration.
- [backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaLanguageModelProvider.java](../../backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaLanguageModelProvider.java) — WebClient-based chat provider.
- [backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaWebClientConfiguration.java](../../backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaWebClientConfiguration.java) — client configuration.

## Request flow

HTTP request -> Controller -> Service -> Ollama provider -> Ollama API -> response mapping -> HTTP response.

## Important behavior

- The request DTO validates that the message is not blank.
- The service enforces the configured maximum message length.
- The provider sends a POST to /api/chat with a simple user message payload.
