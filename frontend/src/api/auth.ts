import { api } from './client'

export interface AuthResponse {
  accessToken: string
  onBoardingCompleted?: boolean
  role: string
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
  birthDate: string
}

export interface LoginRequest {
  email: string
  password: string
}
export interface UserProfile {
  id: number
  name: string
  email: string
  birthDate?: string | null
  role: string
  onBoardingCompleted?: boolean
  onboardingTutorialCompleted?: boolean
  profileOnboardingTutorialCompleted?: boolean
  themePreference: 'LIGHT' | 'DARK'
  audioSettings?: AudioSettings
}

export interface AudioSettings {
  interfaceVolume: number
  ambientVolume: number
  minigameVolume: number
}

export interface AccountSettings {
  name: string
  email: string
  birthDate: string | null
}

export interface UpdateAccountSettingsRequest {
  name: string
  birthDate?: string | null
}

export const getMe = () => {
  return api.get<UserProfile>('/users/me')
}

export const register = (data: RegisterRequest) => {
  return api.post<AuthResponse>('/auth/register', data, { skipAuthRedirect: true })
}

export const login = (data: LoginRequest) => {
  return api.post<AuthResponse>('/auth/login', data, { skipAuthRedirect: true })
}

export const backofficeLogin = (data: LoginRequest) => {
  return api.post<AuthResponse>('/auth/backoffice/login', data, { skipAuthRedirect: true })
}

export const logout = () => {
  return api.post<void>('/auth/logout', {})
}

export const updateThemePreference = (themePreference: UserProfile['themePreference']) => {
  return api.put<void>('/users/me/theme', { themePreference })
}

export const updateAudioSettings = (audioSettings: AudioSettings) => {
  return api.put<AudioSettings>('/users/me/audio-settings', audioSettings)
}

export const getAccountSettings = () => {
  return api.get<AccountSettings>('/users/me/settings')
}

export const updateAccountSettings = (data: UpdateAccountSettingsRequest) => {
  return api.put<AccountSettings>('/users/me/settings', data)
}

export const getUserCoins = () =>
  api.get<{ coins: number }>('/users/me/coins')

export interface Membership {
  active: boolean
  planCode: string | null
  productId: string | null
  expiresAt: string | null
}

export const getMyMembership = () =>
  api.get<Membership>('/users/me/membership')

export const forgotPassword = (email: string) =>
  api.post<void>('/auth/forgot-password', { email }, { skipAuthRedirect: true })

export const resetPassword = (token: string, newPassword: string) =>
  api.post<void>('/auth/reset-password', { token, newPassword }, { skipAuthRedirect: true })

export const changePassword = (currentPassword: string, newPassword: string) =>
  api.put<void>('/users/me/password', { currentPassword, newPassword })
