export type ExtractionStatus = 'UPLOADED' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
export type IndexingStatus = 'NOT_INDEXED' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
export type DocumentMetadata = {
  id: string
  originalFileName: string
  contentType: string
  fileSize: number
  extractionStatus: ExtractionStatus
  extractionError: string | null
  pageCount: number | null
  fileAvailable: boolean
  indexingStatus: IndexingStatus
  indexingStartedAt: string | null
  indexingCompletedAt: string | null
  indexingError: string | null
  chunkCount: number
  embeddingModel: string | null
  embeddingDimension: number | null
  qdrantCollectionName: string | null
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
  index: (id: string) => request<DocumentMetadata>(`/api/v1/documents/${id}/index`, { method: 'POST' }),
  reindex: (id: string) => request<DocumentMetadata>(`/api/v1/documents/${id}/reindex`, { method: 'POST' }),
  removeIndex: (id: string) => request<void>(`/api/v1/documents/${id}/index`, { method: 'DELETE' }),
  search: (query: string, limit: number, documentId?: string) => request<VectorSearchResponse>('/api/v1/vector-search/test', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, limit, documentId: documentId || null }),
  }),
}

export type VectorSearchResponse = {
  results: { chunkId: string; documentId: string; chunkIndex: number; score: number; contentPreview: string; originalFileName: string }[]
  notice: string
}
