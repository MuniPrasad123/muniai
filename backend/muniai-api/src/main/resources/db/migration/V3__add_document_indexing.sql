ALTER TABLE documents ADD COLUMN indexing_status VARCHAR(20) NOT NULL DEFAULT 'NOT_INDEXED';
ALTER TABLE documents ADD COLUMN indexing_started_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE documents ADD COLUMN indexing_completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE documents ADD COLUMN indexing_error VARCHAR(500);
ALTER TABLE documents ADD COLUMN chunk_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE documents ADD COLUMN embedding_model VARCHAR(255);
ALTER TABLE documents ADD COLUMN embedding_dimension INTEGER;
ALTER TABLE documents ADD COLUMN qdrant_collection_name VARCHAR(255);

ALTER TABLE documents ADD CONSTRAINT ck_documents_indexing_status
    CHECK (indexing_status IN ('NOT_INDEXED', 'PROCESSING', 'COMPLETED', 'FAILED'));
ALTER TABLE documents ADD CONSTRAINT ck_documents_chunk_count CHECK (chunk_count >= 0);

CREATE TABLE document_chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    character_start INTEGER NOT NULL,
    character_end INTEGER NOT NULL,
    token_count_estimate INTEGER NOT NULL,
    qdrant_point_id UUID NOT NULL UNIQUE,
    embedding_model VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_document_chunks_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT uq_document_chunks_document_index UNIQUE (document_id, chunk_index),
    CONSTRAINT ck_document_chunks_index CHECK (chunk_index >= 0),
    CONSTRAINT ck_document_chunks_range CHECK (character_start >= 0 AND character_end > character_start),
    CONSTRAINT ck_document_chunks_tokens CHECK (token_count_estimate > 0)
);

CREATE INDEX idx_document_chunks_document ON document_chunks (document_id, chunk_index);
CREATE INDEX idx_documents_indexing_status ON documents (indexing_status);
