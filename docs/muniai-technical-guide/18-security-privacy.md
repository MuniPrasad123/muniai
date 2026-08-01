# Security and privacy

## Local-first architecture

The design keeps all major data local: PostgreSQL stores conversation and metadata, the filesystem stores original documents, Ollama handles chat and embeddings, and Qdrant stores vector data.

## Upload validation and path safety

The backend rejects empty uploads, unsupported file types, oversized files, and invalid signatures. It also sanitizes file names and prevents path traversal by resolving and validating the storage path.

## Prompt injection and model output

The RAG prompt explicitly tells the model to treat document content as untrusted reference material and to ignore prompt-injection instructions inside the retrieved text.

## Limitations

The repository does not implement encryption-at-rest, user authentication, or an approval framework. The guide reflects the implementation that currently exists.
