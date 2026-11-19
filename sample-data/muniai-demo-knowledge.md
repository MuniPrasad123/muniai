# MuniAI Demonstration Knowledge Base

MuniAI is a private, local-first personal AI assistant. Its browser interface is built with React and TypeScript. Its backend is a Java 21 Spring Boot modular monolith.

Conversation history is stored in PostgreSQL. Uploaded documents are validated, stored locally, and extracted into text. Extracted text is split into overlapping chunks. Ollama's `nomic-embed-text` model generates local embeddings, and Qdrant stores the document vectors used for semantic retrieval.

MuniAI has two chat modes. Normal Chat sends the current message and bounded conversation history to the local `llama3.2:3b` model. Ask Documents retrieves relevant chunks from selected indexed documents, builds a bounded grounded prompt, and returns citations with an inspectable evidence panel. When no relevant document context is found, the assistant returns a safe no-context response instead of inventing evidence.

Controlled long-term memory is separate from conversation history and document knowledge. A memory starts as PROPOSED and cannot influence an answer until the user explicitly approves it. Active memories may be edited, disabled, re-enabled, superseded, or deleted. Sensitive values such as passwords, API keys, and access tokens are rejected by deterministic validation before persistence.

All core runtime services bind to loopback interfaces. PostgreSQL stores structured application data, Qdrant stores document vectors, and Ollama performs chat and embedding inference locally. MuniAI does not require a paid cloud model API for its demonstrated features.
