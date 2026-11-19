# Phase 8 memory architecture

## High-level architecture

```mermaid
flowchart LR
 U["User"] --> UI["React Memory + Chat UI"] --> API["Spring Boot memory module"] --> P[("PostgreSQL")]
 API --> C["Conversation/RAG services"]
 C --> O["Local Ollama"]
 C --> Q[("Qdrant documents only")]
```
```text
User -> React -> memory API -> PostgreSQL
                    `-> selected approved data -> Normal/RAG prompt -> Ollama
Document chunks -----------------------------------------------> Qdrant
```

## Proposal and approval

```mermaid
flowchart LR
 R["Explicit remember/manual entry"] --> P["PROPOSED"] --> A{"User action"}
 A -->|Approve| X["ACTIVE"]
 A -->|Edit| P
 A -->|Reject| J["REJECTED"]
```
```text
request -> validate/sensitive check -> PROPOSED -> review -> approve -> ACTIVE
                                                `-> edit/reject
```

## Conflict and superseding

```mermaid
flowchart LR
 N["New singular proposal"] --> C{"Active same key?"} -->|Yes| U["Explicit replacement confirmation"] --> S["Old SUPERSEDED"] --> A["New ACTIVE"]
 C -->|No| A
```
```text
new target_role + active target_role -> 409 conflict -> confirm -> old SUPERSEDED + new ACTIVE
```

## Normal chat with memory

```mermaid
flowchart LR
 M["Current message"] --> S["Rule selection"] --> P["System + approved memories + history + message"] --> O["Ollama"] --> U["Usage snapshot"]
```
```text
message -> ACTIVE/non-expired deterministic selection -> bounded prompt -> model success -> usage update
```

## RAG chat with memory

```mermaid
flowchart LR
 M["Question"] --> S["Memory selection"] --> R["Document retrieval"] --> P["System + memories + history + retrieved context + question"] --> O["Ollama"] --> C["Citation alignment + usage"]
```
```text
question -> memory rules + Qdrant evidence -> grounded prompt -> answer -> backend citations + memory disclosure
```

## Forget and delete

```mermaid
flowchart LR
 F["Forget/disable"] --> D["DISABLED"] --> E["Can re-enable"]
 X["Explicit delete"] --> Z["DELETED"] --> N["Never selected"]
```
```text
forget -> confirm match -> DISABLED (reversible)
delete -> DELETED marker -> excluded permanently
```

## Three persistence models

```mermaid
flowchart TB
 P[("PostgreSQL conversations")] --> H["Chat continuity"]
 Q[("Qdrant document vectors")] --> R["RAG evidence + citations"]
 M[("PostgreSQL user_memory")] --> X["Approved personalization"]
```
```text
messages != document chunks != personal memory
continuity   evidence           approved personalization
```

## Failure flow

```mermaid
flowchart LR
 S["Selection"] -->|Failure| W["Warning + chat without memory"]
 O["Model call"] -->|Failure| N["No usage update"]
 A["Approval transaction"] -->|Failure| P["Remain PROPOSED"]
 C["Conflict transaction"] -->|Failure| X["Old ACTIVE preserved"]
```
```text
selection failure -> safe unpersonalized chat
generation failure -> preserve existing failure behavior; no usage/status mutation
transaction failure -> rollback lifecycle change
```

## API and data design

The `/api/v1/memories` resource supports list/filter/get/edit, proposal creation, approve, reject, disable, enable, supersede, delete, and delete-all. `user_memory` stores lifecycle timestamps and optimistic versioning. `message_memory_usage` stores a safe snapshot linked to an assistant message so transparency survives conversation reopening.

All lifecycle mutations are transactional. No database transaction spans Ollama or Qdrant calls. Consequential external-action approvals remain Phase 9 and are not implemented here.
