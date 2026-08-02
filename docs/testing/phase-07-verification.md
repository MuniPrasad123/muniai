# Phase 7 verification

Date: 2026-08-02

Final status: `VERIFIED`

## Scope

Phase 7 preserves Normal Chat and adds explicit Ask Documents mode, selected/all-document retrieval, bounded grounded prompts, no-context handling, deterministic citation alignment, citation persistence, and frontend retrieved-evidence display. Long-term memory is not included.

## Features checked

- `ConversationApplicationService` routes `NORMAL` and `DOCUMENT_RAG` without silently enabling retrieval.
- `RagService`, `DocumentRetrievalService`, `RagPromptBuilder`, and `CitationAligner` implement retrieval, filtering, untrusted-context delimiters, grounding, no-context behavior, and validated citations.
- `ConversationPersistenceService`, `MessageCitationEntity`, `MessageCitationRepository`, and Flyway `V4__add_rag_citations.sql` persist chat mode and citation snapshots.
- `ConversationController` accepts selected document IDs, top-K, and similarity threshold through the existing message endpoint.
- `App.tsx` and `src/api/conversations.ts` retain Normal Chat, add Ask Documents/document selection, and render expandable retrieved-evidence details.

## Commands executed

```powershell
cd backend/muniai-api
mvn test
mvn -DskipTests package

cd ../../frontend/muniai-web
npm.cmd test -- --run
npm.cmd run build

Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/v1/conversations
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/v1/conversations/{id}/messages
Invoke-RestMethod http://127.0.0.1:8080/api/v1/conversations/{id}
```

The message commands were run for Normal Chat, one selected indexed document, two selected indexed documents, and a similarity threshold of 1.0 for the controlled no-context path.

## Automated test results

- Full backend suite: 52 tests passed; 0 failures, errors, or skips.
- Phase 7 tests cover RAG routing, selected-document validation/filtering, ordered/de-duplicated retrieval, prompt boundaries and injection defence, context limits, no-context without model invocation, chat failure without a fake assistant response, citation alignment, structured citation persistence, and conversation reload.
- Frontend: 19 tests passed. The Ask Documents test selects the mode and renders grounded citation/evidence details.
- Both TypeScript configurations and the Vite production build passed.
- Normal Chat regression tests, Phase 6 diagnostic-search tests, and Flyway V1-V4 migration validation passed in the same run.

## Manual test steps

1. Started the complete local stack with PostgreSQL, Ollama `llama3.2:3b`, Ollama embeddings, and Qdrant.
2. Sent a Normal Chat message.
3. Indexed two synthetic documents.
4. Asked a question with one selected document.
5. Asked a question with both selected documents.
6. Reopened the conversation through `GET /api/v1/conversations/{id}`.
7. Sent an unrelated query with threshold 1.0 to force no-relevant-context behavior.
8. Reopened the application conversation and confirmed conversation messages, citation metadata, and the retrieved-evidence panel remained available.
9. Confirmed no-context and dependency-failure behavior without fabricated evidence or assistant messages.

## Results and evidence

- Normal Chat returned `mode=NORMAL`, provider `ollama`, no citations, and the requested response.
- Single-document Ask Documents returned `mode=DOCUMENT_RAG`, `noRelevantContext=false`, one citation tied to the selected document, and a grounded answer.
- The two-document request returned two citations spanning two distinct selected documents.
- Reopening the conversation returned four RAG messages with three persisted citation snapshots across the two assistant answers.
- The threshold-1.0 request returned `noRelevantContext=true` with zero citations and did not require an invented answer.
- Qdrant unavailability produced the standard safe 503 `QDRANT_UNAVAILABLE` envelope with a correlation ID.
- The Phase 6 diagnostic endpoint worked before the forced outage, confirming the RAG work did not remove the diagnostic path.
- Completed repository-owner manual end-to-end verification confirms Normal Chat, Ask Documents, PostgreSQL conversation/citation persistence, single- and multiple-document retrieval, grounded generation, citation alignment, the retrieved-evidence panel, and no-context handling.
- Commit `2464db2` introduced the Phase 7 backend, V4 migration, tests, frontend mode/evidence UI, APIs, and architecture documentation.

## Known limitations

- Retrieval is vector-only with no reranker or hybrid keyword search.
- Citation alignment is deterministic lexical/phrase support, not formal entailment.
- Page numbers are unavailable because chunks do not preserve page mapping.
- Retrieved-evidence metadata supports user inspection but does not constitute a formal proof that every generated statement is logically entailed.

## Final phase status

`VERIFIED`. Automated tests/builds pass, and completed manual end-to-end verification covers Normal Chat, Ask Documents, single- and multiple-document RAG, grounded answers, citation alignment and persistence, conversation reopening, retrieved-evidence display, no-context behavior, and safe dependency failure handling.
