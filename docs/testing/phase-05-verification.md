# Phase 5 verification

Date: 2026-07-23

## Automated evidence

- `cd backend/muniai-api; mvn test` — 26 tests passed with no failures or errors. Coverage includes synthetic PDF, TXT, and Markdown uploads; metadata and text persistence; ordering; validation; filename/path safety; failure retention; deletion and physical-file behavior; Flyway V1+V2; and existing chat/conversation regressions.
- `cd frontend/muniai-web; npm.cmd test -- --run` — 14 tests passed across conversation and document UI behavior.
- Both TypeScript configurations passed `tsc --noEmit`.
- `npm.cmd run build` produced the Vite production bundle.
- `git diff --check` passed, with only informational line-ending warnings.

PDF and TXT checks use small synthetic fixtures. No private documents are committed.

## Manual/native verification

A separately launched backend against native PostgreSQL and browser-driven upload were not completed because no usable live runtime configuration was confirmed. Phase 5 therefore remains `IMPLEMENTED`, not `VERIFIED`.

Before promotion:

1. Apply Flyway V2 to local PostgreSQL.
2. Upload one synthetic native-text PDF and one UTF-8 TXT through the browser.
3. Confirm metadata and text, then delete both and confirm physical files are gone.
4. Send and reload a chat message to reconfirm live Ollama and conversation persistence.
