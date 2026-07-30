# Phase 7 RAG chat API

Phase 7 extends the existing endpoint:

`POST /api/v1/conversations/{conversationId}/messages`

Normal Chat:

```json
{
  "message": "Explain Spring Boot.",
  "mode": "NORMAL"
}
```

Ask all indexed documents:

```json
{
  "message": "How is traffic distributed between servers?",
  "mode": "DOCUMENT_RAG",
  "documentIds": [],
  "topK": 5
}
```

Ask selected indexed documents by supplying one or more UUIDs in `documentIds`. Only `COMPLETED` indexes are accepted. `topK` must be between 1 and `RAG_MAX_TOP_K`; `similarityThreshold`, when supplied, must be between 0 and 1.

Assistant messages contain:

```json
{
  "mode": "DOCUMENT_RAG",
  "content": "Traffic is forwarded through the load balancer. [Source 1]",
  "citations": [
    {
      "citationId": "uuid",
      "citationIndex": 1,
      "documentId": "uuid",
      "originalFileName": "architecture.pdf",
      "chunkId": "uuid",
      "chunkIndex": 7,
      "pageNumber": null,
      "similarityScore": 0.82,
      "contentPreview": "The load balancer forwards traffic..."
    }
  ]
}
```

`noRelevantContext=true` means retrieval found no acceptable evidence; the model was not called and citations are empty. Dependency failures use the existing safe error envelope and preserve the saved user message.

## Configuration

| Environment variable | Default |
|---|---:|
| `RAG_TOP_K` | `5` |
| `RAG_MAX_TOP_K` | `10` |
| `RAG_SIMILARITY_THRESHOLD` | `0.45` |
| `RAG_MAX_CONTEXT_CHUNKS` | `5` |
| `RAG_MAX_CONTEXT_CHARACTERS` | `6000` |
| `RAG_MAX_HISTORY_MESSAGES` | `8` |
| `RAG_CHAT_MODEL` | empty (uses the configured chat model) |
| `RAG_TEMPERATURE` | `0.1` |
| `RAG_REQUEST_TIMEOUT` | `300s` |

## Frontend usage

Choose **Normal Chat** for general model responses. Choose **Ask Documents**,
optionally select one or more indexed documents, enter a question, and inspect
the expandable **Retrieved evidence** cards. An empty document selection
searches all indexed documents. Spring Boot removes model-written source
labels and attaches validated labels by matching answer sentences against the
retrieved chunks; sentences without textual support remain uncited.

## Testing

```powershell
cd backend/muniai-api
mvn test

cd ../../frontend/muniai-web
npm.cmd test -- --run
npm.cmd run build
```
