# ADR-006: Human approval for consequential actions

- Status: Accepted
- Date: 2026-07-21

## Context

Models can misunderstand intent or be influenced by untrusted content. External mutations such as creating drafts or modifying provider state can have privacy and reputation consequences.

## Decision

Require explicit, time-bounded human approval for consequential actions. Approval is bound to the exact action, target, and material payload. Model text, imported content, schedules, and prior approvals cannot approve a changed action.

## Consequences

User agency and auditability improve, with additional interaction cost. The backend must enforce approvals, handle expiry and idempotency, and record outcomes without sensitive payload leakage.

