import { FormEvent, useRef, useState } from 'react'
import { sendChat } from './api/chat'

type Message = { id: string; role: 'user' | 'assistant'; content: string; detail?: string }
const MAX_MESSAGE_LENGTH = 10_000

export default function App() {
  const [draft, setDraft] = useState('')
  const [messages, setMessages] = useState<Message[]>([])
  const [error, setError] = useState('')
  const [isSending, setIsSending] = useState(false)
  const controller = useRef<AbortController | null>(null)
  const remaining = MAX_MESSAGE_LENGTH - draft.length
  const canSend = draft.trim().length > 0 && remaining >= 0 && !isSending

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!canSend) return
    const content = draft.trim()
    setDraft(''); setError(''); setIsSending(true)
    setMessages((current) => [...current, { id: crypto.randomUUID(), role: 'user', content }])
    controller.current = new AbortController()
    try {
      const response = await sendChat(content, controller.current.signal)
      setMessages((current) => [...current, {
        id: crypto.randomUUID(), role: 'assistant', content: response.answer,
        detail: `${response.model} · ${response.provider} · ${response.correlationId}`,
      }])
    } catch (caught) {
      if ((caught as DOMException).name !== 'AbortError') setError(caught instanceof Error ? caught.message : 'MuniAI could not complete the request.')
    } finally { controller.current = null; setIsSending(false) }
  }

  function clearChat() {
    controller.current?.abort(); setMessages([]); setDraft(''); setError('')
  }

  return <main className="shell">
    <header className="topbar">
      <a className="brand" href="#chat" aria-label="MuniAI home"><span className="brand-mark" aria-hidden="true">M</span><span>MuniAI</span></a>
      <div className="privacy"><span aria-hidden="true" /> Local session</div>
      <button className="clear-button" type="button" onClick={clearChat} disabled={!messages.length && !draft}>Clear chat</button>
    </header>
    <section id="chat" className="chat" aria-labelledby="chat-title">
      {messages.length === 0 ? <div className="welcome">
        <p className="eyebrow">PRIVATE · ON YOUR DEVICE</p>
        <h1 id="chat-title">A quieter place<br />to think.</h1>
        <p>Ask a question and MuniAI will use your local Ollama model. Nothing in this chat is saved.</p>
        <div className="suggestions" aria-label="Example questions">
          {['Explain Docker networking simply', 'Help me outline a learning plan'].map((suggestion) =>
            <button key={suggestion} type="button" onClick={() => setDraft(suggestion)}>{suggestion}<span aria-hidden="true">↗</span></button>)}
        </div>
      </div> : <div className="messages" aria-live="polite" aria-busy={isSending}>
        <h1 id="chat-title" className="sr-only">Chat with MuniAI</h1>
        {messages.map((message) => <article className={`message ${message.role}`} key={message.id}>
          <p className="message-label">{message.role === 'user' ? 'You' : 'MuniAI'}</p>
          <div>{message.content}</div>
          {message.detail && <p className="message-detail">{message.detail}</p>}
        </article>)}
        {isSending && <div className="thinking" role="status"><span /><span /><span /> Thinking locally</div>}
      </div>}
      <div className="composer-wrap">
        {error && <div className="error" role="alert">{error}</div>}
        <form className="composer" onSubmit={submit}>
          <label className="sr-only" htmlFor="message">Message MuniAI</label>
          <textarea id="message" value={draft} onChange={(event) => setDraft(event.target.value)}
            onKeyDown={(event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); event.currentTarget.form?.requestSubmit() } }}
            placeholder="Ask MuniAI anything…" rows={2} maxLength={MAX_MESSAGE_LENGTH + 1} disabled={isSending} />
          <div className="composer-footer">
            <span className={remaining < 0 ? 'limit exceeded' : 'limit'}>{remaining.toLocaleString()} characters</span>
            <button type="submit" disabled={!canSend} aria-label="Send message">Send <span aria-hidden="true">↑</span></button>
          </div>
        </form>
        <p className="disclaimer">Model output may be inaccurate. Your conversation exists only in this browser tab.</p>
      </div>
    </section>
  </main>
}
