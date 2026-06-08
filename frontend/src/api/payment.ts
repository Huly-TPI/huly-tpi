import { api } from './client'

export interface Product {
  id: string
  name: string
  description: string
  price: number
  coinsAmount: number
}

export interface CreatePreferenceResponse {
  preferenceId: string
  initPoint: string
}

export const getProducts = () => api.get<Product[]>('/payment/products')

export const createPreference = (productId: string) =>
  api.post<CreatePreferenceResponse>(`/payment/preference/${productId}`, {})

export const getMyCoins = () =>
  api.get<{ coins: number }>('/payment/me/coins')
