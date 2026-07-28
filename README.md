# MuniAI

MuniAI is a private, local-first personal AI assistant. The project is intentionally bootstrapped as a modular monolith so capabilities can evolve behind clear boundaries while remaining simple to run on one trusted machine.

## Current scope

Phases 0 through 4 are verified. Phase 5 adds constrained local document storage and native text extraction. The browser, API, database, Ollama, and original files remain local. Chunking, embeddings, Qdrant, RAG, OCR, memory, agents, and connectors remain out of scope.

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

## Start here

Read [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md), [SECURITY.md](SECURITY.md), and the architecture decision records before implementation. Phase boundaries are deliberate: later-phase services must not be introduced early.

See [Chat request-to-response flow](docs/architecture/chat-request-response-flow.md) for current capabilities, limitations, and the complete browser-to-Ollama lifecycle.

Phase 4 guides: [PostgreSQL setup](docs/setup/POSTGRESQL_SETUP.md), [architecture](docs/architecture/phase-04-conversation-history.md), and [conversation API](docs/api/CONVERSATION_API.md).

Phase 5 guides: [document architecture](docs/architecture/phase-05-document-upload.md) and [document API](docs/api/DOCUMENT_API.md).

Phase 6 guides: [indexing and vector architecture](docs/architecture/phase-06-indexing-and-vectors.md), [Qdrant setup](docs/setup/QDRANT_SETUP.md), and [indexing API](docs/api/DOCUMENT_INDEXING_API.md). Phase 6 creates searchable vectors but does not connect documents to chat.
