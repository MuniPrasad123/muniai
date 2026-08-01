# Phase 6 — chunking, embeddings, and Qdrant

## Goal

Turn extracted text into searchable vector representations and store them in Qdrant.

## Main classes

- [backend/muniai-api/src/main/java/com/muniai/document/application/DocumentChunker.java](../../backend/muniai-api/src/main/java/com/muniai/document/application/DocumentChunker.java)
- [backend/muniai-api/src/main/java/com/muniai/document/application/DocumentIndexingService.java](../../backend/muniai-api/src/main/java/com/muniai/document/application/DocumentIndexingService.java)
- [backend/muniai-api/src/main/java/com/muniai/document/infrastructure/OllamaEmbeddingProvider.java](../../backend/muniai-api/src/main/java/com/muniai/document/infrastructure/OllamaEmbeddingProvider.java)
- [backend/muniai-api/src/main/java/com/muniai/document/infrastructure/QdrantVectorStore.java](../../backend/muniai-api/src/main/java/com/muniai/document/infrastructure/QdrantVectorStore.java)

## Chunking behavior

The chunker normalizes whitespace, splits big documents into overlapping chunks, hashes each chunk, and keeps metadata such as character start/end and token estimate.

## Embedding flow

Chunk text -> call Ollama embedding endpoint -> numeric vector -> validate dimension -> Qdrant point.

## Qdrant behavior

The vector store ensures a collection exists, creates it if missing, upserts points, deletes points by document, and returns search hits.
