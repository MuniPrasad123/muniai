# ADR-007: OAuth for external connectors

- Status: Accepted
- Date: 2026-07-21

## Context

Future GitHub and Gmail capabilities require delegated provider access. Collecting account passwords or long-lived broadly scoped credentials would create unacceptable risk.

## Decision

Use provider-supported OAuth authorization with the smallest capability-specific scopes, beginning read-only. Store tokens using protected local credential facilities, support revocation and reauthorization, and never expose tokens to models or the frontend.

## Consequences

Users gain provider-controlled consent and revocation. Connector setup, refresh handling, scope migrations, and secure token storage add complexity. Each new scope requires explicit review and user consent.

