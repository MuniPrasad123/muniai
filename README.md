# MuniAI

MuniAI is a private, local-first personal AI assistant. The project is intentionally bootstrapped as a modular monolith so capabilities can evolve behind clear boundaries while remaining simple to run on one trusted machine.

## Current scope

MuniAI includes local conversation persistence, constrained document upload/extraction, Ollama embeddings, Qdrant indexing, semantic diagnostics, and explicit document-grounded chat with persisted citations. The browser, API, database, Ollama, Qdrant, and original files remain local. OCR, long-term memory, agents, tools, web search, and connectors remain out of scope.

## Repository layout

- `frontend/muniai-web/` — React chat client with in-memory state only
- `backend/muniai-api/` — Spring Boot API for local Ollama chat
- `infrastructure/` — future local service and deployment configuration
- `docs/architecture/` — system design and security boundaries
- `docs/adr/` — architecture decision records
- `scripts/` — setup, verification, and backup automation
- `sample-data/` — safe, synthetic development inputs
- `local-data/` — ignored private runtime data

## Environment check

From PowerShell, run:

```powershell
./scripts/verification/verify-environment.ps1
```

The script performs read-only availability and version checks. It does not install or configure anything.

## Start after restarting Windows

Start and health-check the existing local services and application with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-muniai.ps1
```

See [MuniAI Windows startup script](docs/setup/STARTUP_SCRIPT.md) for options
and one-time prerequisites.

## Start here

Read [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md), [SECURITY.md](SECURITY.md), and the architecture decision records before implementation. Phase boundaries are deliberate: later-phase services must not be introduced early.

See [Chat request-to-response flow](docs/architecture/chat-request-response-flow.md) for current capabilities, limitations, and the complete browser-to-Ollama lifecycle.

Phase 4 guides: [PostgreSQL setup](docs/setup/POSTGRESQL_SETUP.md), [architecture](docs/architecture/phase-04-conversation-history.md), and [conversation API](docs/api/CONVERSATION_API.md).

Phase 5 guides: [document architecture](docs/architecture/phase-05-document-upload.md) and [document API](docs/api/DOCUMENT_API.md).

Phase 6 guides: [indexing and vector architecture](docs/architecture/phase-06-indexing-and-vectors.md), [Qdrant setup](docs/setup/QDRANT_SETUP.md), and [indexing API](docs/api/DOCUMENT_INDEXING_API.md). Phase 6 creates searchable vectors but does not connect documents to chat.

Phase 7 guides: [RAG architecture](docs/architecture/phase-07-rag-chat.md) and [RAG chat API](docs/api/RAG_CHAT_API.md).
