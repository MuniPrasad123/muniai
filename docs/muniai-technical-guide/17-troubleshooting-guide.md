# Troubleshooting guide

## Symptom: Ollama model missing

Likely cause: the local model was not pulled.

Resolution: run ollama pull for the required model.

## Symptom: backend connection refused

Likely cause: the Spring Boot process is not running or the port is blocked.

Resolution: verify the Java process and the health endpoint.

## Symptom: React cannot reach the backend

Likely cause: the Vite dev server proxy is wrong or port 8080 is not listening.

Resolution: confirm the proxy rules in [frontend/muniai-web/vite.config.ts](../../frontend/muniai-web/vite.config.ts) and verify the backend health endpoint.
