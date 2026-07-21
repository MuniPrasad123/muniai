# Phase 2: Spring Boot to local Ollama

The application is a modular monolith with a provider-neutral `LanguageModelProvider` boundary. Chat orchestration depends on this interface; the Ollama adapter owns local HTTP mapping, and provider payloads never cross the public API.

The flow is controller validation, application-service bounds checking, provider invocation, and neutral response mapping. Dependencies use constructor injection. Generated requests are not retried.

Input failures return 400, provider connectivity failures 503, timeouts 504, malformed responses 502, and missing model configuration 503. A global handler returns stable errors without stack traces.

Server and provider defaults are loopback-only. Prompts and responses are not logged. Correlation IDs appear in the response header, API bodies, and logging context. WireMock makes automated tests independent of Ollama; manual verification exercises the packaged jar against the real local model.
