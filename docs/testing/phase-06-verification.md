# Phase 6 verification

Date: 2026-07-23

## Passed

- `mvn test`: 35 tests passed, 0 failures/errors.
- Flyway validated and applied V1, V2, and V3 in H2 PostgreSQL compatibility mode.
- Focused Phase 6 suite: 15 tests passed for chunking, embedding parsing/validation, safe Qdrant payload mapping, indexing, re-index idempotency, index deletion, failure state, and partial cleanup.
- Existing chat, conversation persistence, upload, PDF/TXT/Markdown extraction, and deletion regression tests passed.
- `npm.cmd test -- --run`: 16 frontend tests passed.
- Both TypeScript configurations and `npm.cmd run build` passed.
- `git diff --check` passed with informational Windows line-ending warnings only.
- A final focused indexed-document deletion test passed after the full suite, verifying Qdrant/chunk cleanup and retained-state consistency before physical/database deletion.

## Live verification not completed

Docker Desktop/Qdrant was not running (`docker_engine` pipe unavailable), ports 6333/6334 had no listeners, and `ollama` was not available on this shell PATH. Therefore no claim is made that a real document was embedded, stored, searched, re-indexed, or deleted against live Qdrant during this run.

To promote Phase 6 to `VERIFIED`:

1. Start Qdrant using `infrastructure/qdrant-compose.yml`.
2. Confirm `nomic-embed-text:latest` is installed and Ollama answers `/api/embed`.
3. Start the rebuilt backend so PostgreSQL receives Flyway V3.
4. Upload and index a synthetic document.
5. Inspect Qdrant point count and run diagnostic search.
6. Re-index and confirm point count is unchanged.
7. Delete the document and confirm its Qdrant filter count and PostgreSQL chunk count are zero.
8. Send/reload a normal chat message to reconfirm live Phase 2-5 behavior.
