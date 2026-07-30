import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import type { Conversation } from './api/conversations'

const summary = conversation('c1', 'Docker networking', [])
const detail = conversation('c1', 'Docker networking', [message('m1', 'USER', 'Explain Docker networking'), message('m2', 'ASSISTANT', 'Containers communicate over virtual networks.', 'llama3.2:3b')])

afterEach(() => { cleanup(); vi.restoreAllMocks() })

describe('persistent conversation history', () => {
  it('renders the list and opens its newest conversation', async () => {
    mockApi(({ method, path }) => method === 'GET' && path === '/api/v1/conversations' ? json([summary]) : json(detail))
    render(<App />)
    expect((await screen.findAllByText('Docker networking')).length).toBeGreaterThan(0)
    expect(await screen.findByText('Containers communicate over virtual networks.')).toBeInTheDocument()
  })

  it('shows the empty state', async () => {
    mockApi(() => json([])); render(<App />)
    expect(await screen.findByText('No saved conversations yet.')).toBeInTheDocument()
    expect(screen.getByText(/Start a chat/)).toBeInTheDocument()
  })

  it('creates a new conversation', async () => {
    const fetchMock = mockApi(({ method }) => method === 'POST' ? json(conversation('new', 'New conversation', [])) : json([]))
    render(<App />); await screen.findByText('No saved conversations yet.')
    await userEvent.click(screen.getAllByRole('button', { name: 'New Chat' })[0])
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith('/api/v1/conversations', expect.objectContaining({ method: 'POST' })))
    expect(screen.getByText('This conversation is ready for its first message.')).toBeInTheDocument()
  })

  it('sends and safely displays stored messages', async () => {
    mockApi(({ method, path }) => {
      if (method === 'GET' && path === '/api/v1/conversations') return json([])
      if (method === 'POST' && path === '/api/v1/conversations') return json(conversation('new', 'New conversation', []))
      return json({ conversationId: 'new', userMessage: message('u1', 'USER', '<b>hello</b>', null, 'new'), assistantMessage: message('a1', 'ASSISTANT', 'Hello locally.', 'llama3.2:3b', 'new') })
    })
    render(<App />); await screen.findByText('No saved conversations yet.')
    await userEvent.type(screen.getByLabelText('Message MuniAI'), '<b>hello</b>')
    await userEvent.click(screen.getByRole('button', { name: 'Send message' }))
    expect(await screen.findByText('Hello locally.')).toBeInTheDocument()
    expect(screen.getByText('<b>hello</b>')).toBeInTheDocument()
    expect(document.querySelector('b')).toBeNull()
  })

  it('renames a conversation', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('Containers')
    const fetchMock = mockApi(({ method, path }) => method === 'PATCH' ? json({ ...summary, title: 'Containers' }) : path.endsWith('/c1') ? json(detail) : json([summary]))
    render(<App />); await screen.findByText('Containers communicate over virtual networks.')
    await userEvent.click(screen.getByRole('button', { name: 'Rename Docker networking' }))
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith('/api/v1/conversations/c1', expect.objectContaining({ method: 'PATCH' })))
  })

  it('requires confirmation before deleting one conversation', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false)
    const fetchMock = mockApi(({ path }) => path.endsWith('/c1') ? json(detail) : json([summary]))
    render(<App />); await screen.findByText('Containers communicate over virtual networks.')
    await userEvent.click(screen.getByRole('button', { name: 'Delete Docker networking' }))
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'DELETE')).toBe(false)
  })

  it('deletes all conversations after confirmation', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const fetchMock = mockApi(({ method, path }) => method === 'DELETE' ? empty() : path.endsWith('/c1') ? json(detail) : json([summary]))
    render(<App />); await screen.findByText('Containers communicate over virtual networks.')
    await userEvent.click(screen.getByRole('button', { name: 'Delete all conversations' }))
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith('/api/v1/conversations', expect.objectContaining({ method: 'DELETE' })))
    expect(screen.getByText('No saved conversations yet.')).toBeInTheDocument()
  })

  it('shows API errors with correlation references', async () => {
    mockApi(() => new Response(JSON.stringify({ message: 'Database unavailable.', correlationId: 'ref-1' }), { status: 503 }))
    render(<App />)
    expect(await screen.findByRole('alert')).toHaveTextContent('Reference: ref-1')
  })

  it('recovers from a deleted active conversation without retaining optimistic messages', async () => {
    let listCalls = 0
    mockApi(({ method, path }) => {
      if (method === 'GET' && path === '/api/v1/conversations') {
        listCalls++
        return json(listCalls === 1 ? [summary] : [])
      }
      if (method === 'GET' && path === '/api/v1/conversations/c1') return json(detail)
      if (method === 'POST' && path.endsWith('/messages')) {
        return new Response(JSON.stringify({
          code: 'CONVERSATION_NOT_FOUND',
          message: 'The conversation was not found.',
          correlationId: 'stale-ref',
        }), { status: 404, headers: { 'Content-Type': 'application/json' } })
      }
      return json([])
    })

    render(<App />)
    expect(await screen.findByText('Containers communicate over virtual networks.')).toBeInTheDocument()
    await userEvent.type(screen.getByLabelText('Message MuniAI'), 'Will not be duplicated')
    await userEvent.click(screen.getByRole('button', { name: 'Send message' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Reference: stale-ref')
    expect(screen.queryByText('Will not be duplicated')).not.toBeInTheDocument()
    expect(screen.getByText(/Start a chat/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument()
  })

  it('selects Ask Documents and renders grounded citation details', async () => {
    const indexed = { id:'d1', originalFileName:'architecture.pdf', indexingStatus:'COMPLETED' }
    mockApi(({ method, path }) => {
      if (path === '/api/v1/documents') return json([indexed])
      if (method === 'GET' && path === '/api/v1/conversations') return json([])
      if (method === 'POST' && path === '/api/v1/conversations') return json(conversation('new','New conversation',[]))
      return json({
        conversationId:'new',
        userMessage:{...message('u1','USER','How is traffic routed?',null,'new'),mode:'DOCUMENT_RAG'},
        assistantMessage:{...message('a1','ASSISTANT','Through a load balancer. [Source 1]','llama3.2:3b','new'),
          mode:'DOCUMENT_RAG',citations:[{citationId:'x1',citationIndex:1,documentId:'d1',originalFileName:'architecture.pdf',
            chunkId:'k1',chunkIndex:7,pageNumber:null,similarityScore:0.82,contentPreview:'The load balancer forwards traffic.'}]},
        provider:'ollama',correlationId:'ref',noRelevantContext:false,
      })
    })
    render(<App />); await screen.findByText('No saved conversations yet.')
    await waitFor(() => expect(screen.getByRole('button',{name:'Ask Documents'})).toBeEnabled())
    await userEvent.click(screen.getByRole('button',{name:'Ask Documents'}))
    expect(screen.getByText('Using all indexed documents. Responses include local source citations.')).toBeInTheDocument()
    await userEvent.type(screen.getByLabelText('Message MuniAI'),'How is traffic routed?')
    await userEvent.click(screen.getByRole('button',{name:'Send message'}))
    expect(await screen.findByText('Through a load balancer. [Source 1]')).toBeInTheDocument()
    await userEvent.click(screen.getByText('Retrieved evidence (1)'))
    expect(screen.getByText('[1] architecture.pdf')).toBeInTheDocument()
    expect(screen.getByText('Similarity 0.8200')).toBeInTheDocument()
  })
})

function conversation(id: string, title: string, messages: ReturnType<typeof message>[]): Conversation {
  return { id, title, createdAt: '2026-07-22T08:00:00Z', updatedAt: '2026-07-22T08:00:00Z', messages }
}
function message(id: string, role: 'USER' | 'ASSISTANT' | 'SYSTEM', content: string, model: string | null = null, conversationId = 'c1') {
  return { id, conversationId, role, content, model, mode: 'NORMAL' as const, citations: [], createdAt: '2026-07-22T08:00:00Z' }
}
function mockApi(handler: (request: { method: string; path: string }) => Response) {
  return vi.spyOn(globalThis, 'fetch').mockImplementation((input, init) => Promise.resolve(handler({ method: init?.method ?? 'GET', path: input.toString() })))
}
function json(value: unknown) { return new Response(JSON.stringify(value), { status: 200, headers: { 'Content-Type': 'application/json' } }) }
function empty() { return new Response(null, { status: 204 }) }
