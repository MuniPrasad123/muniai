# Executive overview

MuniAI is a private, local-first personal assistant that runs on a single trusted machine. It is designed to keep the browser, API, database, local files, and model requests on loopback or local storage instead of requiring a cloud service.

## What problem it solves

The project demonstrates how to combine a local chat model, a local vector database, and a small document workflow so a user can chat with a local assistant, upload documents, index them, retrieve relevant chunks, and ask grounded questions with citations.

## Main user journeys

1. Start a chat and receive an answer from Ollama.
2. Persist conversations in PostgreSQL.
3. Upload documents, extract text, and store the original file locally.
4. Index extracted text into Qdrant and run diagnostic vector searches.
5. Ask document-grounded questions with citations.

## High-level component responsibilities

- React UI: collect input and render chat and document views.
- Spring Boot API: validate requests, coordinate services, and persist state.
- PostgreSQL: store conversation history, document metadata, and citation records.
- Ollama: answer chat prompts and create embeddings.
- Qdrant: store vectors and return nearest neighbors.
- Local filesystem: hold the original uploaded documents.
