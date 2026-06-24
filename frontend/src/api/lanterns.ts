import { api } from './client'

export interface LanternThought {
  id: number
  text: string
  workedOn: boolean
  createdAt: string
}

export const lanternsApi = {
  list: () => api.get<LanternThought[]>('/clouds'),
  create: (text: string) => api.post<LanternThought>('/clouds/thought', { thought: text }),
  updateStatus: (id: number, status: 'COMPLETED' | 'CANCELLED') =>
    api.patch<void>(`/clouds/${id}/status`, { status }),
  markWorkedOn: (id: number) => api.patch<void>(`/clouds/${id}/worked-on`),
}
