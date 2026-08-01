# Code navigation guide

## I want to understand chat requests

1. [frontend/muniai-web/src/App.tsx](../../frontend/muniai-web/src/App.tsx)
2. [frontend/muniai-web/src/api/conversations.ts](../../frontend/muniai-web/src/api/conversations.ts)
3. [backend/muniai-api/src/main/java/com/muniai/conversation/api/ConversationController.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/api/ConversationController.java)
4. [backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationApplicationService.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationApplicationService.java)
5. [backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaLanguageModelProvider.java](../../backend/muniai-api/src/main/java/com/muniai/ai/infrastructure/OllamaLanguageModelProvider.java)

## I want to understand document indexing

1. [frontend/muniai-web/src/DocumentManager.tsx](../../frontend/muniai-web/src/DocumentManager.tsx)
2. [backend/muniai-api/src/main/java/com/muniai/document/api/DocumentController.java](../../backend/muniai-api/src/main/java/com/muniai/document/api/DocumentController.java)
3. [backend/muniai-api/src/main/java/com/muniai/document/application/DocumentApplicationService.java](../../backend/muniai-api/src/main/java/com/muniai/document/application/DocumentApplicationService.java)
4. [backend/muniai-api/src/main/java/com/muniai/document/application/DocumentIndexingService.java](../../backend/muniai-api/src/main/java/com/muniai/document/application/DocumentIndexingService.java)
5. [backend/muniai-api/src/main/java/com/muniai/document/infrastructure/QdrantVectorStore.java](../../backend/muniai-api/src/main/java/com/muniai/document/infrastructure/QdrantVectorStore.java)

## I want to understand RAG

1. [backend/muniai-api/src/main/java/com/muniai/conversation/application/RagService.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/RagService.java)
2. [backend/muniai-api/src/main/java/com/muniai/conversation/application/RagPromptBuilder.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/RagPromptBuilder.java)
3. [backend/muniai-api/src/main/java/com/muniai/conversation/application/CitationAligner.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/CitationAligner.java)
