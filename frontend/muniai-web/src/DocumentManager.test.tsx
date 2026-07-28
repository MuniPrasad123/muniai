import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import DocumentManager from './DocumentManager'

afterEach(() => { cleanup(); vi.restoreAllMocks() })
const document = { id:'d1', originalFileName:'notes.txt', contentType:'text/plain', fileSize:12,
  extractionStatus:'COMPLETED', extractionError:null, pageCount:null, fileAvailable:true,
  indexingStatus:'NOT_INDEXED', indexingStartedAt:null, indexingCompletedAt:null, indexingError:null,
  chunkCount:0, embeddingModel:null, embeddingDimension:null, qdrantCollectionName:null,
  createdAt:'2026-07-23T08:00:00Z', updatedAt:'2026-07-23T08:00:00Z' }

describe('document management', () => {
  it('renders loading and empty states', async () => {
    mockApi(() => json([])); render(<DocumentManager />)
    expect(screen.getByText('Loading documents…')).toBeInTheDocument()
    expect(await screen.findByText('No documents uploaded yet.')).toBeInTheDocument()
  })
  it('validates file type and maximum size', async () => {
    const fetchMock = mockApi(() => json([])); render(<DocumentManager />); await screen.findByText('No documents uploaded yet.')
    fireEvent.change(screen.getByLabelText('Choose a document'), { target: { files: [new File(['x'],'image.png',{type:'image/png'})] } })
    expect(screen.getByRole('alert')).toHaveTextContent('PDF, TXT, or Markdown')
    fireEvent.change(screen.getByLabelText('Choose a document'), { target: { files: [new File([new Uint8Array(10*1024*1024+1)],'large.txt',{type:'text/plain'})] } })
    expect(screen.getByRole('alert')).toHaveTextContent('10 MB')
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })
  it('uploads a valid document and displays status', async () => {
    let uploaded = false
    mockApi(({ method }) => method === 'POST' ? (uploaded=true, json(document)) : json(uploaded ? [document] : []))
    render(<DocumentManager />); await screen.findByText('No documents uploaded yet.')
    await userEvent.upload(screen.getByLabelText('Choose a document'), new File(['hello'],'notes.txt',{type:'text/plain'}))
    expect(await screen.findByText('notes.txt')).toBeInTheDocument()
    expect(screen.getByText('COMPLETED')).toBeInTheDocument()
  })
  it('does not crash when a legacy backend omits indexing metadata', async () => {
    const { indexingStatus: _indexingStatus, ...legacyDocument } = document
    mockApi(() => json([legacyDocument]))
    render(<DocumentManager />)
    expect(await screen.findByText('Index: NOT_INDEXED')).toBeInTheDocument()
  })
  it('shows upload API errors', async () => {
    mockApi(({ method }) => method === 'POST' ? new Response(JSON.stringify({message:'Rejected.',correlationId:'ref-5'}),{status:400}) : json([]))
    render(<DocumentManager />); await screen.findByText('No documents uploaded yet.')
    await userEvent.upload(screen.getByLabelText('Choose a document'), new File(['hello'],'notes.txt',{type:'text/plain'}))
    expect(await screen.findByRole('alert')).toHaveTextContent('Reference: ref-5')
  })
  it('views extracted text', async () => {
    mockApi(({ path }) => path.endsWith('/text') ? json({id:'d1',text:'Extracted content'}) : json([document]))
    render(<DocumentManager />); await screen.findByText('notes.txt')
    await userEvent.click(screen.getByRole('button',{name:'View text'}))
    expect(await screen.findByText('Extracted content')).toBeInTheDocument()
  })
  it('requires confirmation and deletes', async () => {
    vi.spyOn(window,'confirm').mockReturnValue(true)
    const fetchMock = mockApi(({ method }) => method === 'DELETE' ? new Response(null,{status:204}) : json([document]))
    render(<DocumentManager />); await screen.findByText('notes.txt')
    await userEvent.click(screen.getByRole('button',{name:'Delete'}))
    await waitFor(() => expect(fetchMock.mock.calls.some(([,init]) => init?.method === 'DELETE')).toBe(true))
  })
  it('indexes and renders chunk metadata', async () => {
    let indexed=false
    mockApi(({method,path}) => method==='POST'&&path.endsWith('/index')
      ? (indexed=true,json({...document,indexingStatus:'COMPLETED',chunkCount:2,embeddingModel:'test-embed'}))
      : json([{...document,...(indexed?{indexingStatus:'COMPLETED',chunkCount:2,embeddingModel:'test-embed'}:{})}]))
    render(<DocumentManager/>);await screen.findByText('notes.txt')
    await userEvent.click(screen.getByRole('button',{name:'Index'}))
    expect(await screen.findByText(/2 chunks/)).toBeInTheDocument()
  })
  it('confirms re-index and remove-index actions', async () => {
    vi.spyOn(window,'confirm').mockReturnValue(false)
    const indexed={...document,indexingStatus:'COMPLETED',chunkCount:2,embeddingModel:'test-embed'}
    const fetchMock=mockApi(()=>json([indexed]))
    render(<DocumentManager/>);await screen.findByText(/2 chunks/)
    await userEvent.click(screen.getByRole('button',{name:'Re-index'}))
    await userEvent.click(screen.getByRole('button',{name:'Remove index'}))
    expect(fetchMock.mock.calls.filter(([,init])=>init?.method==='POST'||init?.method==='DELETE')).toHaveLength(0)
  })
})
function mockApi(handler:(request:{method:string,path:string})=>Response) {
  return vi.spyOn(globalThis,'fetch').mockImplementation((input,init)=>Promise.resolve(handler({method:init?.method??'GET',path:input.toString()})))
}
function json(value:unknown){return new Response(JSON.stringify(value),{status:200,headers:{'Content-Type':'application/json'}})}
