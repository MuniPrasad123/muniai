# ADR-001: Modular monolith

- Status: Accepted
- Date: 2026-07-21

## Context

MuniAI spans chat, retrieval, memory, approvals, and connectors, but begins as a single-user local application. Independent services would add deployment, networking, consistency, and observability costs before those costs solve a measured problem.

## Decision

Build the backend as one deployable Spring Boot application with explicit internal modules and owned data boundaries. Communicate across modules through defined interfaces; do not share implementation details or directly access another module's tables.

## Consequences

Local deployment, transactions, testing, and debugging remain simpler. Module discipline is required to prevent a tightly coupled codebase. A module may be extracted later only when operational evidence justifies it.

