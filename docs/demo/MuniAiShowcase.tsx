import Image from 'next/image'

export function MuniAiShowcase() {
  return <section className="detail-block" aria-labelledby="muniai-demo-title">
    <div className="eyebrow">Applied AI · Working local demonstration</div>
    <h2 id="muniai-demo-title">Local RAG and user-controlled memory you can inspect</h2>
    <p>Watch a real local session: approve a preference, index a synthetic document, run semantic retrieval, inspect citations, and see which approved memory influenced the answer.</p>
    <video controls playsInline preload="metadata" poster="/demo/muniai/muniai-answer.png" style={{width:'100%',borderRadius:14,background:'#173f32'}} aria-label="MuniAI local document and memory demonstration">
      <source src="/demo/muniai/muniai-demo.mp4" type="video/mp4" />
      <track kind="captions" src="/demo/muniai/muniai-demo.vtt" srcLang="en" label="English scene captions" default />
      Your browser does not support video. <a href="/demo/muniai/muniai-demo.mp4">Download the demonstration</a>.
    </video>
    <p style={{fontSize:14}}>Edited 1280×720 browser capture from 19 November 2026. The document is an original synthetic project summary. The video has scene captions and no narration. Observed timings are not performance benchmarks.</p>
    <p><a className="text-link" href="https://github.com/MuniPrasad123/muniai" target="_blank" rel="noreferrer">Explore the source code ↗</a>{' · '}<a className="text-link" href="/demo/muniai/muniai-demo-knowledge.md">Read the synthetic demo document ↗</a></p>

    <h3>What the session demonstrates</h3>
    <ul className="case-decisions">
      <li>An explicit memory request remains approval-gated before it can influence another conversation.</li>
      <li>Ollama generated local 768-dimensional embeddings and Qdrant returned inspectable semantic matches.</li>
      <li>Ask Documents produced a grounded answer with two persisted citations and visible retrieved passages.</li>
      <li>The response separately disclosed the approved memory used for personalization.</li>
      <li>The Memory page exposed source, dates, usage count, edit, disable, and delete controls.</li>
    </ul>

    <figure style={{margin:'24px 0'}}>
      <Image src="/demo/muniai/muniai-evidence.png" alt="MuniAI displaying retrieved passages and similarity scores for a grounded answer" width={1265} height={712} style={{width:'100%',height:'auto',borderRadius:14}} />
      <figcaption>Document evidence and memory influence are disclosed separately.</figcaption>
    </figure>

    <h3>Completed</h3>
    <p>Local chat, PostgreSQL history, secure document handling, local embeddings, Qdrant indexing and search, citation-backed RAG, no-context handling, and controlled long-term memory are implemented. The current regression evidence is 57 passing backend tests, 24 passing frontend tests, and a successful production build.</p>

    <h3>Pending and operational boundaries</h3>
    <p>Phase 8 remains implemented rather than fully verified until disabled/deleted-memory exclusion, restart persistence, and memory-retrieval failure fallback are recorded in the native stack. The original Qdrant collection also needs a controlled rebuild after a persisted payload segment failed during this session; the demo used an isolated collection without deleting the original data. Phase 9 and external integrations are not started.</p>
    <p>The static recording remains available when the optional local stack is offline. The live application is intentionally loopback-only and should not expose PostgreSQL, Qdrant, Ollama, or its API directly to the internet.</p>
  </section>
}
