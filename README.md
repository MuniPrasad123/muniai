# MuniAI

MuniAI is a private, local-first personal AI assistant. The project is intentionally bootstrapped as a modular monolith so capabilities can evolve behind clear boundaries while remaining simple to run on one trusted machine.

## Current scope

Only Phase 0 is present: repository structure, architecture documentation, decision records, security guidance, and environment verification. There is no application code, runtime stack, connector, or AI integration yet.

## Repository layout

- `frontend/` — future React client
- `backend/` — future Spring Boot application
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

