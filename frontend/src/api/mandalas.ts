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
}
