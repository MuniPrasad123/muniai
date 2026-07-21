# ADR-005: React frontend

- Status: Accepted
- Date: 2026-07-21

## Context

MuniAI needs an interactive local interface for streaming chat, sources, memory controls, and precise action previews and approvals.

## Decision

Use React for the future browser frontend, consuming a versioned backend API. Keep security policy and authorization in the backend; the UI presents controls but is not an enforcement boundary.

## Consequences

React supports a component-oriented interaction model and broad tooling. It introduces a separate build toolchain and requires careful state, accessibility, and output-encoding practices. Phase 0 adds no React code.

