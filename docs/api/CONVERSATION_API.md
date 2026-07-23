# Conversation API

Base URL: `http://127.0.0.1:8080/api/v1/conversations`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/` | Create an empty conversation |
| `GET` | `/` | List newest-updated conversations first |
| `GET` | `/{id}` | Get metadata and chronological messages |
| `PATCH` | `/{id}` | Rename with `{ "title": "New title" }` |
| `POST` | `/{id}/messages` | Send with `{ "message": "Hello" }` |
| `DELETE` | `/{id}` | Delete one conversation and its messages |
| `DELETE` | `/` | Delete all conversations and messages |

Identifiers are UUIDs. Roles are `USER`, `ASSISTANT`, and `SYSTEM`. Message content is required and limited by `MUNIAI_CHAT_MAX_MESSAGE_LENGTH` (default 10,000); titles are required for rename and limited to 200 characters.

Errors use the safe JSON envelope containing `status`, `message`, and `correlationId`. If Ollama fails, the saved user message remains and no assistant message is created.
