import { api } from './client'
import { type UserGoalResponse, type UserPlantSummaryResponse } from './userGoals'

export interface PlantGoalsResponse {
  plantId: number
  goals: UserGoalResponse[]
}

export const userPlantsApi = {
  getCurrent: () =>
    api.get<UserPlantSummaryResponse>('/user-plants/current'),

  getAll: () =>
    api.get<UserPlantSummaryResponse[]>('/user-plants/me'),

  getGoals: (plantId: number) =>
    api.get<PlantGoalsResponse>(`/user-plants/${plantId}/goals`),
}
