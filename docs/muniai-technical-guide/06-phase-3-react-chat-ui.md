# Phase 3 — React chat interface

## Goal

Add a browser-based chat experience that can send messages to the Spring Boot backend and display the responses.

## Frontend structure

- [frontend/muniai-web/src/App.tsx](../../frontend/muniai-web/src/App.tsx) — main shell with conversation state and message compose behavior.
- [frontend/muniai-web/src/DocumentManager.tsx](../../frontend/muniai-web/src/DocumentManager.tsx) — upload and management of documents.
- [frontend/muniai-web/src/DocumentIndexPanel.tsx](../../frontend/muniai-web/src/DocumentIndexPanel.tsx) — indexing and vector search controls.
- [frontend/muniai-web/src/api/conversations.ts](../../frontend/muniai-web/src/api/conversations.ts) — conversation API client.
- [frontend/muniai-web/src/api/documents.ts](../../frontend/muniai-web/src/api/documents.ts) — document and vector-search API client.

## User flow

User types message -> frontend validation -> fetch POST to /api/v1/conversations/{id}/messages -> UI updates with the assistant response.

## Frontend-to-backend contract

The frontend calls the backend over the Vite proxy configured in [frontend/muniai-web/vite.config.ts](../../frontend/muniai-web/vite.config.ts). The proxy forwards /api and /actuator to http://127.0.0.1:8080.
