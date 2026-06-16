import { api } from './client'

export interface LanternThought {
  id: number
  text: string
  workedOn: boolean
  createdAt: string
}

export const lanternsApi = {
  list: () => api.get<LanternThought[]>('/lanterns'),
  create: (text: string) => api.post<LanternThought>('/lanterns', { text }),
  updateStatus: (id: number, status: 'COMPLETED' | 'CANCELLED') =>
    api.patch<void>(`/lanterns/${id}/status`, { status }),
  markWorkedOn: (id: number) => api.patch<void>(`/lanterns/${id}/worked-on`),
}
