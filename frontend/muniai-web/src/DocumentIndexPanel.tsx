import { FormEvent, useState } from 'react'
import { documentApi, DocumentMetadata, VectorSearchResponse } from './api/documents'

type Props = { document: DocumentMetadata; documents: DocumentMetadata[]; onChanged: () => Promise<unknown>; onError: (value: string) => void }

export default function DocumentIndexPanel({ document, onChanged, onError }: Props) {
  const [busy, setBusy] = useState(false)
  const indexingStatus = document.indexingStatus ?? 'NOT_INDEXED'
  async function run(mode: 'index' | 'reindex' | 'remove') {
    if (mode === 'reindex' && !window.confirm(`Re-index “${document.originalFileName}”? Existing vectors will be replaced.`)) return
    if (mode === 'remove' && !window.confirm(`Remove the vector index for “${document.originalFileName}”? The upload remains.`)) return
    setBusy(true); onError('')
    try {
      if (mode === 'index') await documentApi.index(document.id)
      else if (mode === 'reindex') await documentApi.reindex(document.id)
      else await documentApi.removeIndex(document.id)
      await onChanged()
    } catch (value) { onError(messageOf(value)); await onChanged().catch(() => undefined) }
    finally { setBusy(false) }
  }
  return <div className="index-details">
    <span className={`status ${indexingStatus.toLowerCase()}`}>Index: {indexingStatus}</span>
    {document.chunkCount > 0 && <span>{document.chunkCount} chunks · {document.embeddingModel}</span>}
    {indexingStatus === 'NOT_INDEXED' && <button disabled={busy || document.extractionStatus !== 'COMPLETED'} onClick={() => run('index')}>Index</button>}
    {indexingStatus === 'FAILED' && <button disabled={busy} onClick={() => run('reindex')}>Retry</button>}
    {indexingStatus === 'COMPLETED' && <>
      <button disabled={busy} onClick={() => run('reindex')}>Re-index</button>
      <button disabled={busy} onClick={() => run('remove')}>Remove index</button>
    </>}
    {busy && <span role="status">Indexing locally…</span>}
  </div>
}

export function VectorSearchPanel({ documents, onError }: { documents: DocumentMetadata[]; onError: (value: string) => void }) {
  const [query,setQuery]=useState(''); const [selected,setSelected]=useState('')
  const [busy,setBusy]=useState(false); const [response,setResponse]=useState<VectorSearchResponse|null>(null)
  async function submit(event:FormEvent){
    event.preventDefault();if(!query.trim())return;setBusy(true);onError('')
    try{setResponse(await documentApi.search(query.trim(),5,selected||undefined))}
    catch(value){onError(messageOf(value))}finally{setBusy(false)}
  }
  return <section className="vector-search"><h2>Vector search diagnostic</h2>
    <p>Similarity test only. Matching chunks are not sent to the chat model.</p>
    <form onSubmit={submit}>
      <input aria-label="Vector search query" value={query} onChange={event=>setQuery(event.target.value)} placeholder="Enter a test query" maxLength={2000}/>
      <select aria-label="Search document" value={selected} onChange={event=>setSelected(event.target.value)}>
        <option value="">All indexed documents</option>
        {documents.filter(item=>item.indexingStatus==='COMPLETED').map(item=><option key={item.id} value={item.id}>{item.originalFileName}</option>)}
      </select>
      <button disabled={busy||!query.trim()}>{busy?'Searching…':'Search vectors'}</button>
    </form>
    {response&&<div className="search-results">{response.results.length===0?<p>No matching chunks.</p>:response.results.map(result=>
      <article key={result.chunkId}><strong>{result.originalFileName} · chunk {result.chunkIndex+1}</strong>
        <span>Score {result.score.toFixed(4)}</span><p>{result.contentPreview}</p></article>)}</div>}
  </section>
}
function messageOf(value:unknown){return value instanceof Error?value.message:'The local indexing request failed.'}
