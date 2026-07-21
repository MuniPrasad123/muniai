# Implementation plan

MuniAI will be delivered incrementally. Each phase must preserve the local-first security model and include focused verification before the next phase begins.

## Phase sequence

0. Establish repository conventions, architecture, ADRs, security guidance, and environment checks.
1. Verify a local Ollama installation and a deliberately selected model.
2. Add a minimal Spring Boot boundary for local Ollama communication.
3. Add a React chat interface with no connector or retrieval scope.
4. Persist conversation history in PostgreSQL with migrations and retention controls.
5. Add constrained document upload with validation, limits, and safe storage.
6. Generate embeddings locally and store vectors in Qdrant.
7. Complete retrieval-augmented chat with citations and failure handling.
8. Add opt-in, inspectable, editable, and deletable long-term memory.
9. Add an approval framework and tamper-evident audit trail for side effects.
10. Add a least-privilege, read-only GitHub integration.
11. Add LinkedIn drafting with manual publication only.
12. Add portfolio analysis with explicit source and freshness indicators.
13. Add a least-privilege, read-only Gmail integration.
14. Allow Gmail draft creation only after explicit approval; never send mail automatically.
15. Add a career and job tracker with private local records.
16. Add scheduled proactive assistance with bounded tasks, approvals, and opt-out controls.

## Delivery gate

A phase is complete only when its artifacts exist, security implications are reviewed, relevant checks pass, and `IMPLEMENTATION_STATUS.md` records the result honestly. A blocked or unavailable dependency must remain explicit rather than being simulated as verified.

