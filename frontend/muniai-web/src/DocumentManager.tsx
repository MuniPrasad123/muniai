import { ChangeEvent, DragEvent, useCallback, useEffect, useState } from 'react'
import { documentApi, DocumentMetadata } from './api/documents'
import DocumentIndexPanel, { VectorSearchPanel } from './DocumentIndexPanel'

const MAX_SIZE = 10 * 1024 * 1024
const EXTENSIONS = ['.pdf', '.txt', '.md']

export default function DocumentManager() {
  const [documents, setDocuments] = useState<DocumentMetadata[]>([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [text, setText] = useState<{ name: string; value: string } | null>(null)

  const refresh = useCallback(async () => setDocuments(await documentApi.list()), [])
  useEffect(() => { void refresh().catch((e) => setError(messageOf(e))).finally(() => setLoading(false)) }, [refresh])

  async function upload(file?: File) {
    if (!file) return
    const validation = validate(file)
    if (validation) { setError(validation); setSuccess(''); return }
    setUploading(true); setError(''); setSuccess('')
    try {
      const created = await documentApi.upload(file)
      setSuccess(`${created.originalFileName} uploaded with status ${created.extractionStatus}.`)
      await refresh()
    } catch (caught) { setError(messageOf(caught)) }
    finally { setUploading(false) }
  }
  function select(event: ChangeEvent<HTMLInputElement>) { void upload(event.target.files?.[0]); event.target.value = '' }
  function drop(event: DragEvent) { event.preventDefault(); void upload(event.dataTransfer.files[0]) }
  async function view(document: DocumentMetadata) {
    setError('')
    try { setText({ name: document.originalFileName, value: (await documentApi.text(document.id)).text }) }
    catch (caught) { setError(messageOf(caught)) }
  }
  async function remove(document: DocumentMetadata) {
    if (!window.confirm(`Delete “${document.originalFileName}” and its stored file?`)) return
    setError('')
    try { await documentApi.remove(document.id); if (text?.name === document.originalFileName) setText(null); await refresh() }
    catch (caught) { setError(messageOf(caught)) }
  }

  return <section className="documents" aria-labelledby="documents-title">
    <header><p className="eyebrow">PRIVATE · LOCAL STORAGE</p><h1 id="documents-title">Documents</h1>
      <p>Upload PDF, UTF-8 text, or Markdown files. Native text extraction only—no OCR or document chat.</p></header>
    <label className={`drop-zone ${uploading ? 'busy' : ''}`} onDragOver={(event) => event.preventDefault()} onDrop={drop}>
      <input aria-label="Choose a document" type="file" accept=".pdf,.txt,.md,application/pdf,text/plain,text/markdown" disabled={uploading} onChange={select} />
      <strong>{uploading ? 'Uploading and extracting…' : 'Choose a document or drop it here'}</strong>
      <span>PDF, TXT, or Markdown · maximum 10 MB</span>
    </label>
    {error && <div className="error" role="alert">{error}</div>}
    {success && <div className="success" role="status">{success}</div>}
    <div className="document-list" aria-busy={loading || uploading}>
      <h2>Uploaded documents</h2>
      {loading ? <p>Loading documents…</p> : documents.length === 0 ? <p className="empty-documents">No documents uploaded yet.</p>
        : documents.map((document) => <article className="document-row" key={document.id}>
          <div><strong>{document.originalFileName}</strong><span>{document.contentType} · {formatSize(document.fileSize)} · {new Date(document.createdAt).toLocaleString()}</span></div>
          <span className={`status ${document.extractionStatus.toLowerCase()}`}>{document.extractionStatus}</span>
          <button disabled={document.extractionStatus !== 'COMPLETED'} onClick={() => view(document)}>View text</button>
          <button className="danger" onClick={() => remove(document)}>Delete</button>
          <DocumentIndexPanel document={document} documents={documents} onChanged={refresh} onError={setError} />
          {document.extractionError && <p className="extraction-error">{document.extractionError}</p>}
          {document.indexingError && <p className="extraction-error">{document.indexingError}</p>}
          {!document.fileAvailable && <p className="extraction-error">The original stored file is missing.</p>}
        </article>)}
    </div>
    <VectorSearchPanel documents={documents} onError={setError} />
    {text && <section className="text-view"><div><h2>{text.name}</h2><button onClick={() => setText(null)}>Close</button></div><pre>{text.value}</pre></section>}
  </section>
}

function validate(file: File) {
  const lower = file.name.toLowerCase()
  if (!EXTENSIONS.some((extension) => lower.endsWith(extension))) return 'Choose a PDF, TXT, or Markdown file.'
  if (file.size === 0) return 'Choose a non-empty document.'
  if (file.size > MAX_SIZE) return 'The document exceeds the 10 MB maximum.'
  return ''
}
function formatSize(bytes: number) { return bytes < 1024 ? `${bytes} B` : bytes < 1024 * 1024 ? `${(bytes / 1024).toFixed(1)} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB` }
function messageOf(value: unknown) { return value instanceof Error ? value.message : 'MuniAI could not complete the document request.' }
