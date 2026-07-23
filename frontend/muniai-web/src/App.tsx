import { FormEvent, useCallback, useEffect, useRef, useState } from 'react'
import { Conversation, conversationApi, StoredMessage } from './api/conversations'
import DocumentManager from './DocumentManager'

const MAX_MESSAGE_LENGTH = 10_000

export default function App() {
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [active, setActive] = useState<Conversation | null>(null)
  const [draft, setDraft] = useState('')
  const [error, setError] = useState('')
  const [isLoadingList, setIsLoadingList] = useState(true)
  const [isLoadingChat, setIsLoadingChat] = useState(false)
  const [isSending, setIsSending] = useState(false)
  const [view, setView] = useState<'chat' | 'documents'>('chat')
  const controller = useRef<AbortController | null>(null)
  const remaining = MAX_MESSAGE_LENGTH - draft.length
  const canSend = draft.trim().length > 0 && remaining >= 0 && !isSending

  const refreshList = useCallback(async () => {
    const values = await conversationApi.list()
    setConversations(values)
    return values
  }, [])

  const openConversation = useCallback(async (id: string) => {
    setIsLoadingChat(true); setError('')
    try { setActive(await conversationApi.get(id)) }
    catch (caught) { setError(messageOf(caught)) }
    finally { setIsLoadingChat(false) }
  }, [])

  useEffect(() => {
    void refreshList().then((values) => values[0] && openConversation(values[0].id))
      .catch((caught) => setError(messageOf(caught))).finally(() => setIsLoadingList(false))
  }, [openConversation, refreshList])

  async function newChat() {
    setError(''); setDraft('')
    try {
      const created = await conversationApi.create()
      setActive(created)
      await refreshList()
    } catch (caught) { setError(messageOf(caught)) }
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!canSend) return
    const content = draft.trim()
    let conversation = active
    setError(''); setIsSending(true); setDraft('')
    try {
      if (!conversation) {
        conversation = await conversationApi.create()
        setActive(conversation)
      }
      const pending: StoredMessage = {
        id: crypto.randomUUID(), conversationId: conversation.id, role: 'USER', content, model: null,
        createdAt: new Date().toISOString(),
      }
      setActive({ ...conversation, messages: [...conversation.messages, pending] })
      controller.current = new AbortController()
      const response = await conversationApi.send(conversation.id, content, controller.current.signal)
      setActive((current) => current && current.id === response.conversationId
        ? { ...current, messages: [...current.messages.filter((item) => item.id !== pending.id), response.userMessage, response.assistantMessage] }
        : current)
      await refreshList()
    } catch (caught) {
      setError(messageOf(caught))
      if (conversation) await openConversation(conversation.id)
      await refreshList().catch(() => undefined)
    } finally { controller.current = null; setIsSending(false) }
  }

  async function renameConversation(conversation: Conversation) {
    const title = window.prompt('Rename conversation', conversation.title)?.trim()
    if (!title) return
    try {
      const updated = await conversationApi.rename(conversation.id, title)
      setActive((current) => current?.id === updated.id ? { ...current, title: updated.title, updatedAt: updated.updatedAt } : current)
      await refreshList()
    } catch (caught) { setError(messageOf(caught)) }
  }

  async function deleteConversation(conversation: Conversation) {
    if (!window.confirm(`Delete “${conversation.title}” and all its messages?`)) return
    try {
      await conversationApi.remove(conversation.id)
      const remainingConversations = await refreshList()
      if (active?.id === conversation.id) {
        setActive(null)
        if (remainingConversations[0]) await openConversation(remainingConversations[0].id)
      }
    } catch (caught) { setError(messageOf(caught)) }
  }

  async function deleteAll() {
    if (!window.confirm('Delete every conversation and message? This cannot be undone.')) return
    try { await conversationApi.removeAll(); setConversations([]); setActive(null); setDraft(''); setError('') }
    catch (caught) { setError(messageOf(caught)) }
  }

  return <main className="shell app-layout">
    <aside className="sidebar" aria-label="Conversation history">
      <div className="sidebar-brand"><span className="brand-mark">M</span><strong>MuniAI</strong></div>
      <button className="new-chat" type="button" onClick={newChat}>+ New Chat</button>
      <button className="document-nav" type="button" onClick={() => setView(view === 'chat' ? 'documents' : 'chat')}>
        {view === 'chat' ? 'Documents' : 'Back to chat'}
      </button>
      <div className="conversation-list">
        {isLoadingList ? <p className="sidebar-state">Loading conversations…</p> : conversations.length === 0
          ? <p className="sidebar-state">No saved conversations yet.</p>
          : conversations.map((conversation) => <div className={`conversation-item ${active?.id === conversation.id ? 'active' : ''}`} key={conversation.id}>
              <button className="conversation-open" onClick={() => openConversation(conversation.id)}>{conversation.title}</button>
              <button aria-label={`Rename ${conversation.title}`} onClick={() => renameConversation(conversation)}>✎</button>
              <button aria-label={`Delete ${conversation.title}`} onClick={() => deleteConversation(conversation)}>×</button>
            </div>)}
      </div>
      <button className="delete-all" type="button" disabled={!conversations.length} onClick={deleteAll}>Delete all conversations</button>
    </aside>

    <section className="main-panel">
      {view === 'documents' ? <DocumentManager /> : <>
      <header className="topbar">
        <div className="mobile-brand">MuniAI</div>
        <div className="privacy"><span aria-hidden="true" /> Local · saved</div>
        <button className="clear-button" type="button" onClick={newChat}>New Chat</button>
      </header>
      <section className="chat" aria-labelledby="chat-title">
        {isLoadingChat ? <div className="welcome"><p>Loading conversation…</p></div>
          : !active || active.messages.length === 0 ? <div className="welcome">
              <p className="eyebrow">PRIVATE · SAVED LOCALLY</p>
              <h1 id="chat-title">A quieter place<br />to think.</h1>
              <p>{active ? 'This conversation is ready for its first message.' : 'Start a chat. Conversations are saved locally until you delete them.'}</p>
            </div>
          : <div className="messages" aria-live="polite" aria-busy={isSending}>
              <h1 id="chat-title" className="sr-only">{active.title}</h1>
              {active.messages.map((message) => <article className={`message ${message.role.toLowerCase()}`} key={message.id}>
                <p className="message-label">{message.role === 'USER' ? 'You' : 'MuniAI'}</p>
                <div>{message.content}</div>
                {message.role === 'ASSISTANT' && <p className="message-detail">{message.model ?? 'local model'}</p>}
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
            <div className="composer-footer"><span className={remaining < 0 ? 'limit exceeded' : 'limit'}>{remaining.toLocaleString()} characters</span>
              <button type="submit" disabled={!canSend} aria-label="Send message">Send <span aria-hidden="true">↑</span></button></div>
          </form>
          <p className="disclaimer">Model output may be inaccurate. Conversation history is not long-term AI memory.</p>
        </div>
      </section></>}
    </section>
  </main>
}

function messageOf(caught: unknown) {
  return caught instanceof Error ? caught.message : 'MuniAI could not complete the request.'
}
