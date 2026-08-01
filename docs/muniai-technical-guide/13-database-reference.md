# Database reference

## Flyway migrations

- V1 creates conversations and messages.
- V2 creates documents.
- V3 adds indexing metadata and document_chunks.
- V4 adds chat_mode and message_citations.

## Tables

### conversations

- id: UUID primary key
- title: VARCHAR(200)
- created_at, updated_at: TIMESTAMP WITH TIME ZONE

### messages

- id: UUID primary key
- conversation_id: UUID foreign key to conversations(id)
- role: VARCHAR(20)
- content: TEXT
- model: VARCHAR(255)
- chat_mode: VARCHAR(20)
- created_at: TIMESTAMP WITH TIME ZONE

### documents

- id: UUID primary key
- original_file_name, stored_file_name, content_type, storage_path
- extraction_status, extracted_text, extraction_error, page_count
- indexing_status, indexing_started_at, indexing_completed_at, indexing_error
- chunk_count, embedding_model, embedding_dimension, qdrant_collection_name
- created_at, updated_at

### document_chunks

- id: UUID primary key
- document_id: UUID foreign key to documents(id)
- chunk_index, content, content_hash, character_start, character_end, token_count_estimate
- qdrant_point_id, embedding_model, created_at, updated_at

### message_citations

- id: UUID primary key
- message_id: UUID foreign key to messages(id)
- citation_index, document_id, chunk_id, chunk_index, original_file_name, page_number, similarity_score, content_preview, created_at
