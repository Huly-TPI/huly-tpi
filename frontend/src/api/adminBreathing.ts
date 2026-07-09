import { api } from './client'

export interface AdminBreathingTechnique {
    id: number
    name: string
    description: string
    inhaleSeconds: number
    holdSeconds: number
    exhaleSeconds: number
    roundsInterval: number
    rounds: number
    active: boolean
}

export interface BreathingFormData {
    name: string
    description: string
    inhaleSeconds: number
    holdSeconds: number
    exhaleSeconds: number
    roundsInterval: number
    rounds: number
}

export const adminBreathingApi = {
    list: () => api.get<AdminBreathingTechnique[]>('/admin/breathing-techniques'),
    create: (data: BreathingFormData) => api.post<AdminBreathingTechnique>('/admin/breathing-techniques', data),
    update: (id: number, data: BreathingFormData) => api.put<AdminBreathingTechnique>(`/admin/breathing-techniques/${id}`, data),
    setActive: (id: number, active: boolean) => api.patch<AdminBreathingTechnique>(`/admin/breathing-techniques/${id}/active`, { active }),
}