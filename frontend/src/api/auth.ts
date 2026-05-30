import { api } from './client'

export type AuthResponse = {
  accessToken: string
}

export const register = (data: {
  email: string
  password: string
  name: string
  lastname: string
  birthDate: string
}) => {
  return api.post<AuthResponse>('/auth/register', data)
}