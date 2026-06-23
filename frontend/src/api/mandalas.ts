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

export const mandalasApi = {
  getAvailable: () => api.get<MandalaResponse[]>('/mandalas'),
  getProgress: (mandalaId: string) => api.getBlob(`/mandalas/${mandalaId}/progress`),
  saveProgress: (mandalaId: string, blob: Blob) =>
    api.putBlob<void>(`/mandalas/${mandalaId}/progress`, blob),
  clearProgress: (mandalaId: string) =>
    api.delete<void>(`/mandalas/${mandalaId}/progress`),
}
