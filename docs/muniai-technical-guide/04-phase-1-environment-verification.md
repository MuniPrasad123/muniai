# Phase 1 — environment verification

## Goal

Verify that the local prerequisites for chat, PostgreSQL, Ollama, Qdrant, Java, Maven, Node, npm, and Docker are all present before the application is started.

## What was built

The repository includes a read-only environment verifier at [scripts/verification/verify-environment.ps1](../../scripts/verification/verify-environment.ps1) and a developer startup script at [scripts/start-muniai.ps1](../../scripts/start-muniai.ps1).

## What the verifier checks

- Java, Maven, Node.js, npm, Git, and Docker availability
- Ollama CLI availability
- The Ollama API on http://127.0.0.1:11434/api/tags
- The presence of llama3.2:3b and nomic-embed-text:latest
