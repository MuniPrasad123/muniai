# Security policy and design baseline

## Security goals

MuniAI protects confidentiality and user agency. Private content should remain on the user's device by default, access should be least-privilege, and every external side effect should be understandable and intentional.

## Data handling

- Store runtime data only under ignored, access-controlled local paths such as `local-data/`.
- Never commit secrets or real personal data. `.env.example` contains names and safe placeholders only.
- Minimize collected data and define retention and deletion behavior before persistence is added.
- Encrypt sensitive data at rest where practical and use protected OS credential storage for secrets.
- Avoid placing prompt content, document text, credentials, or personal data in logs.
- Backups must be explicit, encrypted, restorable, and subject to the same retention rules.

## Trust boundaries

The browser, backend, local model runtime, data stores, imported files, and external connectors are distinct trust zones. Bind local services to loopback by default, authenticate sensitive APIs, validate all untrusted input, and deny cross-origin access unless explicitly required.

## Actions and connectors

- Start connectors with the narrowest read-only scopes.
- Require preview and explicit approval for draft creation or any external mutation.
- LinkedIn publication remains manual outside MuniAI.
- Record approvals and consequential operations without logging sensitive payloads.
- Treat model output as untrusted data, never as authorization.

## Vulnerability reporting

This is a private repository. Report suspected vulnerabilities privately to the repository owner; do not open a public issue containing exploit details, credentials, or user data. Rotate exposed credentials immediately and preserve minimal evidence needed for investigation.

