# Backend setup

## Requirements

- Java 21
- Maven 3.9+
- Ollama on `127.0.0.1:11434`
- Local model `llama3.2:3b`

The backend binds to `127.0.0.1:8080` and does not persist conversations.

## Configuration

| Variable | Default |
|---|---|
| `OLLAMA_BASE_URL` | `http://127.0.0.1:11434` |
| `MUNIAI_CHAT_MODEL` | `llama3.2:3b` |
| `OLLAMA_TIMEOUT_SECONDS` | `300` |
| `MUNIAI_CHAT_MAX_MESSAGE_LENGTH` | `10000` |

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
