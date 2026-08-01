# End-to-end code flow

This chapter traces the main user journeys through the repository.

## 1. Normal chat

User message -> [frontend/muniai-web/src/App.tsx](../../frontend/muniai-web/src/App.tsx) -> [frontend/muniai-web/src/api/conversations.ts](../../frontend/muniai-web/src/api/conversations.ts) -> [backend/muniai-api/src/main/java/com/muniai/conversation/api/ConversationController.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/api/ConversationController.java) -> [backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationApplicationService.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationApplicationService.java) -> [backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaLanguageModelProvider.java](../../backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaLanguageModelProvider.java) -> Ollama -> response saved to PostgreSQL.

## 2. Upload a PDF

The document manager calls POST /api/v1/documents. The backend stores the file, updates the document entity, and extracts text.

## 3. Index a document

The UI calls POST /api/v1/documents/{id}/index. The indexing service chunks the extracted text, calls the embedding model, and upserts vectors to Qdrant.

## 4. Ask a document question using RAG

The UI selects a single indexed document, sends the question in DOCUMENT_RAG mode, and the backend retrieves matching chunks from Qdrant.
