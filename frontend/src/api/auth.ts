import { api } from './client'

export interface AuthResponse {
  accessToken: string
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
  birthDate: string
}

export const register = (data: RegisterRequest) => {
  return api.post<AuthResponse>('/auth/register', data)
}