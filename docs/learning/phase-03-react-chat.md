# Phase 3: React local chat

The frontend is a Vite, React, and TypeScript single-page interface. It consumes the versioned Phase 2 chat endpoint through a loopback development proxy. The backend remains the enforcement boundary; UI limits exist only for immediate feedback.

Conversation state is intentionally ephemeral and Clear chat removes it. Persistence belongs to Phase 4. Model output is rendered by React as text, not injected HTML.

The interface includes loading and safe error states, correlation references, provider metadata, keyboard submission, focus styles, screen-reader labels, reduced-motion handling, and responsive layout. It makes no direct Ollama request and requires no permissive CORS configuration.
