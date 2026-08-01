# API reference

## Health

- GET /api/v1/system/health — returns service health information.

## Chat

- POST /api/v1/chat — sends a plain chat request to Ollama.

## Conversations

- POST /api/v1/conversations — create a conversation.
- GET /api/v1/conversations — list saved conversations.
- GET /api/v1/conversations/{id} — load one conversation.
- PATCH /api/v1/conversations/{id} — rename a conversation.
- POST /api/v1/conversations/{id}/messages — send a message in NORMAL or DOCUMENT_RAG mode.
- DELETE /api/v1/conversations/{id} — delete one conversation.

## Documents

- POST /api/v1/documents — upload a document.
- GET /api/v1/documents — list documents.
- GET /api/v1/documents/{id} — get document metadata.
- GET /api/v1/documents/{id}/text — read extracted text.
- DELETE /api/v1/documents/{id} — delete document and cleanup.
- POST /api/v1/documents/{id}/index — index a document.
- POST /api/v1/documents/{id}/reindex — re-index a document.
- GET /api/v1/documents/{id}/index-status — read indexing status.
- DELETE /api/v1/documents/{id}/index — remove the index.

## Vector diagnostics

- POST /api/v1/vector-search/test — run a diagnostic vector search.
