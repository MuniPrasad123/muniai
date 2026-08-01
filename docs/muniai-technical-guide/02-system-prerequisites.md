# System prerequisites

## Operating system assumptions

The repository is written for a local Windows development environment, but the application code is cross-platform Java and Node code. The startup script in [scripts/start-muniai.ps1](../../scripts/start-muniai.ps1) is Windows-focused.

## Verified runtime versions and tools

- Java 21 from [backend/muniai-api/pom.xml](../../backend/muniai-api/pom.xml)
- Spring Boot 3.3.13 from [backend/muniai-api/pom.xml](../../backend/muniai-api/pom.xml)
- React 19.2.8 and Vite 8.1.5 from [frontend/muniai-web/package.json](../../frontend/muniai-web/package.json)
- PostgreSQL 17 is referenced in [IMPLEMENTATION_STATUS.md](../../IMPLEMENTATION_STATUS.md)
- Ollama CLI 0.32.1 is referenced in [IMPLEMENTATION_STATUS.md](../../IMPLEMENTATION_STATUS.md)
- Qdrant image qdrant/qdrant:v1.15.4 from [infrastructure/qdrant-compose.yml](../../infrastructure/qdrant-compose.yml)

## Required services and ports

- PostgreSQL: 127.0.0.1:5432
- Ollama: 127.0.0.1:11434
- Qdrant: 127.0.0.1:6333 and 127.0.0.1:6334
- Backend: 127.0.0.1:8080
- Frontend: 127.0.0.1:5173

## Required models

- Chat model: llama3.2:3b
- Embedding model: nomic-embed-text:latest
