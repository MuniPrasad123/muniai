# Phase 5: secure document upload and extraction

## Architecture

```text
React document-management UI
    -> Spring Boot Document API
        -> local filesystem: original files with UUID-based names
        -> PostgreSQL: metadata, status, errors, and extracted text
```

The document module is separate from chat and owns its table, repository, validation, storage, and extraction behavior. V2 adds `documents` without changing Phase 4 conversation tables.

## Upload flow

1. React checks extension, non-empty size, and the 10 MB default before upload.
2. The API enforces configured size, MIME type, extension, and practical content signatures.
3. The display name becomes a safe basename. Storage uses a UUID filename and a normalized path constrained beneath the configured root.
4. The bytes are written locally and an `UPLOADED` row is committed. A database failure triggers best-effort file cleanup.
5. Status changes to `PROCESSING`; PDFBox extracts native PDF text page by page, while TXT and Markdown use strict UTF-8 decoding.
6. Success stores `COMPLETED`, text, and PDF page count. Failure stores `FAILED` and a safe error while retaining the original and metadata.
7. Delete removes the file first, then its row. A missing file is already absent; other deletion failures return an error and retain the row.

The API never returns the internal filename, storage root, or absolute path. Uploaded content is not logged.

## Formats and limits

- PDF (`application/pdf`) with `%PDF-` signature
- TXT (`text/plain`) with strict UTF-8 decoding
- Markdown (`text/markdown`, `text/x-markdown`, or browser-supplied `text/plain`) with strict UTF-8 decoding

DOCX is intentionally excluded to avoid an unnecessary parser surface. Scanned/image-only PDFs fail extraction because Phase 5 has no OCR. The default maximum is 10 MB. Configure `MUNIAI_DOCUMENT_MAX_SIZE` and `MUNIAI_DOCUMENT_UPLOAD_DIR`.

## Scope boundary

Phase 5 stores complete extracted text only. It does not implement chunking, embeddings, Qdrant, semantic search, retrieval, RAG, document-aware chat, OCR, summaries, or memory.
