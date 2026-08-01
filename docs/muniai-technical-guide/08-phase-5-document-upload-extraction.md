# Phase 5 — document upload and text extraction

## Goal

Allow a user to upload local documents, store them safely, extract text, and track the extraction status.

## Key classes

- [backend/muniai-api/src/main/java/com/muniai/document/api/DocumentController.java](../../backend/muniai-api/src/main/java/com/muniai/document/api/DocumentController.java)
- [backend/muniai-api/src/main/java/com/muniai/document/application/DocumentApplicationService.java](../../backend/muniai-api/src/main/java/com/muniai/document/application/DocumentApplicationService.java)
- [backend/muniai-api/src/main/java/com/muniai/document/application/DocumentExtractor.java](../../backend/muniai-api/src/main/java/com/muniai/document/application/DocumentExtractor.java)
- [backend/muniai-api/src/main/java/com/muniai/document/infrastructure/DocumentEntity.java](../../backend/muniai-api/src/main/java/com/muniai/document/infrastructure/DocumentEntity.java)

## Validation rules

- Only PDF, TXT, and Markdown files are accepted.
- The implementation checks for empty payloads, oversized uploads, and invalid signatures.
- File names are sanitized to reduce path traversal risk.

## Storage behavior

The original file is placed under the configured upload directory and stored by a UUID-based safe file name. The document record in PostgreSQL stores the original file name and status.
