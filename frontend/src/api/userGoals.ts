import { api } from './client'

export interface UserGoalResponse {
  id: number
  userId: number
  title: string
  description: string | null
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED'
  createdAt: string
  activityId: number | null
  imageUrl: string | null
  coinsReward: number
  coinsRewardWithImage: number
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
  title: string
  description?: string
  activityId?: number
}

export interface UpdateUserGoalRequest {
  title: string
  description?: string
  activityId?: number
}

export interface AcceptChallengeRequest {
  title: string
  description?: string
  activityId?: number
}

export interface UserPlantSummaryResponse {
  id: number
  plantNumber: number
  requiredGoals: number
  completedGoalsCount: number
  status: 'GROWING' | 'COMPLETED'
  startedAt: string
  completedAt: string | null
}

export interface GoalCompleteResponse {
  goal: UserGoalResponse
  harvestTriggered: boolean
  harvestedPlantNumber: number | null
  currentPlant: UserPlantSummaryResponse
}

export const userGoalsApi = {
  getForCurrentUser: (page = 0, size = 50) =>
    api.get<UserGoalListResponse>(`/user-goals/me?page=${page}&size=${size}`),

  create: (data: CreateUserGoalRequest) =>
    api.post<UserGoalResponse>('/user-goals', data),

  update: (id: number, data: UpdateUserGoalRequest) =>
    api.put<UserGoalResponse>(`/user-goals/${id}`, data),

  delete: (id: number) =>
    api.delete<void>(`/user-goals/${id}`),

  complete: (id: number, image?: File) => {
    const form = new FormData()
    if (image) form.append('image', image)
    return api.patch<GoalCompleteResponse>(`/user-goals/${id}/complete`, form)
  },

  acceptChallenge: (data: AcceptChallengeRequest) =>
    api.post<UserGoalResponse>('/user-goals/accept', data),
}
