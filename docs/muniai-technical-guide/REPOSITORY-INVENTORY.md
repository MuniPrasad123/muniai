# Repository inventory

## Top-level modules

- [backend/muniai-api](../../backend/muniai-api) — Spring Boot 3.3.13 application with Java 21, PostgreSQL, Flyway, PDFBox, WebClient, and Spring Data JPA.
- [frontend/muniai-web](../../frontend/muniai-web) — Vite + React + TypeScript frontend using the browser fetch API.
- [infrastructure](../../infrastructure) — Docker Compose for Qdrant and other local runtime assets.
- [scripts](../../scripts) — PowerShell startup and environment verification scripts.
- [docs](../../docs) — architecture and API documentation.

## Main application entry points

- [backend/muniai-api/src/main/java/com/muniai/bootstrap/MuniAiApplication.java](../../backend/muniai-api/src/main/java/com/muniai/bootstrap/MuniAiApplication.java) — Spring Boot entry point.
- [frontend/muniai-web/src/main.tsx](../../frontend/muniai-web/src/main.tsx) — React bootstrapping entry point.
- [scripts/start-muniai.ps1](../../scripts/start-muniai.ps1) — developer startup script for Windows.
- [scripts/verification/verify-environment.ps1](../../scripts/verification/verify-environment.ps1) — environment verifier.

## Configuration files

- [backend/muniai-api/pom.xml](../../backend/muniai-api/pom.xml)
- [backend/muniai-api/src/main/resources/application.yml](../../backend/muniai-api/src/main/resources/application.yml)
- [frontend/muniai-web/package.json](../../frontend/muniai-web/package.json)
- [frontend/muniai-web/vite.config.ts](../../frontend/muniai-web/vite.config.ts)
- [infrastructure/qdrant-compose.yml](../../infrastructure/qdrant-compose.yml)

## Actual versions and services discovered

- Java: 21 from [backend/muniai-api/pom.xml](../../backend/muniai-api/pom.xml)
- Spring Boot: 3.3.13 from [backend/muniai-api/pom.xml](../../backend/muniai-api/pom.xml)
- React: 19.2.8 and React DOM 19.2.8 from [frontend/muniai-web/package.json](../../frontend/muniai-web/package.json)
- Vite: 8.1.5 from [frontend/muniai-web/package.json](../../frontend/muniai-web/package.json)
- PDFBox: 3.0.5 from [backend/muniai-api/pom.xml](../../backend/muniai-api/pom.xml)
- Qdrant image: qdrant/qdrant:v1.15.4 in [infrastructure/qdrant-compose.yml](../../infrastructure/qdrant-compose.yml)
- Ollama chat model: llama3.2:3b from [backend/muniai-api/src/main/resources/application.yml](../../backend/muniai-api/src/main/resources/application.yml)
- Ollama embedding model: nomic-embed-text:latest from [backend/muniai-api/src/main/resources/application.yml](../../backend/muniai-api/src/main/resources/application.yml)
- Backend port: 8080 from [backend/muniai-api/src/main/resources/application.yml](../../backend/muniai-api/src/main/resources/application.yml)
- Frontend port: 5173 from [frontend/muniai-web/vite.config.ts](../../frontend/muniai-web/vite.config.ts)
- PostgreSQL default URL: jdbc:postgresql://127.0.0.1:5432/muniai from [backend/muniai-api/src/main/resources/application.yml](../../backend/muniai-api/src/main/resources/application.yml)
- Qdrant HTTP port: 6333 and gRPC 6334 from [infrastructure/qdrant-compose.yml](../../infrastructure/qdrant-compose.yml)
- Ollama API base URL: http://127.0.0.1:11434 from [backend/muniai-api/src/main/resources/application.yml](../../backend/muniai-api/src/main/resources/application.yml)
