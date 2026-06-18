import { api } from './client'
import { Timeframe } from '../types/timeframe'

export interface UserResponse {
  id: number
  name: string
  email: string
  role: string
  status: string
  birthDate: string | null
  antiScrollEnabled: boolean
  dataSharingConsent: boolean
  mostUsedApp?: string | null
  mostUsedAppActiveSeconds?: number
  totalScrollTimeSeconds?: number
  dailyScrollTimeSeconds?: Record<string, number>
  topApps?: TopAppResponse[]
  coins?: number
  plan?: string | null
  dominantEmotion?: string | null
}

export interface TopAppResponse {
  domain: string
  totalActiveSeconds: number
}

export interface VectorMemoryResponse {
  id: string
  content: string
  sourceType: string
  contentType: string
  createdAt: string | null
}

export interface ActivitySessionResponse {
  id: number
  activityType: string
  createdAt: string
}

export interface EmotionalEventResponse {
  id: number
  source: string | null
  inputText: string | null
  detectedEmotion: string | null
  confidence: number | null
  valence: number | null
  arousal: number | null
  dominance: number | null
  intensity: number | null
  userGoal: string | null
  generatedRecommendation: string | null
  recommendedActivityId: number | null
  chosenActivityId: number | null
  recommendationDecision: string | null
  feedbackScore: number | null
  feedbackText: string | null
  createdAt: string
}

export interface PaymentEventResponse {
  id: number
  productId: number
  productName: string
  productPrice: number
  externalReference: string
  mpPaymentId: number | null
  status: string
  coinsAmount: number
  productType: string
  createdAt: string
}

export interface UserActivitiesResponse {
  activitySessions: ActivitySessionResponse[]
  todayActivitiesCount: number
  favoriteActivity: string | null
  averageSessionsText: string
  activityDistribution: Record<string, number>
}

export interface UserAiDiagnosticsResponse {
  aiMemories: VectorMemoryResponse[]
  emotionalEvents: EmotionalEventResponse[]
  preferredName: string | null
  communicationStyle: string | null
  personalitySummary?: string
  topicsDetected?: string[]
  copingStrategies?: string[]
  receptivityScore?: number
  receptivityLabel?: string
  acceptedActivities?: string[]
  ignoredActivities?: string[]
  dominantEmotion?: string | null
  emotionDistribution?: Record<string, number>
}

export interface UserFinancialsResponse {
  paymentEvents: PaymentEventResponse[]
  totalEarnings: number
}

export interface AntiScrollDashboardResponse {
  totalModalsShown: number
  totalRedirects: number
  totalUsersCount: number
  activeExtensionUsersCount: number
  dataSharingConsentUsersCount: number
  topUsedApps: TopAppResponse[]
}

export const getUsers = async (search?: string): Promise<UserResponse[]> => {
  const url = search ? `/admin/users?search=${encodeURIComponent(search)}` : '/admin/users'
  return api.get<UserResponse[]>(url)
}

export const getAntiScrollDashboard = async (): Promise<AntiScrollDashboardResponse> => {
  return api.get<AntiScrollDashboardResponse>('/admin/users/antiscroll/dashboard')
}

export interface AntiScrollConfigResponse {
  defaultPauseIntervalMinutes: number
  termsAndConditions: string
}

export const getAntiScrollConfig = async (): Promise<AntiScrollConfigResponse> => {
  return api.get<AntiScrollConfigResponse>('/admin/users/antiscroll/config')
}

export const saveAntiScrollConfig = async (config: AntiScrollConfigResponse): Promise<void> => {
  return api.post<void>('/admin/users/antiscroll/config', config)
}

export const getUserActivities = async (userId: number, timeframe?: Timeframe): Promise<UserActivitiesResponse> => {
  const url = timeframe ? `/admin/users/${userId}/statistics/activities?timeframe=${timeframe}` : `/admin/users/${userId}/statistics/activities`
  return api.get<UserActivitiesResponse>(url)
}

export const getUserAiDiagnostics = async (userId: number): Promise<UserAiDiagnosticsResponse> => {
  return api.get<UserAiDiagnosticsResponse>(`/admin/users/${userId}/statistics/ai`)
}

export const getUserFinancials = async (userId: number): Promise<UserFinancialsResponse> => {
  return api.get<UserFinancialsResponse>(`/admin/users/${userId}/statistics/finance`)
}

export interface UserAntiScrollResponse {
  antiScrollEnabled: boolean
  dataSharingConsent: boolean
  mostUsedApp: string | null
  mostUsedAppActiveSeconds: number
  totalScrollTimeSeconds: number
  dailyScrollTimeSeconds: Record<string, number>
  topApps: TopAppResponse[]
}

export const getUserAntiScroll = async (
  userId: number,
  week: 'current' | 'previous' = 'current',
  day: string = 'all',
): Promise<UserAntiScrollResponse> => {
  return api.get<UserAntiScrollResponse>(`/admin/users/${userId}/statistics/antiscroll?week=${week}&day=${day}`)
}

