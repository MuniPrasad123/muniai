# Backend setup

## Requirements

- Java 21
- Maven 3.9+
- Ollama on `127.0.0.1:11434`
- Local model `llama3.2:3b`
- PostgreSQL 17 (or a compatible supported PostgreSQL version)

The backend binds to `127.0.0.1:8080`. PostgreSQL persists conversations, document metadata, and extracted text; original document files remain in local filesystem storage.

## Configuration

| Variable | Default |
|---|---|
| `OLLAMA_BASE_URL` | `http://127.0.0.1:11434` |
| `MUNIAI_CHAT_MODEL` | `llama3.2:3b` |
| `OLLAMA_TIMEOUT_SECONDS` | `300` |
| `MUNIAI_CHAT_MAX_MESSAGE_LENGTH` | `10000` |
| `MUNIAI_DB_URL` | `jdbc:postgresql://127.0.0.1:5432/muniai` |
| `MUNIAI_DB_USERNAME` | `muniai` |
| `MUNIAI_DB_PASSWORD` | empty; set locally |
| `MUNIAI_DOCUMENT_UPLOAD_DIR` | `../../local-data/documents` |
| `MUNIAI_DOCUMENT_MAX_SIZE` | `10MB` |
| `MUNIAI_DOCUMENT_MAX_REQUEST_SIZE` | `11MB` (allows multipart overhead) |

## Build and run

```powershell
cd backend/muniai-api
mvn package
java -jar target/muniai-api-0.0.1-SNAPSHOT.jar
```

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
Invoke-RestMethod http://127.0.0.1:8080/api/v1/system/health
$body = @{ message = 'Explain Docker networking.' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/v1/chat -ContentType 'application/json' -Body $body
```

Stop with `Ctrl+C`. `X-Correlation-ID` is accepted and returned; a UUID is generated when absent. Errors use the standard API error shape without stack traces.

The upload directory is created automatically. Keep it under an ignored, access-controlled local path. Run `mvn test` for the complete backend suite; tests use H2 in PostgreSQL compatibility mode and a dedicated temporary upload directory.
