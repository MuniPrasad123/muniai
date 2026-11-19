# Memory security

## Policy

- Memory is local PostgreSQL data and is never written to Qdrant.
- Nothing becomes active without an explicit approval endpoint call.
- Passwords, keys, tokens, private-key material, card/bank identifiers, government identifiers, and authentication answers are rejected by application code before a proposal is stored.
- Memory payloads and prompts are not logged. Standard errors contain a code and correlation ID, not the rejected value.
- Only active, non-expired memories enter prompts. Disabled, rejected, superseded, and deleted records are excluded.
- Usage snapshots expose ID/category/key/label only. Normalized values, entity versions, and prompts are never returned.

## Injection boundary

Memory values are HTML-delimiter escaped and wrapped in `<approved_memories>`. Both Normal and RAG system layers state that stored text is reference data, not instruction or authorization. Application code—not the model—enforces lifecycle, limits, conflicts, expiry, and sensitive-value rejection.

## Privacy and deletion

Deletion immediately removes a record from selection by changing its status to `DELETED`. Historical status is retained for transparency. Delete-all applies the same transition to every non-deleted record. Backups remain subject to the repository's local data policy.

## Remaining risks

Pattern-based secret detection is intentionally conservative but cannot recognize every sensitive phrase. Users should never place secrets in chat or memory. Deterministic relevance can over-select a benign memory, which is why every response discloses the selected records.
