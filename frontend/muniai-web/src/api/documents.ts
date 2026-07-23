export type ExtractionStatus = 'UPLOADED' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
export type DocumentMetadata = {
  id: string
  originalFileName: string
  contentType: string
  fileSize: number
  extractionStatus: ExtractionStatus
  extractionError: string | null
  pageCount: number | null
  fileAvailable: boolean
  createdAt: string
  updatedAt: string
}

type ErrorBody = { message?: string; correlationId?: string }
async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, { ...init, headers: { 'X-Correlation-ID': crypto.randomUUID(), ...init?.headers } })
  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ErrorBody
    throw new Error(`${body.message ?? 'MuniAI could not complete the document request.'}${body.correlationId ? ` Reference: ${body.correlationId}` : ''}`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
export const documentApi = {
  list: () => request<DocumentMetadata[]>('/api/v1/documents'),
  upload: (file: File) => {
    const body = new FormData(); body.append('file', file)
    return request<DocumentMetadata>('/api/v1/documents', { method: 'POST', body })
  },
  text: (id: string) => request<{ id: string; text: string }>(`/api/v1/documents/${id}/text`),
  remove: (id: string) => request<void>(`/api/v1/documents/${id}`, { method: 'DELETE' }),
}
