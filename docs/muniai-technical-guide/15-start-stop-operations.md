# Start, stop, and operations guide

## Start order

1. Start PostgreSQL.
2. Start Docker Desktop.
3. Start Qdrant.
4. Start Ollama.
5. Start the backend.
6. Start the frontend.

## PowerShell commands

```powershell
./scripts/verification/verify-environment.ps1
./scripts/start-muniai.ps1
```

## Stop workflow

- Stop React with Ctrl+C in the terminal where npm run dev is running.
- Stop the Spring Boot process with Ctrl+C in its terminal.
- Stop Qdrant with Docker Compose.
- Stop Ollama if no longer needed.
