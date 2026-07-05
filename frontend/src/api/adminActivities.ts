import { api, type RequestOptions } from './client'
import { ActivityType } from './activities'

export interface AdminActivityConfig {
  id: number
  type: ActivityType
  valenceMin: number
  valenceMax: number
  arousalMin: number
  arousalMax: number
  dominanceMin: number
  dominanceMax: number
  effectValence: number
  effectArousal: number
  effectDominance: number
  title: string
  description: string
  goalKeywords: string
  routePath: string
}

export interface AdminActivityMetric {
  activityType: ActivityType
  activityName: string
  totalRecommendations: number
  acceptanceRate: number
  rejectionRate: number
  averageSatisfaction: number
  totalSessions: number
  moodImprovementRate: number
  averageValenceChange: number
  averageArousalChange: number
  decisionsDistribution: Record<string, number>
  emotionDistribution: Record<string, number>
}

export interface AdminActivitiesKpiResponse {
  totalSessions: number
  topActivity: {
    type: string
    sessions: number
  }
  averageMoodImprovement: number
}

export interface AdminActivityPopularityResponse {
  activityType: string
  activityName: string
  totalSessions: number
}

export interface AdminActivityCorrelationResponse {
  activityType: string
  emotion: string
  suggestionsCount: number
  acceptanceRate: number
}

export interface AdminActivityImpactResponse {
  activityType: string
  averageValenceChange: number
  averageArousalChange: number
  basedOnMetrics: boolean
}

export const adminActivitiesApi = {
  getActivitiesConfig: (options?: RequestOptions) =>
    api.get<AdminActivityConfig[]>('/admin/activities', options),

  updateActivityConfig: (id: number, data: Omit<AdminActivityConfig, 'id' | 'type'>, options?: RequestOptions) =>
    api.put<AdminActivityConfig>(`/admin/activities/${id}`, data, options),

  getKpis: (timeframe?: string, options?: RequestOptions) => {
    const url = timeframe ? `/admin/activities/kpis?timeframe=${timeframe}` : '/admin/activities/kpis'
    return api.get<AdminActivitiesKpiResponse>(url, options)
  },

  getPopularity: (timeframe?: string, options?: RequestOptions) => {
    const url = timeframe ? `/admin/activities/popularity?timeframe=${timeframe}` : '/admin/activities/popularity'
    return api.get<AdminActivityPopularityResponse[]>(url, options)
  },

  getCorrelation: (timeframe?: string, options?: RequestOptions) => {
    const url = timeframe ? `/admin/activities/correlation?timeframe=${timeframe}` : '/admin/activities/correlation'
    return api.get<AdminActivityCorrelationResponse[]>(url, options)
  },

  getImpact: (timeframe?: string, options?: RequestOptions) => {
    const url = timeframe ? `/admin/activities/impact?timeframe=${timeframe}` : '/admin/activities/impact'
    return api.get<AdminActivityImpactResponse[]>(url, options)
  },
}
