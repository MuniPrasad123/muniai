# Phase 7 — RAG and citations

## Goal

Combine retrieval and generation so the chat can answer from the most relevant retrieved document chunks rather than from general model knowledge.

## Main classes

- [backend/muniai-api/src/main/java/com/muniai/conversation/application/RagService.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/RagService.java)
- [backend/muniai-api/src/main/java/com/muniai/conversation/application/RagPromptBuilder.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/RagPromptBuilder.java)
- [backend/muniai-api/src/main/java/com/muniai/conversation/application/CitationAligner.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/CitationAligner.java)
- [backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationApplicationService.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationApplicationService.java)

## Chat modes

The conversation API supports NORMAL and DOCUMENT_RAG. The frontend exposes the latter as Ask Documents.

## RAG flow

Question -> embed -> retrieve from Qdrant -> filter weak results -> build grounded prompt -> call Ollama -> align citations -> save assistant answer and citations.

## Citation behavior

The citation aligner removes any model-written source labels, splits the answer into sentences, and aligns each sentence to the best matching source chunk. Citations are then persisted with a document ID, chunk ID, filename, similarity score, and preview text.
