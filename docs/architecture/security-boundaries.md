# Security boundaries

## Boundary model

| Boundary | Trust level | Required controls |
|---|---|---|
| Browser UI | Partially trusted | Output encoding, CSRF/CORS policy, no durable secrets |
| Backend policy boundary | Trusted enforcement point | Authentication, authorization, validation, approval checks, audit |
| Model runtime and output | Untrusted decision input | Prompt bounds, output validation, no direct tool authority |
| Uploaded/imported content | Untrusted | Type and size limits, safe parsing, isolation, provenance |
| Local stores | Sensitive | Loopback binding, credentials, least access, retention, backups |
| Connector providers | External | OAuth scopes, TLS, revocation, freshness and error handling |
| Local filesystem/backups | Sensitive | Restricted permissions, encryption where practical, tested restore |

## Prompt injection

Text from documents, email, repositories, websites, and model responses is data, not policy. It cannot override system rules, expand connector scopes, retrieve unrelated private context, or approve actions. Tool requests must be authorized from structured application state rather than natural-language claims.

## Secrets

Tokens and passwords belong in protected runtime configuration or OS credential storage. They must be redacted from errors, logs, prompts, exports, and audit payloads. `.env` is ignored and is suitable only for local development, not as a long-term secret store.

## Approval integrity

Approval binds the user, exact operation, target, material payload, and expiration. Any material change invalidates prior approval. Retries must be idempotent where possible, and denial or timeout must leave external state unchanged.

## Local threat assumptions

Local-first reduces data exposure but does not defend against a fully compromised host. Disk protection, OS patching, account security, filesystem permissions, and encrypted backups remain user responsibilities.

