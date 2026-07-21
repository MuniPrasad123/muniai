# Local deployment

## Intended topology

MuniAI is designed for a single trusted workstation. The future frontend, backend, model runtime, and stores run locally, with service ports bound to `127.0.0.1`. External access occurs only through explicitly configured OAuth connectors.

## Data locations

Runtime data belongs under `local-data/` or another user-selected protected directory outside version control. Deployment configuration must distinguish durable data from replaceable caches and document backup and restore behavior before persistence is enabled.

## Configuration

`.env.example` lists illustrative variable names. A local `.env` is ignored, must contain only machine-specific values, and must not be copied into diagnostics or commits. Production-quality secret storage will use operating-system facilities where available.

## Startup and health

Later phases should provide explicit start, stop, health, migration, backup, and restore procedures. Service startup order must not conceal failures, and health checks must distinguish process availability from dependency readiness.

## Exposure rules

- Do not publish database, Qdrant, Redis, or Ollama ports beyond loopback.
- Do not add remote access by default.
- If LAN access is later requested, require authentication, TLS, firewall rules, and a dedicated threat review.
- Containers must run without unnecessary privileges and use pinned, reviewed images when introduced.

## Phase 0 note

No Compose file, container configuration, service, database, or model connection exists yet. `infrastructure/` contains placeholders only.

