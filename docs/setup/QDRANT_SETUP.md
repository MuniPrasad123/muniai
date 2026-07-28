# Local Qdrant and embedding setup

## Start Qdrant

From the repository root:

```powershell
docker compose -f infrastructure/qdrant-compose.yml up -d
Invoke-RestMethod http://127.0.0.1:6333/collections
```

Ports bind to loopback only. Persistent data is written under ignored `local-data/qdrant/`.

Without Compose:

```powershell
docker run --name muniai-qdrant -p 127.0.0.1:6333:6333 -p 127.0.0.1:6334:6334 -v "${PWD}/local-data/qdrant:/qdrant/storage" qdrant/qdrant:v1.15.4
```

## Install the embedding model

```powershell
ollama pull nomic-embed-text
ollama list
```

The default expected model name is `nomic-embed-text:latest` and its vector dimension is 768. The chat model remains `llama3.2:3b`; these are deliberately separate responsibilities.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `DOCUMENT_CHUNK_SIZE` | `1000` | Maximum characters per chunk |
| `DOCUMENT_CHUNK_OVERLAP` | `150` | Repeated characters near boundaries |
| `DOCUMENT_INDEX_BATCH_SIZE` | `8` | Maximum points in one Qdrant upsert |
| `OLLAMA_EMBEDDING_BASE_URL` | `http://127.0.0.1:11434` | Local embedding API |
| `OLLAMA_EMBEDDING_MODEL` | `nomic-embed-text:latest` | Dedicated embedding model |
| `OLLAMA_EMBEDDING_TIMEOUT` | `120s` | Per-request timeout |
| `OLLAMA_EMBEDDING_DIMENSION` | `768` | Required vector length |
| `QDRANT_HOST` | `127.0.0.1` | Qdrant host |
| `QDRANT_HTTP_PORT` | `6333` | HTTP API port |
| `QDRANT_GRPC_PORT` | `6334` | Reserved gRPC port |
| `QDRANT_API_KEY` | empty | Optional secret; never commit it |
| `QDRANT_HTTPS_ENABLED` | `false` | Local HTTP by default |
| `QDRANT_COLLECTION_NAME` | `muniai_document_chunks` | Chunk collection |
| `QDRANT_DISTANCE_METRIC` | `COSINE` | Similarity metric |
| `QDRANT_REQUEST_TIMEOUT` | `30s` | Vector-store timeout |

If a collection already exists with another dimension or distance metric, MuniAI fails safely. Stop the backend and either configure the matching model/dimension or explicitly remove/recreate the development collection after confirming its contents are disposable. It is never silently deleted.

## Stop and inspect

```powershell
docker compose -f infrastructure/qdrant-compose.yml ps
docker compose -f infrastructure/qdrant-compose.yml logs qdrant
docker compose -f infrastructure/qdrant-compose.yml down
```

`down` stops containers but retains the bind-mounted local data. Removing `local-data/qdrant` is destructive and must be an explicit operator decision.
