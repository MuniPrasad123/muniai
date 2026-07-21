# ADR-003: PostgreSQL for structured persistence

- Status: Accepted
- Date: 2026-07-21

## Context

Conversations, document metadata, approvals, audit records, schedules, and connector metadata need durable transactions, constraints, migrations, and reliable backup tooling.

## Decision

Use PostgreSQL as the future system of record for structured application data. Each backend module owns its schema boundary, and all changes use reviewed migrations.

## Consequences

The project gains mature relational guarantees and tooling at the cost of operating a local database. Sensitive fields, retention, deletion, backup, and restore must be designed before data is stored. Phase 0 adds no database.

