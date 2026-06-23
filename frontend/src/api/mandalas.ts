import { api } from './client'

export type MandalaUnlockSource = 'FREE' | 'PURCHASED' | 'SUBSCRIPTION'

export interface MandalaResponse {
  id: string
  title: string
  description?: string
  assetKey: string
  displayOrder: number
  unlockSource: MandalaUnlockSource
}

export interface MandalaPageResponse {
  content: MandalaResponse[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

interface MandalaPageParams {
  page?: number
  size?: number
}

export const mandalasApi = {
  getAvailable: ({ page = 0, size = 6 }: MandalaPageParams = {}) =>
    api.get<MandalaPageResponse>(`/mandalas?page=${page}&size=${size}`),
  getProgress: (mandalaId: string) => api.getBlob(`/mandalas/${mandalaId}/progress`),
  saveProgress: (mandalaId: string, blob: Blob) =>
    api.putBlob<void>(`/mandalas/${mandalaId}/progress`, blob),
  clearProgress: (mandalaId: string) =>
    api.delete<void>(`/mandalas/${mandalaId}/progress`),
}
