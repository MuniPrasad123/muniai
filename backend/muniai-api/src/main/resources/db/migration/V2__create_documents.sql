CREATE TABLE documents (
    id UUID PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    extraction_status VARCHAR(20) NOT NULL,
    extracted_text TEXT,
    extraction_error VARCHAR(500),
    page_count INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_documents_file_size CHECK (file_size > 0),
    CONSTRAINT ck_documents_status CHECK (extraction_status IN ('UPLOADED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_documents_page_count CHECK (page_count IS NULL OR page_count > 0)
);

CREATE INDEX idx_documents_created_at ON documents (created_at DESC, id);
