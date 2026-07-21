# ADR-004: Qdrant for vector search

- Status: Accepted
- Date: 2026-07-21

## Context

Retrieval-augmented chat will need similarity search over locally generated embeddings, plus metadata filters and source traceability.

## Decision

Use local Qdrant as the future vector store. PostgreSQL remains authoritative for source metadata; Qdrant entries reference stable source and authorization identifiers and can be rebuilt.

## Consequences

Purpose-built vector search is available without placing authoritative records in the index. Another local service must be secured, backed up as appropriate, and kept consistent. Phase 0 adds no Qdrant service.

