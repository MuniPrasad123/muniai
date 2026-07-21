# ADR-002: Local Ollama inference

- Status: Accepted
- Date: 2026-07-21

## Context

Private prompts and documents should remain local by default. Cloud inference would introduce external data transfer, account dependencies, and provider retention concerns.

## Decision

Use a user-managed local Ollama runtime as the initial inference provider. Access it through a narrow backend adapter, bind it to loopback, and make model choice explicit and configurable.

## Consequences

Privacy and offline operation improve, while model quality, speed, and resource use depend on local hardware. The application must handle unavailable models and runtimes clearly. Phase 0 does not connect to Ollama.

