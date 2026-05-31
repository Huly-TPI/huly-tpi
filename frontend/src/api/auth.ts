import { api } from './client'

export interface AuthResponse {
  accessToken: string
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

export const register = (data: RegisterRequest) => {
  return api.post<AuthResponse>('/auth/register', data)
}

export const login = (data: LoginRequest) => {
  return api.post<AuthResponse>('/auth/login', data)
}

export const logout = () => {
  return api.post<void>('/auth/logout', {})
}