# Configuration reference

## Backend configuration

| Property | Purpose | Default | Source |
| --- | --- | --- | --- |
| server.port | Backend port | 8080 | application.yml |
| spring.datasource.url | PostgreSQL URL | jdbc:postgresql://127.0.0.1:5432/muniai | application.yml |
| muniai.chat.max-message-length | Maximum chat length | 10000 | application.yml |
| muniai.ollama.base-url | Ollama base URL | http://127.0.0.1:11434 | application.yml |
| muniai.ollama.model | Chat model | llama3.2:3b | application.yml |
| muniai.documents.upload-directory | Upload root | ../../local-data/documents | application.yml |
| muniai.documents.max-file-size | Upload size limit | 10MB | application.yml |
| muniai.indexing.chunk-size | Chunk size | 1000 | application.yml |
| muniai.indexing.chunk-overlap | Chunk overlap | 150 | application.yml |
| muniai.indexing.embedding.model | Embedding model | nomic-embed-text:latest | application.yml |
| muniai.indexing.embedding.dimension | Embedding dimension | 768 | application.yml |
| muniai.indexing.qdrant.collection-name | Qdrant collection | muniai_document_chunks | application.yml |
| muniai.rag.top-k | RAG top-K | 5 | application.yml |
| muniai.rag.similarity-threshold | Similarity threshold | 0.45 | application.yml |

## Frontend configuration

- [frontend/muniai-web/vite.config.ts](../../frontend/muniai-web/vite.config.ts) sets the dev server to 127.0.0.1:5173 and proxies /api to 127.0.0.1:8080.
