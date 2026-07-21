# System context

## Purpose

MuniAI assists one user with private conversations, documents, memory, and explicitly enabled external services. Its default operating boundary is the user's trusted local machine.

## People and systems

- **User:** owns the data, initiates work, reviews output, grants connector access, and approves side effects.
- **MuniAI:** coordinates chat, retrieval, memory, approvals, and connector adapters as those capabilities are introduced.
- **Local Ollama:** a future local inference provider; model output is untrusted and advisory.
- **Local stores:** future PostgreSQL, Qdrant, Redis, and file storage, each reachable only from required local components.
- **External providers:** future GitHub, Gmail, and other explicitly approved services accessed through scoped OAuth tokens.
- **LinkedIn:** receives no automated publication; the user manually transfers and publishes approved drafts.

## Core constraints

- Local processing and storage are the default.
- External network access is capability-specific and visible.
- Imported content and model output cannot grant authority.
- The user must approve consequential external mutations.
- Data export, deletion, and connector revocation must remain possible.

## Out of scope for Phase 0

No executable application, inference connection, data store, authentication, retrieval pipeline, or external connector is created in this phase.

