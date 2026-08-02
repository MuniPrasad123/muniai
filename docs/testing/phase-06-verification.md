# Phase 6 verification

Date: 2026-08-02

Final status: `VERIFIED`

## Scope

Phase 6 adds deterministic document chunking, local Ollama embeddings, PostgreSQL chunk metadata, Qdrant vector storage, indexing/re-indexing/index removal, and a diagnostic semantic-search endpoint. It does not generate answers or citations.

## Features checked

- `DocumentChunker`, `DocumentIndexingService`, `EmbeddingProvider`, `OllamaEmbeddingProvider`, `VectorStore`, and `QdrantVectorStore` implement the indexing workflow.
- `DocumentChunkEntity`, `DocumentChunkRepository`, and Flyway `V3__add_document_indexing.sql` provide authoritative chunk/index state and constraints.
- `DocumentController` exposes index, re-index, index-status, and index-removal endpoints.
- `VectorSearchController` exposes `/api/v1/vector-search/test` without calling the chat model.
- `DocumentIndexPanel.tsx`, `DocumentManager.tsx`, and `src/api/documents.ts` expose indexing status and confirmed re-index/index-removal actions.
- Failure compensation removes partial chunks/vectors where possible and records safe indexing errors.

## Commands executed

```powershell
cd backend/muniai-api
mvn test
mvn -DskipTests package

cd ../../frontend/muniai-web
npm.cmd test -- --run
npm.cmd run build

docker compose -f infrastructure/qdrant-compose.yml up -d
Invoke-RestMethod http://127.0.0.1:11434/api/tags
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/v1/documents/{id}/index
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/v1/documents/{id}/reindex
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/v1/vector-search/test
Invoke-RestMethod -Method Delete http://127.0.0.1:8080/api/v1/documents/{id}/index
docker compose -f infrastructure/qdrant-compose.yml stop qdrant
docker compose -f infrastructure/qdrant-compose.yml start qdrant
```

## Automated test results

- Full backend suite: 52 tests passed; 0 failures, errors, or skips.
- Phase 6 coverage includes ordered overlapping chunks, stable hashes, embedding response/dimension validation, safe Qdrant collection/payload mapping, document filters, indexing, idempotent re-indexing, index deletion, indexed-document deletion, failure state, and partial cleanup.
- Flyway validated and applied V1 through V4 in H2 PostgreSQL compatibility mode.
- Frontend: 19 tests passed, including indexing metadata, confirmed re-indexing, and confirmed index removal.
- Both TypeScript configurations and the production build passed.

## Manual test steps

1. Started Docker Desktop and repository-pinned Qdrant 1.15.4; confirmed loopback ports 6333/6334.
2. Confirmed Ollama exposed `nomic-embed-text:latest` with embedding dimension 768.
3. Ran the application with PostgreSQL, Ollama, and Qdrant using the repository configuration.
4. Uploaded and indexed two synthetic documents.
5. Checked chunk counts, embedding metadata, exact Qdrant point count, and diagnostic semantic search.
6. Re-indexed one document and compared point counts before and after.
7. Removed the second document's index and checked both status and Qdrant count.
8. Indexed multiple documents and confirmed independent vectors and document-filtered search results.
9. Restarted and recovered Qdrant, then reconfirmed the collection, semantic search, re-indexing, and index removal paths.

## Results and evidence

- Both documents reached `indexingStatus=COMPLETED`, each with one chunk, model `nomic-embed-text:latest`, and dimension 768.
- Qdrant contained exactly two points after indexing.
- Diagnostic search returned the selected document.
- Re-indexing preserved one chunk for the document and the total point count remained two, demonstrating no duplicate points.
- Removing the second index produced `NOT_INDEXED`, zero chunks, and reduced Qdrant count from two to one.
- With Qdrant stopped, the API returned HTTP 503, code `QDRANT_UNAVAILABLE`, a safe message, and a correlation ID.
- Completed repository-owner manual verification confirms PostgreSQL chunk metadata, multiple-document indexing, local embeddings, Qdrant recovery, semantic search after recovery, re-indexing, and index removal.
- Qdrant recovery is complete; there is no remaining known collection-corruption blocker.
- Commit `3e8397b` introduced the Phase 6 indexing stack; `6a9e34c` strengthened Qdrant integration and UI behavior.

## Known limitations

- Chunk size is character-based rather than tokenizer-exact.
- Embeddings are generated sequentially; there is no hybrid search or reranking.
- Diagnostic search returns previews only.
- Vector storage remains local and uses deterministic document filters; cloud vector stores are outside scope.

## Final phase status

`VERIFIED`. Automated tests/builds pass, and completed manual verification covers chunk persistence, real local embeddings, single- and multiple-document Qdrant indexing, semantic search, duplicate-free re-indexing, index removal, safe failure handling, and successful Qdrant recovery.
