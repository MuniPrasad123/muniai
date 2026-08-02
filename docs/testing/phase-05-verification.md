# Phase 5 verification

Date: 2026-08-02

Final status: `VERIFIED`

## Scope

Phase 5 provides constrained local PDF, UTF-8 text, and Markdown upload; validation; UUID-based local storage; PostgreSQL document metadata; text extraction and viewing; listing; and safe deletion. It does not include chunking, embeddings, vector search, RAG, OCR, or memory.

## Features checked

- `DocumentController` exposes upload, list, detail, extracted-text, and delete endpoints under `/api/v1/documents`.
- `DocumentApplicationService`, `DocumentExtractor`, `DocumentConfigurationProperties`, `DocumentEntity`, and `DocumentRepository` implement validation, safe storage, extraction state, persistence, and deletion.
- Flyway `V2__create_documents.sql` owns document metadata and extracted text without changing conversation persistence.
- `DocumentManager.tsx` and `src/api/documents.ts` implement upload, list, extracted-text viewing, error/empty/loading states, and confirmed deletion.
- File type, extension, size, signature, UTF-8 decoding, filename/path containment, extraction failure, and missing-file deletion behavior are covered by application code and tests.

## Commands executed

```powershell
cd backend/muniai-api
mvn test
mvn -DskipTests package

cd ../../frontend/muniai-web
npm.cmd test -- --run
npm.cmd run build

curl.exe -F "file=@<synthetic-text-file>" http://127.0.0.1:8080/api/v1/documents
Invoke-RestMethod http://127.0.0.1:8080/api/v1/documents/{id}/text
Invoke-RestMethod -Method Delete http://127.0.0.1:8080/api/v1/documents/{id}
```

The sandboxed Vite build could not replace existing generated `dist` files (`EPERM`). The identical build was rerun with approved host access and passed. The first multipart attempt used the unsupported Windows PowerShell `-Form` option; the successful retry used `curl.exe`.

## Automated test results

- Full backend suite: 52 tests passed; 0 failures, errors, or skips.
- Flyway validated and applied V1 through V4 in H2 PostgreSQL compatibility mode.
- Phase 5 backend coverage includes PDF/TXT/Markdown upload, extraction, metadata/text reads, newest-first listing, validation, physical-file behavior, and deletion.
- Frontend: 19 tests passed, including upload validation, successful upload display, extracted-text viewing, safe error handling, and confirmed deletion.
- Both TypeScript configurations passed through `npm.cmd run build`.
- Vite production build passed: 20 modules transformed.

## Manual test steps

1. Applied the document migration and ran the application with its local PostgreSQL database and configured local upload directory.
2. Uploaded supported synthetic documents through the application.
3. Confirmed document metadata persistence and successful text extraction.
4. Viewed extracted text through the document UI/API.
5. Deleted documents and confirmed metadata and locally stored files were removed safely.

## Results and evidence

- Both uploads returned `extractionStatus=COMPLETED` and `fileAvailable=true` (260 and 272 bytes).
- Extracted text length matched the first synthetic file and contained the expected marker.
- The API returned document metadata without exposing an internal path.
- After deletion, the deleted document ID was absent.
- Repository-owner manual verification records successful upload, PostgreSQL metadata persistence, extraction, extracted-text viewing, and safe document deletion through the application.
- The current verification pass independently reconfirmed the HTTP workflow with synthetic local files and the complete automated regression suite.
- Commit `f4a2739` introduced the Phase 5 implementation, API, migration, tests, UI, and documentation.

## Known limitations

- Scanned/image-only PDFs require OCR and are rejected when no text can be extracted.
- DOCX is intentionally unsupported.
- OCR, DOCX, cloud storage, and document summarization remain outside Phase 5 scope.

## Final phase status

`VERIFIED`. Automated tests/builds pass, and completed manual verification covers upload, PostgreSQL metadata persistence, text extraction and viewing, local-file handling, and safe deletion.
