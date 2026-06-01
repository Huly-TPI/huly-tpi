import { api } from './client'

export interface AuthResponse {
  accessToken: string
  profileOnBoardingCompleted?: boolean
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
  role: string
}

export const getMe = () => {
  return api.get<UserProfile>('/users/me')
}

export const register = (data: RegisterRequest) => {
  return api.post<AuthResponse>('/auth/register', data)
}

export const login = (data: LoginRequest) => {
  return api.post<AuthResponse>('/auth/login', data)
}

export const backofficeLogin = (data: LoginRequest) => {
  return api.post<AuthResponse>('/auth/backoffice/login', data, { skipAuthRedirect: true })
}

export const logout = () => {
  return api.post<void>('/auth/logout', {})
}