import { api } from './client'

export type EstimatedDuration = 'FIFTEEN_MIN' | 'ONE_HOUR' | 'HALF_DAY' | 'FULL_DAY'
export type PendingCategory = 'TRABAJO' | 'PERSONAL' | 'SALUD' | 'HOGAR'
export type PendingStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED'
export type RecommendationDecision = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export interface PendingSubtaskResponse {
  id: number
  taskId: number
  text: string
  done: boolean
  position: number
}

export interface PendingTaskResponse {
  id: number
  title: string
  description: string | null
  dueDate: string | null
  estimatedDuration: EstimatedDuration | null
  category: PendingCategory | null
  status: PendingStatus
  subtasks: PendingSubtaskResponse[]
  positionX: number | null
  positionY: number | null
  rotationDeg: number | null
  pinnedAt: string | null
  recommended: boolean
  createdAt: string
  completedAt: string | null
}

export interface CreatePendingTaskRequest {
  title: string
  description?: string
  dueDate?: string
  estimatedDuration?: EstimatedDuration
  category?: PendingCategory
  subtasks?: string[]
}

export interface UpdatePendingTaskRequest {
  title: string
  description?: string
  dueDate?: string
  estimatedDuration?: EstimatedDuration
  category?: PendingCategory
}

export interface PendingRecommendationResponse {
  recommendationId: number
  date: string
  decision: RecommendationDecision
  recommendedTaskIds: number[]
  isNew: boolean
}

export const pendingApi = {
  list: (status?: PendingStatus) =>
    api.get<PendingTaskResponse[]>(status ? `/pending?status=${status}` : '/pending'),

  create: (data: CreatePendingTaskRequest) =>
    api.post<PendingTaskResponse>('/pending', data),

  update: (id: number, data: UpdatePendingTaskRequest) =>
    api.patch<void>(`/pending/${id}`, data),

  delete: (id: number) =>
    api.delete<void>(`/pending/${id}`),

  complete: (id: number) =>
    api.patch<void>(`/pending/${id}/complete`),

  addSubtask: (taskId: number, text: string) =>
    api.post<PendingSubtaskResponse>(`/pending/${taskId}/subtasks`, { text }),

  toggleSubtask: (taskId: number, subtaskId: number) =>
    api.patch<void>(`/pending/${taskId}/subtasks/${subtaskId}/toggle`),

  deleteSubtask: (taskId: number, subtaskId: number) =>
    api.delete<void>(`/pending/${taskId}/subtasks/${subtaskId}`),

  updatePosition: (id: number, positionX: number, positionY: number) =>
    api.patch<PendingTaskResponse>(`/pending/${id}/position`, { positionX, positionY }),

  getTodayRecommendation: () =>
    api.get<PendingRecommendationResponse | null>('/pending/recommendation/today'),

  generateRecommendation: () =>
    api.post<PendingRecommendationResponse | null>('/pending/recommendation/generate', undefined),

  respondToRecommendation: (recommendationId: number, decision: 'ACCEPTED' | 'REJECTED') =>
    api.post<PendingRecommendationResponse>(`/pending/recommendation/${recommendationId}/respond`, { decision }),
}
