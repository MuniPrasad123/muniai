# Contributing

## Workflow

1. Confirm the active phase and read its architecture documents and ADRs.
2. Keep changes small, phase-scoped, and free of unrelated refactoring.
3. Use synthetic fixtures; never add credentials or personal data.
4. Add or update verification appropriate to the change.
5. Update documentation and implementation status based on actual evidence.

## Change quality

- Follow `.editorconfig` and existing conventions.
- Prefer clear module boundaries and dependency direction.
- Document a meaningful design reversal or new constraint in an ADR.
- Include security and privacy effects in change descriptions.
- Do not commit generated runtime state, local databases, uploaded files, model artifacts, or `.env` files.

## Definition of done

A contribution is done when its requested behavior and documentation are complete, relevant verification passes, no secrets or private data are present, and known limitations are reported.

