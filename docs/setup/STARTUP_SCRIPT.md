# MuniAI Windows startup script

Use `scripts/start-muniai.ps1` after restarting Windows. It reuses the
software and models already installed on the machine; it does not install or
download anything.

The script checks and starts these components in dependency order:

1. PostgreSQL Windows service
2. Docker Desktop and the existing Qdrant container
3. Ollama and the existing `llama3.2:3b` and `nomic-embed-text:latest` models
4. Spring Boot backend
5. Vite/React frontend

## Start MuniAI

Open PowerShell in the repository root and run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-muniai.ps1
```

If `MUNIAI_DB_PASSWORD` is not already present in the current environment,
the script prompts for it securely. The password is inherited by the backend
process but is not written to a file or printed in a log.

The application is available at <http://127.0.0.1:5173>. Process output is
written below `local-data/runtime`, which is excluded from Git.

## Useful options

Check health without changing anything:

```powershell
.\scripts\start-muniai.ps1 -CheckOnly
```

Restart Ollama, the backend, and the frontend while leaving PostgreSQL and
Qdrant running:

```powershell
.\scripts\start-muniai.ps1 -RestartAppProcesses
```

The script intentionally does not delete or recreate Qdrant collections.

## One-time requirements

- PostgreSQL must already have a configured `muniai` database and user.
- Docker Desktop and the pinned Qdrant image must already be installed.
- Ollama and both required models must already exist locally.
- Java, Maven, Node.js, and npm must be available on `PATH`.
- Frontend dependencies must already be installed with `npm install`.

Starting a stopped PostgreSQL Windows service may require running PowerShell
as Administrator.
