import { api } from './client'

export interface UserGoalResponse {
  id: number
  userId: number
  title: string
  description: string | null
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED'
  createdAt: string
  activityId: number | null
}

export interface UserGoalPageResponse {
  content: UserGoalResponse[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface UserGoalListResponse {
  completados: UserGoalPageResponse
  pendientes: UserGoalPageResponse
}

export interface CreateUserGoalRequest {
  userId: number
  title: string
  description?: string
  activityId?: number
}

export interface UpdateUserGoalRequest {
  title: string
  description?: string
  activityId?: number
}

export const userGoalsApi = {
  getByUser: (userId: number, page = 0, size = 50) =>
    api.get<UserGoalListResponse>(`/user-goals/user/${userId}?page=${page}&size=${size}`),

  create: (data: CreateUserGoalRequest) =>
    api.post<UserGoalResponse>('/user-goals', data),

  update: (id: number, data: UpdateUserGoalRequest) =>
    api.put<UserGoalResponse>(`/user-goals/${id}`, data),

  delete: (id: number) =>
    api.delete<void>(`/user-goals/${id}`),

  complete: (id: number) =>
    api.patch<UserGoalResponse>(`/user-goals/${id}/complete`),
}
