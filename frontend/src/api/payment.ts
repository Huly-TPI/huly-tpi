import { api } from './client'

export interface Product {
  id: string
  name: string
  description: string
  price: number
  coinsAmount: number
}

export interface Plan {
  id: string
  name: string
  description: string
  price: number
  coinsAmount: number
  planCode: string
  chatDailyLimit: number | null
  audioDailyLimit: number | null
}

export interface CreatePreferenceResponse {
  preferenceId: string
  initPoint: string
}

export const getProducts = () => api.get<Product[]>('/payment/products')

export const getPlans = () => api.get<Plan[]>('/payment/plans')

export const createPreference = (productId: string) =>
  api.post<CreatePreferenceResponse>(`/payment/preference/${productId}`, {})

export const createStoreItemPreference = (storeItemId: string) =>
  api.post<CreatePreferenceResponse>(`/payment/store-preference/${storeItemId}`, {})
