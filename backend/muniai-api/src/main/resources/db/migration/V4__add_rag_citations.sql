ALTER TABLE messages ADD COLUMN chat_mode VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
ALTER TABLE messages ADD CONSTRAINT ck_messages_chat_mode
    CHECK (chat_mode IN ('NORMAL', 'DOCUMENT_RAG'));

CREATE TABLE message_citations (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    citation_index INTEGER NOT NULL,
    document_id UUID NOT NULL,
    chunk_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    page_number INTEGER,
    similarity_score DOUBLE PRECISION NOT NULL,
    content_preview VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_message_citations_message
        FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT uq_message_citations_message_index UNIQUE (message_id, citation_index),
    CONSTRAINT ck_message_citations_index CHECK (citation_index > 0),
    CONSTRAINT ck_message_citations_chunk_index CHECK (chunk_index >= 0),
    CONSTRAINT ck_message_citations_page CHECK (page_number IS NULL OR page_number > 0)
);

CREATE INDEX idx_message_citations_message ON message_citations (message_id, citation_index);
CREATE INDEX idx_message_citations_document ON message_citations (document_id);
CREATE INDEX idx_message_citations_chunk ON message_citations (chunk_id);
