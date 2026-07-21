# Repository instructions for coding agents

## Product principles

- MuniAI is private and local-first. Prefer on-device processing and local storage.
- Treat prompts, documents, memories, credentials, connector data, and audit events as sensitive.
- Preserve explicit human control over external or irreversible actions.
- Build a modular monolith unless an accepted ADR changes that direction.

## Working rules

- Read `IMPLEMENTATION_PLAN.md`, `IMPLEMENTATION_STATUS.md`, `SECURITY.md`, and relevant ADRs before changing behavior.
- Work only within the requested phase; do not pre-build later phases.
- Never commit secrets, tokens, private user data, generated databases, model files, or local runtime state.
- Keep examples synthetic and safe to publish.
- Update implementation status only after evidence-based verification.
- Record consequential architectural changes as ADRs.
- Do not weaken approval, audit, authentication, authorization, or data-boundary controls for convenience.

## Verification

- Run the narrowest relevant checks first, then broader checks if available.
- Report what was run, what passed, and what could not be verified.
- Do not claim `VERIFIED` based only on file creation or unexecuted tests.

