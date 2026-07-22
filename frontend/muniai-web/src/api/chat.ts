export type ChatResponse = {
  answer: string
  model: string
  provider: string
  correlationId: string
}

type ApiError = {
  message?: string
  correlationId?: string
}

export async function sendChat(message: string, signal?: AbortSignal): Promise<ChatResponse> {
  const correlationId = crypto.randomUUID()
  const response = await fetch('/api/v1/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Correlation-ID': correlationId,
    },
    body: JSON.stringify({ message }),
    signal,
  })

  const body = (await response.json().catch(() => ({}))) as ChatResponse & ApiError
  if (!response.ok) {
    const reference = body.correlationId ? ` Reference: ${body.correlationId}` : ''
    throw new Error(`${body.message ?? 'MuniAI could not complete the request.'}${reference}`)
  }
  return body
}
