# Learning checklist

## Phase 2

- [ ] Trace a chat request from the React UI to Ollama.
- [ ] Read the chat controller and provider classes.

## Phase 3

- [ ] Review the Vite proxy configuration.
- [ ] Understand why the frontend uses separate API modules for conversations and documents.

## Phase 4

- [ ] Explain how conversations and messages are persisted.
- [ ] Identify the main migration files and their responsibilities.

## Phase 5

- [ ] Review the upload validation rules.
- [ ] Explain how the original document file is stored and why the filename is sanitized.

## Phase 6

- [ ] Identify the chunking configuration values.
- [ ] Trace one chunk from the document service to Qdrant.

## Phase 7

- [ ] Explain the difference between NORMAL and DOCUMENT_RAG modes.
- [ ] Trace the retrieval -> prompt building -> citation alignment path.
