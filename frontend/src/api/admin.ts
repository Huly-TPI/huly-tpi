import { api } from './client'

export interface BackofficeUserResponse {
  id: number
  name: string
  email: string
  role: string
  status: string
  birthDate: string | null
  antiScrollEnabled: boolean
  dataSharingConsent: boolean
  mostUsedApp: string | null
  mostUsedAppActiveSeconds: number
  totalScrollTimeSeconds: number
}

export interface TopAppResponse {
  domain: string
  totalActiveSeconds: number
}

export interface AntiScrollDashboardResponse {
  totalModalsShown: number
  totalRedirects: number
  totalUsersCount: number
  activeExtensionUsersCount: number
  dataSharingConsentUsersCount: number
  topUsedApps: TopAppResponse[]
}

export const getBackofficeUsers = async (): Promise<BackofficeUserResponse[]> => {
  return api.get<BackofficeUserResponse[]>('/admin/users')
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
