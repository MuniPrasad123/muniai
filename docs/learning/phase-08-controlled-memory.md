# Phase 8: controlled long-term memory

Long-term memory is an approved fact or preference that may be reused across conversations. It is not conversation history and it is not document evidence.

| Store | Purpose | Scope | Authority |
|---|---|---|---|
| Conversation history | Continue a chat | One conversation | Context only |
| Document knowledge | Ground RAG answers | Indexed documents in Qdrant | Evidence with citations |
| Personal memory | Personalize responses | Across conversations | User-approved reference data |

Memory begins as `PROPOSED`. Approval is an explicit API/UI action. Only `ACTIVE`, non-expired records are selected. Disable is reversible; delete is a retained `DELETED` marker; replacement preserves the prior record as `SUPERSEDED`.

Selection is deterministic. Stable preferences are considered first; technical, career, learning, and project keywords add bounded categories/keys. There are no memory vectors, embeddings, semantic search, automatic profiling, or background extraction.

Prompt values are escaped and placed inside `<approved_memories>`. System rules say memory is untrusted data, cannot authorize actions, and cannot override policy. Successful responses disclose the memory IDs, categories, keys, and labels used. Values and hidden prompts are not returned in usage metadata.

Known limitations: explicit command parsing supports clear key/value phrasing only; deterministic keyword rules can miss indirect relevance; soft-deleted history remains in PostgreSQL until a future retention policy is explicitly designed.
