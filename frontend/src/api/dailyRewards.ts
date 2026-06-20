import { api } from './client'

export interface DailyRewardDay {
  dayNumber: number
  coins: number
}

export interface DailyRewardStatus {
  days: DailyRewardDay[]
  currentStreak: number
  completedDays: number
  canClaimToday: boolean
  nextDay: number
  /** true si el usuario tiene un plan activo: los `coins` ya vienen con el bonus x1.5 aplicado. */
  planBonusActive: boolean
}

export interface ClaimDailyRewardResult {
  coins: number
  dayNumber: number
  newStreak: number
}

export const getDailyRewardStatus = () =>
  api.get<DailyRewardStatus>('/daily-rewards/status')

export const claimDailyReward = () =>
  api.post<ClaimDailyRewardResult>('/daily-rewards/claim', undefined)
