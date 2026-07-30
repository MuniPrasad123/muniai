export type MessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM'
export type ChatMode = 'NORMAL' | 'DOCUMENT_RAG'
export type Citation = {
  citationId: string
  citationIndex: number
  documentId: string
  originalFileName: string
  chunkId: string
  chunkIndex: number
  pageNumber: number | null
  similarityScore: number
  contentPreview: string
}

export type StoredMessage = {
  id: string
  conversationId: string
  role: MessageRole
  content: string
  model: string | null
  mode: ChatMode
  citations: Citation[]
  createdAt: string
}

export type Conversation = {
  id: string
  title: string
  createdAt: string
  updatedAt: string
  messages: StoredMessage[]
}

export type SendMessageResponse = {
  conversationId: string
  userMessage: StoredMessage
  assistantMessage: StoredMessage
  provider: string
  correlationId: string
  noRelevantContext: boolean
}

type ErrorBody = { code?: string; message?: string; correlationId?: string }

export class ConversationApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
    readonly correlationId?: string,
  ) {
    super(message)
    this.name = 'ConversationApiError'
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', 'X-Correlation-ID': crypto.randomUUID(), ...init?.headers },
  })
  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ErrorBody
    const reference = body.correlationId ? ` Reference: ${body.correlationId}` : ''
    throw new ConversationApiError(
      `${body.message ?? 'MuniAI could not complete the request.'}${reference}`,
      response.status,
      body.code,
      body.correlationId,
    )
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const conversationApi = {
  list: () => request<Conversation[]>('/api/v1/conversations'),
  create: () => request<Conversation>('/api/v1/conversations', { method: 'POST' }),
  get: (id: string) => request<Conversation>(`/api/v1/conversations/${id}`),
  rename: (id: string, title: string) => request<Conversation>(`/api/v1/conversations/${id}`, {
    method: 'PATCH', body: JSON.stringify({ title }),
  }),
  remove: (id: string) => request<void>(`/api/v1/conversations/${id}`, { method: 'DELETE' }),
  removeAll: () => request<void>('/api/v1/conversations', { method: 'DELETE' }),
  send: (id: string, message: string, mode: ChatMode, documentIds: string[], signal?: AbortSignal) => request<SendMessageResponse>(
    `/api/v1/conversations/${id}/messages`, {
      method: 'POST', body: JSON.stringify({ message, mode, documentIds, topK: 5 }), signal,
    }),
}
