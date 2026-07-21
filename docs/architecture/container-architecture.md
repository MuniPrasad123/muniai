# Container architecture

This document describes intended logical deployment units. It is not a Docker or Kubernetes specification.

## Planned containers

| Container | Responsibility | Allowed dependencies |
|---|---|---|
| React frontend | Local user interface, previews, approval prompts | Backend API only |
| Spring Boot backend | Application modules, policy enforcement, orchestration, audit boundary | Local stores, Ollama, approved connector endpoints |
| Ollama | Local model inference and embeddings | Local model files |
| PostgreSQL | Structured conversations, metadata, approvals, audit records | Backend only |
| Qdrant | Vector indexes and document chunk references | Backend only |
| Redis | Optional ephemeral coordination and scheduling state | Backend only |
| Local file storage | Uploaded originals, derived artifacts, backups | Backend and explicit backup tooling |

## Modular backend boundaries

The backend begins as one deployable application with internal modules for chat, documents, retrieval, memory, approvals, audit, scheduling, and connectors. Modules expose explicit application interfaces and do not read one another's persistence tables directly.

## Network posture

All local listeners bind to loopback by default. Only the backend may call external connector endpoints, and only after connector enablement and scope checks. Databases and model endpoints are not exposed to the LAN or public internet.

## Evolution

Separate deployment is justified only by measured operational need. Module contracts should make later extraction possible without imposing distributed-system complexity now.

