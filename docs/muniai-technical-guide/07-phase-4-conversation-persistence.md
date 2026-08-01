# Phase 4 — conversation persistence

## Goal

Persist chat history in PostgreSQL so users can reopen and rename conversations after refreshes or restarts.

## Database changes

- V1 creates conversations and messages.
- V4 adds the chat_mode column and the message_citations table.

## Main classes

- [backend/muniai-api/src/main/java/com/muniai/conversation/api/ConversationController.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/api/ConversationController.java)
- [backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationPersistenceService.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/application/ConversationPersistenceService.java)
- [backend/muniai-api/src/main/java/com/muniai/conversation/infrastructure/ConversationEntity.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/infrastructure/ConversationEntity.java)
- [backend/muniai-api/src/main/java/com/muniai/conversation/infrastructure/MessageEntity.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/infrastructure/MessageEntity.java)
- [backend/muniai-api/src/main/java/com/muniai/conversation/infrastructure/MessageCitationEntity.java](../../backend/muniai-api/src/main/java/com/muniai/conversation/infrastructure/MessageCitationEntity.java)

## Important behavior

- Creating a conversation creates a default title of New conversation.
- The first user message can rename the conversation title.
- User messages are saved first. If Ollama fails, the user message remains and the assistant message is not created.
- Assistant messages can include citations in DOCUMENT_RAG mode.
