# MuniAI: private local chat, document intelligence, and controlled memory

MuniAI is a local-first personal AI assistant built as a Java 21 / Spring Boot modular monolith with a React and TypeScript interface. PostgreSQL stores conversations, documents, citations, and approved memories. Ollama runs chat and embedding models locally. Qdrant stores document vectors for semantic retrieval.

## Recorded demonstration

The edited browser capture uses the original synthetic file `sample-data/muniai-demo-knowledge.md`. It demonstrates actual local application state rather than a UI mock:

1. an explicit memory request creates an approval-gated proposal;
2. the synthetic Markdown document is extracted and indexed into two chunks;
3. `nomic-embed-text:latest` generates 768-dimensional local embeddings;
4. Qdrant semantic search returns two inspectable matches;
5. Ask Documents calls `llama3.2:3b` and returns a grounded answer with two persisted citations;
6. the retrieved passages and similarity scores are visible;
7. the response separately discloses the approved memory that influenced it; and
8. the Memory page shows the active value, source, creation date, last-used date, usage count, and lifecycle controls.

The MP4 is an edited sequence of verified browser captures with burned-in scene labels and a WebVTT caption file. It has no narration and does not claim benchmark performance.

## Evidence from 19 November 2026

| Check | Observed result |
| --- | --- |
| Full local stack | PostgreSQL 17, Docker/Qdrant 1.15.4, Ollama, Spring Boot, and Vite started locally |
| Flyway | Native PostgreSQL schema validated at version 5 |
| Document upload/extraction | Synthetic Markdown accepted; 1,593 bytes; extraction `COMPLETED` |
| Indexing | Two chunks, 768 dimensions, `nomic-embed-text:latest`, isolated collection `muniai_demo_20261119` |
| Semantic diagnostic | Two matches; top observed score 0.5783 for the approval question |
| Controlled memory | `preferred_explanation_style=beginner-friendly` explicitly proposed and approved |
| RAG answer | Relevant context found; local `llama3.2:3b` answer returned |
| Citations | Two citation/evidence records displayed from the synthetic document |
| Memory transparency | One approved memory disclosed; usage count updated to 1 |
| Backend regression | 57 tests passed; 0 failures, errors, or skips |
| Frontend regression | 24 tests passed using one worker; 0 failures |
| Production build | TypeScript checks and Vite build passed; 22 modules transformed |

These are demonstration observations from one local machine, not performance or production reliability guarantees.

## Completed

- Repository, architecture, security boundaries, and local Ollama integration.
- React chat UI with PostgreSQL conversation persistence.
- Secure document upload, metadata, text extraction, viewing, and deletion.
- Chunking, local embeddings, Qdrant indexing, re-indexing, removal, and semantic diagnostics.
- Normal Chat and Ask Documents with bounded retrieval, grounded responses, citation alignment, persisted citations, multiple-document support, and no-context behavior.
- Controlled long-term memory with proposal, explicit approval, edit, reject, disable, re-enable, supersede, delete, deterministic selection, sensitive-data rejection, prompt isolation, and usage disclosure.
- Reusable Windows startup/health-check script.
- Automated backend/frontend tests, architecture/security/setup/learning documentation, and this static recorded demonstration.

## Pending

- Complete and record the remaining Phase 8 native acceptance scenarios: disabled-memory exclusion, deleted-memory exclusion, application restart persistence, memory-retrieval failure fallback, and a dedicated sensitive-value browser demonstration.
- Recover or rebuild the original `muniai_document_chunks` Qdrant collection. On 19 November 2026 its persisted payload segment panicked during filtered deletion with `OutputTooSmall`; the demo used a new isolated collection without deleting the original data.
- Improve the startup script's Docker-engine recovery and Ollama restart behavior; the current script correctly reports failures but one restart attempt could not terminate the tray-managed Ollama process.
- Investigate frontend test timing under concurrent CPU-heavy backend/build work. The suite timed out when run concurrently but passed 24/24 with `--maxWorkers=1` after the backend suite completed.
- Phase 9 approval/audit framework and Phases 10–16 integrations remain not started.
- OCR, streaming output, reranking/hybrid retrieval, page-aware citations, authentication, multi-user isolation, packaged deployment, and physical memory-retention policies remain future work.

## Operational note

The recorded demo is static and can be viewed without running the local model stack. A live session still requires PostgreSQL, Docker/Qdrant, Ollama, the Spring Boot backend, and the Vite frontend. Do not expose PostgreSQL, Qdrant, Ollama, or the local API directly to the public internet.

## Assets

- `docs/demo/muniai-demo.mp4` — 1280×720 edited browser demonstration.
- `docs/demo/muniai-demo.vtt` — accessible scene captions.
- `docs/demo/video-check.jpg` — extracted visual QA frame.
- `docs/demo/publish-frames/` — sanitized browser evidence captures containing only synthetic demo data.
- Raw local captures and intermediate rendered frames remain gitignored because they can contain unrelated local record names.
- `docs/demo/prepare-video.py` — reproducible local video build script.
- `sample-data/muniai-demo-knowledge.md` — original synthetic demo document.
