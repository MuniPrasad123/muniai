# Local environment setup

## 1. Install the required tools

Install Java 21, Maven, Node.js, npm, Docker Desktop, PostgreSQL, and Ollama. On Windows, the startup script expects PostgreSQL to be installed as a Windows service and Docker Desktop to be available.

## 2. Configure PostgreSQL

The backend uses the datasource values in [backend/muniai-api/src/main/resources/application.yml](../../backend/muniai-api/src/main/resources/application.yml). Create a local database and user if needed.

## 3. Start Ollama and pull models

The implementation expects the chat model and embedding model to be available locally.

```powershell
ollama serve
ollama pull llama3.2:3b
ollama pull nomic-embed-text:latest
ollama list
```

## 4. Start Qdrant

The compose file starts Qdrant with a local storage mount.

```powershell
docker compose -f .\infrastructure\qdrant-compose.yml up -d
```

## 5. Build and run the backend

```powershell
cd .\backend\muniai-api
mvn -DskipTests package
java -jar .\target\muniai-api-0.0.1-SNAPSHOT.jar
```

## 6. Start the frontend

```powershell
cd .\frontend\muniai-web
npm install
npm run dev
```

## 7. Verify the UI and APIs

Open http://127.0.0.1:5173. Confirm the backend health endpoint at http://127.0.0.1:8080/api/v1/system/health.
