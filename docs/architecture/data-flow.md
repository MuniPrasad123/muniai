# Data flow

## Chat flow

1. The user submits a message through the local frontend.
2. The backend validates size, type, session, and policy context.
3. When enabled, retrieval selects authorized local context and records source references.
4. The backend constructs a bounded prompt and sends it to local Ollama.
5. The response is treated as untrusted content, filtered for display, and returned with available provenance.
6. Only permitted metadata and conversation content are persisted under configured retention rules.

## Document flow

Uploaded files enter an untrusted staging boundary. Future ingestion must validate type and size, reject unsafe formats, store originals outside web roots, extract content with constrained tooling, and attach ownership and deletion metadata. Derived chunks and embeddings must retain links to their source and authorization boundary.

## Memory flow

Long-term memory is opt-in. Candidate memories are presented or governed by explicit policy, then stored with origin, purpose, timestamp, and deletion controls. Retrieval must respect sensitivity and current user intent.

## External action flow

1. A connector request is built from user intent and least-privilege credentials.
2. Read operations are labeled with provider and freshness.
3. A proposed mutation is rendered as an exact preview.
4. The user explicitly approves that specific action.
5. The backend revalidates the request, executes it once, and records outcome metadata.

LinkedIn ends at an approved local draft; publication is performed manually by the user.

## Logging flow

Logs and audit records contain identifiers, decisions, timestamps, and outcomes—not credentials or full sensitive payloads. Diagnostic verbosity must never silently expand the data collected.

