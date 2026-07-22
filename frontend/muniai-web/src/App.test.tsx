import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

const chatResponse = { answer: 'Networks connect containers.', model: 'llama3.2:3b', provider: 'ollama', correlationId: 'test-id' }

afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})

describe('MuniAI chat', () => {
  it('does not send a blank message', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch')
    render(<App />)
    expect(screen.getByRole('button', { name: 'Send message' })).toBeDisabled()
    await userEvent.type(screen.getByLabelText('Message MuniAI'), '   ')
    expect(screen.getByRole('button', { name: 'Send message' })).toBeDisabled()
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('sends a message and renders the provider-neutral response as text', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify(chatResponse), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    render(<App />)
    await userEvent.type(screen.getByLabelText('Message MuniAI'), '<b>hello</b>')
    await userEvent.click(screen.getByRole('button', { name: 'Send message' }))
    expect(await screen.findByText('Networks connect containers.')).toBeInTheDocument()
    expect(screen.getByText('<b>hello</b>')).toBeInTheDocument()
    expect(document.querySelector('b')).toBeNull()
  })

  it('shows a safe API error and its correlation reference', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ message: 'The local AI provider is unavailable.', correlationId: 'ref-1' }), { status: 503 }))
    render(<App />)
    await userEvent.type(screen.getByLabelText('Message MuniAI'), 'hello')
    await userEvent.click(screen.getByRole('button', { name: 'Send message' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Reference: ref-1')
  })

  it('clears in-memory chat content', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify(chatResponse), { status: 200 }))
    render(<App />)
    await userEvent.type(screen.getByLabelText('Message MuniAI'), 'hello')
    await userEvent.click(screen.getByRole('button', { name: 'Send message' }))
    await screen.findByText('Networks connect containers.')
    await userEvent.click(screen.getByRole('button', { name: 'Clear chat' }))
    await waitFor(() => expect(screen.queryByText('Networks connect containers.')).not.toBeInTheDocument())
    expect(screen.getByText(/A quieter place/)).toBeInTheDocument()
  })
})
