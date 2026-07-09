import { api } from './client'

export interface AdminProduct {
    id: number
    name: string
    description: string
    price: number
    coinsAmount: number | null
    type: string
    planCode: string | null
    chatDailyLimit: number | null
    audioDailyLimit: number | null
    active: boolean
}

export interface ProductFormData {
    name: string
    description: string
    price: number
    coinsAmount: number | null
    type: string
    planCode?: string | null
    chatDailyLimit?: number | null
    audioDailyLimit?: number | null
}

export const adminProductsApi = {
    list: (type = 'COIN_PACK') => api.get<AdminProduct[]>(`/admin/products?type=${type}`),
    create: (data: ProductFormData) => api.post<AdminProduct>('/admin/products', data),
    update: (id: number, data: ProductFormData) => api.put<AdminProduct>(`/admin/products/${id}`, data),
    setActive: (id: number, active: boolean) => api.patch<AdminProduct>(`/admin/products/${id}/active`, { active }),
}