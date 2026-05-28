import { api } from './client'

type LoginResponse = {
  accessToken: string
}

export const login = async (email: string, password: string) => {
  return api.post<LoginResponse>('/auth/login', {
    email,
    password,
  })
}