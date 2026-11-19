// Focused MuniAI portfolio update. Preserves unrelated portfolio content.
const fs = require('node:fs')
const path = require('node:path')

const root = path.resolve(process.argv[2] || '')
const expected = path.resolve('D:/Workspace/portfolio/muni-prasad-portfolio')
if (root !== expected) throw new Error(`Unexpected portfolio target: ${root}`)

const pending = new Map()
function replace(file, before, after) {
  const target = path.join(root, file)
  const content = pending.get(target) ?? fs.readFileSync(target, 'utf8')
  if (content.includes(after)) return
  if (!content.includes(before)) throw new Error(`Expected content missing: ${file}`)
  pending.set(target, content.replace(before, after))
}

replace('src/data/site.ts',
  "MuniAI: { slug: 'muniai', category: 'Applied AI', diagram: 'local-chat', approach: 'Connected a React and TypeScript chat interface with a Java 21 / Spring Boot backend and Ollama for local model inference.' }",
  "MuniAI: { slug: 'muniai', category: 'Applied AI', diagram: 'local-chat', approach: 'Built a local-first assistant with Spring Boot, React, PostgreSQL conversation and memory persistence, Ollama inference and embeddings, Qdrant retrieval, grounded citations, and explicit memory approval.' }")

replace('src/data/profile.ts',
  'My independent AI work explores retrieval-augmented generation (RAG), document-grounded answers, and in-memory conversation context. MuniAI connects a Java and Spring Boot backend with React and Ollama for local-first chat, while DharmaAI combines a custom local RAG pipeline, Ollama models, and browser voice features for a book knowledge assistant.',
  'My independent AI work explores local retrieval-augmented generation, inspectable evidence, and user-controlled personalization. MuniAI combines Spring Boot, React, PostgreSQL, Ollama, and Qdrant for local chat, document-grounded answers, citations, and approval-gated long-term memory. DharmaAI combines a custom local RAG pipeline, Ollama models, and browser voice features for a book knowledge assistant.')

replace('src/data/profile.ts',
  "{ title: 'MuniAI', category: 'Independent application', problemStatement: 'Explore a local-first chat experience with a Java backend and a modern web interface.', role: 'Built a chat application with Java 21 and Spring Boot, a React and TypeScript interface, and Ollama for local model inference.', outcome: 'Connected a local model runtime to a full-stack chat application.', techStack: ['Java 21', 'Spring Boot', 'React', 'TypeScript', 'Ollama'], image: '/assets/project-admin.svg' }",
  "{ title: 'MuniAI', category: 'Independent application', problemStatement: 'Keep private chat, document knowledge, and approved personal memory useful without sending them to a hosted model API.', role: 'Designed and built the Java 21 / Spring Boot modular monolith, React and TypeScript interface, PostgreSQL persistence, secure document pipeline, Ollama chat and embeddings, Qdrant retrieval, citation-backed RAG, and explicit memory lifecycle.', outcome: 'Recorded a real local session covering document indexing, semantic search, a grounded answer with two citations, and disclosed use of an approved memory. Current regression evidence includes 57 backend tests, 24 frontend tests, and a production build.', techStack: ['Java 21', 'Spring Boot', 'React', 'TypeScript', 'PostgreSQL', 'Ollama', 'Qdrant', 'RAG'], image: '/demo/muniai/muniai-answer.png' }")

replace('src/data/engineering.ts',
  "{ title: 'Frontend & independent AI applications', context: 'React and Angular complement backend delivery. Java 21, TypeScript, and Ollama are specific to MuniAI; retrieval and voice capabilities are documented in DharmaAI.', tools: ['React', 'Angular', 'JavaScript', 'TypeScript', 'HTML', 'CSS', 'Java 21 · MuniAI', 'Ollama', 'OpenAI APIs', 'File-based retrieval', 'RAG concepts', 'Speech-to-text', 'Text-to-speech', 'Prompt engineering'] }",
  "{ title: 'Frontend & independent AI applications', context: 'React and Angular complement backend delivery. MuniAI adds Java 21, PostgreSQL, Ollama, Qdrant, document-grounded RAG, citations, and controlled memory; retrieval and voice capabilities are also documented in DharmaAI.', tools: ['React', 'Angular', 'JavaScript', 'TypeScript', 'HTML', 'CSS', 'Java 21 · MuniAI', 'Spring Boot', 'PostgreSQL', 'Ollama', 'Qdrant', 'RAG', 'Embeddings', 'Prompt engineering', 'Speech-to-text', 'Text-to-speech'] }")

replace('src/data/engineering.ts',
  "muniai: { decisions: ['Use Java 21 and Spring Boot for the backend, with React and TypeScript for the interface.', 'Use Ollama for local inference in a local-first chat workflow.'], lesson: 'This project extends my backend work into a complete chat interface and local model integration. It is presented as an independent application.', evidence: 'Project details supplied by the owner. No repository, deployment, or automated test suite was available for independent review.' }",
  "muniai: { decisions: ['Keep conversation history, Qdrant document knowledge, and PostgreSQL personal memory as separate models.', 'Use Ollama locally for llama3.2:3b chat and nomic-embed-text embeddings, with Qdrant retrieval and persisted citation snapshots.', 'Require explicit approval before a memory becomes active; reject common secrets deterministically and disclose memory use per response.'], validation: 'A live local session on 19 November 2026 verified a synthetic document upload, two-chunk indexing, semantic search, a grounded answer with two citations, and disclosed use of one approved memory. The backend suite passed 57 tests; the serialized frontend suite passed 24 tests; the production build passed. Phase 8 remains implemented rather than fully verified while several native lifecycle and failure scenarios remain.', lesson: 'Local-first AI still needs operational recovery plans. The original Qdrant collection exposed a persisted payload-segment failure; the demonstration used a fresh isolated collection without deleting original data, and that recovery remains clearly documented as pending.', evidence: 'Reviewed the MuniAI repository and recorded a real local browser session using an original synthetic Markdown document. No production usage or performance figures are claimed.' }")

replace('src/app/(frontend)/portfolio/[slug]/page.tsx',
  "import { DharmaShowcase } from '@/components/site/DharmaShowcase'",
  "import { DharmaShowcase } from '@/components/site/DharmaShowcase'\nimport { MuniAiShowcase } from '@/components/site/MuniAiShowcase'")
replace('src/app/(frontend)/portfolio/[slug]/page.tsx',
  "{slug==='dharmaai'&&<DharmaShowcase/>}",
  "{slug==='muniai'&&<MuniAiShowcase/>}\n      {slug==='dharmaai'&&<DharmaShowcase/>}")
replace('src/app/(frontend)/portfolio/[slug]/page.tsx',
  ":slug==='dharmaai'?<p><a href=\"https://github.com/MuniPrasad123/dharmaai-voice-book-assistant\">Public source code and recorded demonstration</a> are available above.</p>",
  ":slug==='muniai'?<p><a href=\"https://github.com/MuniPrasad123/muniai\">Public source code and recorded demonstration</a> are available above.</p>:slug==='dharmaai'?<p><a href=\"https://github.com/MuniPrasad123/dharmaai-voice-book-assistant\">Public source code and recorded demonstration</a> are available above.</p>")

for (const [target, content] of pending) fs.writeFileSync(target, content)

fs.copyFileSync(path.join(__dirname, 'MuniAiShowcase.tsx'), path.join(root, 'src/components/site/MuniAiShowcase.tsx'))
const destination = path.join(root, 'public/demo/muniai')
fs.mkdirSync(destination, { recursive: true })
for (const [source, name] of [
  ['muniai-demo.mp4', 'muniai-demo.mp4'],
  ['muniai-demo.vtt', 'muniai-demo.vtt'],
  ['publish-frames/03-grounded-answer.png', 'muniai-answer.png'],
  ['publish-frames/04-retrieved-evidence.png', 'muniai-evidence.png'],
  ['publish-frames/06-memory-control.png', 'muniai-memory.png'],
  ['../../sample-data/muniai-demo-knowledge.md', 'muniai-demo-knowledge.md'],
]) fs.copyFileSync(path.join(__dirname, source), path.join(destination, name))

console.log('Updated /portfolio/muniai with reviewed claims and static demonstration assets.')
