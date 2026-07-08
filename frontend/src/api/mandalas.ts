import { api } from './client'

export type MandalaUnlockSource = 'FREE' | 'PURCHASED' | 'SUBSCRIPTION'
export type MandalaAccessType = 'FREE' | 'SUBSCRIPTION' | 'PURCHASABLE'

export interface MandalaResponse {
  id: string
  title: string
  description?: string
  assetKey: string
  displayOrder: number
  unlockSource: MandalaUnlockSource | null
  accessType: MandalaAccessType
  isLocked: boolean
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

export interface MandalaSessionStatusResponse {
  sessionRegistered: boolean
}

interface MandalaPageParams {
  page?: number
  size?: number
}

export const mandalasApi = {
  getAvailable: ({ page = 0, size = 6 }: MandalaPageParams = {}) =>
    api.get<MandalaPageResponse>(`/mandalas?page=${page}&size=${size}`),
  getProgress: (mandalaId: string) => api.getBlob(`/mandalas/${mandalaId}/progress`),
  getSessionStatus: (mandalaId: string) =>
    api.get<MandalaSessionStatusResponse>(`/mandalas/${mandalaId}/session-status`),
  saveProgress: (mandalaId: string, blob: Blob) =>
    api.putBlob<void>(`/mandalas/${mandalaId}/progress`, blob),
  clearProgress: (mandalaId: string) =>
    api.delete<void>(`/mandalas/${mandalaId}/progress`),
}
