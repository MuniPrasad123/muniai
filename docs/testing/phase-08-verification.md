# Phase 8 verification

Date: 2026-11-19 (updated from the 2026-08-02 implementation pass)

Final status: `IMPLEMENTED`

## Scope

Phase 8 adds opt-in, approval-gated long-term memory backed by PostgreSQL. It keeps personal memory separate from conversation history and Qdrant document knowledge, uses deterministic selection, and integrates approved memory with Normal Chat and Ask Documents without adding Phase 9 behavior.

## Features checked

- Flyway V5 creates memory lifecycle and per-message usage-snapshot tables with indexes and foreign keys.
- The memory API supports proposal, review, edit, approval, rejection, disable, re-enable, supersede, individual deletion, and delete-all.
- Singular-key conflicts require explicit superseding; multi-valued keys retain independent active values.
- A deterministic policy rejects common secret and highly sensitive value patterns before a proposal is persisted.
- Explicit remember, recall, and forget/disable commands are handled without relying on model output.
- Only active, non-expired, relevant memories are selected within configured item and character limits.
- Normal Chat and RAG prompts isolate approved memory as untrusted reference data.
- Successful personalized assistant messages persist non-sensitive memory usage snapshots and update usage counters.
- The frontend provides Memory navigation, active/proposed/disabled views, filtering, search, manual proposals, lifecycle actions, conflict resolution, proposal cards, and expandable used-memory disclosure.

## Commands executed

```powershell
cd backend/muniai-api
mvn "-Dtest=MemoryApiIntegrationTest,MemoryPromptFormatterTest,ConversationApplicationServiceTest" test
mvn test

cd ../../frontend/muniai-web
npm.cmd test -- --run
npm.cmd test -- --run --maxWorkers=1
npm.cmd run build

powershell -ExecutionPolicy Bypass -File .\scripts\start-muniai.ps1
curl.exe -X POST -F "file=@sample-data/muniai-demo-knowledge.md;type=text/markdown" http://127.0.0.1:8080/api/v1/documents
curl.exe -X POST http://127.0.0.1:8080/api/v1/documents/{documentId}/index
```

## Automated test results

- Focused backend verification: 8 tests passed with 0 failures or errors. The run applied Flyway V1 through V5 to H2.
- The memory integration tests cover proposal/lifecycle operations, sensitive-data rejection, singular conflict and superseding, and approval in one conversation followed by use in another conversation.
- Conversation application tests cover successful and failed generation behavior while preserving existing chat routing.
- Prompt tests verify isolation of malicious memory content as untrusted data.
- Frontend: 24 tests passed across 3 files with 0 failures.
- Both TypeScript project configurations and the Vite production build passed; 22 modules were transformed.
- Full backend suite: 57 tests passed across 16 report files with 0 failures, errors, or skips.
- The 2026-11-19 backend rerun again passed 57 tests with 0 failures, errors, or skips and validated Flyway V1-V5.
- The 2026-11-19 frontend suite passed 24/24 when run with one worker. Concurrent runs during the CPU-heavy backend suite timed out; no assertion failure remained in the serialized rerun.
- The 2026-11-19 production build passed TypeScript checks and transformed 22 Vite modules.
- The broad run includes existing Normal Chat, conversation persistence, document upload, Qdrant indexing client, retrieval, RAG prompt/citation, and Flyway regression coverage. A passing automated run is not treated as a substitute for live-service verification.

## Manual test scenarios

Native acceptance checklist and current state:

1. **Passed 2026-11-19:** create an explicit `preferred_explanation_style` proposal in conversation A, approve it, and use it in a separate conversation with inspectable usage metadata.
2. **Automated only:** conflicting singular memory and supersede history.
3. **Automated only:** password/token rejection with no proposal persistence.
4. **Pending native check:** disable and delete a memory, start new conversations, and confirm neither state is selected or disclosed as used.
5. **Passed 2026-11-19:** Ask Documents used the approved explanation-style memory, returned a grounded local answer, preserved two citations, and displayed both evidence and memory-use disclosures.
6. **Pending native check:** restart the application and confirm approved memory and usage disclosure persist.
7. **Pending native check:** make memory retrieval unavailable and confirm chat safely continues without personalization and returns a warning.

## Results and evidence

- `MemoryApiIntegrationTest` demonstrates an approval-gated memory created through conversation A and reused through conversation B. It asserts the approved-memory prompt boundary and the post-success usage count.
- Lifecycle API verification demonstrates edit, approve, disable, enable, delete, conflict detection, and explicit superseding.
- Sensitive-value verification asserts a safe rejection and an empty proposal list.
- The migration succeeds in the automated application context and creates both Phase 8 tables.
- Existing conversation tests and the complete frontend regression suite pass with memory enabled.
- The UI tests cover list states, manual creation, approval, edit/reject, disable/enable, conflict confirmation, deletion, delete-all, search/filter, empty state, and safe errors.
- Live PostgreSQL/Ollama/Qdrant/browser evidence on 2026-11-19 used the original synthetic `sample-data/muniai-demo-knowledge.md` document. It indexed into two 768-dimensional chunks, semantic search returned two matches, and the RAG response persisted two citations plus one used-memory snapshot.
- The browser displayed `Personalized using 1 approved memory`, the expanded `Preferred Explanation Style` metadata, two retrieved passages with similarity scores, and the active memory record with source and usage count.
- The recorded evidence is in `docs/demo/muniai-demo.mp4`, `docs/demo/muniai-demo.vtt`, and `docs/demo/CASE-STUDY.md`.

## Security verification

- Memory remains `PROPOSED` until an explicit approval action.
- Secret-pattern rejection is deterministic and occurs before proposal persistence.
- Only `ACTIVE`, non-expired records can be selected.
- Prompt instructions and delimiters label memory values as untrusted reference data; values cannot authorize actions or override system policy.
- Usage metadata discloses IDs, categories, keys, and labels, but not normalized values, versions, or hidden prompts.
- Usage counters are updated only after a successful assistant response.
- No memory embeddings, vectors, automatic profiling, background extraction, or external integrations were introduced.

## Known limitations

- Selection is deterministic and keyword-based; there is no semantic memory search or embedding index.
- Explicit command parsing intentionally supports clear, bounded phrasing rather than arbitrary natural-language extraction.
- Soft-deleted records remain as auditable `DELETED` rows; a future retention policy may define physical purge behavior.
- The original persisted `muniai_document_chunks` collection panicked during filtered deletion on 2026-11-19 (`OutputTooSmall`, then `channel closed`). No original vectors were deleted. Live demo verification used a fresh isolated `muniai_demo_20261119` collection; recovery/rebuild of the original collection remains operational work.
- Native disabled/deleted exclusion, restart persistence, and memory-retrieval failure fallback remain unrecorded.

## Final phase status

`IMPLEMENTED`. The Phase 8 code, migration, full backend suite, frontend suite, type checks, and production build pass. A native cross-conversation RAG-with-memory scenario, citation display, evidence display, and memory-use disclosure are now recorded. `VERIFIED` is intentionally withheld until disabled/deleted exclusion, restart persistence, failure fallback, and the original Qdrant collection recovery are completed and recorded.
