# Phase 4: conversation history

## Architecture

```text
React chat UI
    -> Spring Boot REST API
        -> PostgreSQL: conversations and messages
        -> Ollama: local inference
```

All runtime connections use loopback addresses. PostgreSQL stores conversation metadata and chronological `USER`, `ASSISTANT`, or `SYSTEM` messages. A database foreign key cascade deletes messages with their conversation.

## Request lifecycle

1. The browser creates or selects a conversation.
2. The message endpoint validates the text.
3. The backend commits the user message in its own transaction. The first message also supplies a simple truncated title.
4. The existing Ollama adapter performs local inference.
5. On success, the assistant message and model name are committed and returned.
6. On Ollama failure, the API returns the existing safe provider error. The committed user message remains and no assistant message is invented.

The separate commits are deliberate so an inference failure cannot roll back evidence of the user request.

Saving is automatic and records remain until manual deletion. There is no scheduled expiry. Conversation history is stored chat, not long-term AI memory. Phase 4 adds no embeddings, vector search, RAG, summarization, documents, Qdrant, or external integrations. The existing adapter still sends the current message to Ollama.
