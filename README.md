# MuniAI

MuniAI is a private, local-first personal AI assistant. The project is intentionally bootstrapped as a modular monolith so capabilities can evolve behind clear boundaries while remaining simple to run on one trusted machine.

## Current scope

Phases 0 through 3 are verified. The repository includes a loopback-only Spring Boot API and an ephemeral React chat interface for local Ollama. No persistence, retrieval, memory, agent, or connector capability is present yet.

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
