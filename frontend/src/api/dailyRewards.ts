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
