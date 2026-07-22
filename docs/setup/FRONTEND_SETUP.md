# Frontend setup

## Requirements

- Node.js 22 or newer and npm
- The Phase 2 backend and local Ollama runtime

The development server binds to `127.0.0.1:5173` and proxies `/api` and `/actuator` to the loopback backend on port 8080. This avoids enabling cross-origin backend access.

## Install and verify

```powershell
cd frontend/muniai-web
npm install
npm test
npm run build
```

Dependencies are locked in `package-lock.json`; generated dependencies and build output are ignored.

## Run locally

Start Ollama and the backend, then in another terminal:

```powershell
cd frontend/muniai-web
npm run dev
```

Open `http://127.0.0.1:5173`. Stop both application processes with `Ctrl+C`.

Messages exist only in React memory for the current tab. Refreshing, closing, or clearing the tab removes them. Phase 3 uses no cookies, `localStorage`, IndexedDB, or conversation database.
