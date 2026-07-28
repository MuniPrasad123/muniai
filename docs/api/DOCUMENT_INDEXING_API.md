# Phase 6 document indexing API

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/documents/{id}/index` | Create or idempotently replace an index |
| `POST` | `/api/v1/documents/{id}/reindex` | Explicitly replace chunks and vectors |
| `GET` | `/api/v1/documents/{id}/index-status` | Inspect separate indexing state |
| `DELETE` | `/api/v1/documents/{id}/index` | Remove chunks/vectors but keep the document |
| `POST` | `/api/v1/vector-search/test` | Diagnostic similarity search only |

Indexing requires `extractionStatus=COMPLETED` and nonblank extracted text. Both index operations remove prior Qdrant points and PostgreSQL chunks first, so repeated calls do not create duplicates.

Diagnostic request:

```json
{
  "query": "experience with Spring Boot",
  "limit": 5,
  "documentId": null
}
```

The response includes chunk/document IDs, chunk index, similarity score, a maximum 300-character preview, and original filename. It never calls the chat model or produces an answer.

Common safe errors include `DOCUMENT_NOT_EXTRACTED`, `DOCUMENT_TEXT_EMPTY`, `EMPTY_EMBEDDING`, `EMBEDDING_DIMENSION_MISMATCH`, `EMBEDDING_PROVIDER_UNAVAILABLE`, `QDRANT_UNAVAILABLE`, `QDRANT_COLLECTION_MISMATCH`, and `QDRANT_POINT_COUNT_MISMATCH`.

On failure, inspect `index-status`, correct local Ollama/Qdrant configuration, and call re-index. If vector cleanup itself failed, restore Qdrant and use `DELETE .../{id}/index` or re-index to reconcile that document.
