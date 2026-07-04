import { api } from './client'
import type { StoreItemResponse } from './store'

export interface StoreItemFormData {
    name: string
    description: string
    category: string
    priceCoins: number
    price: number | null
    premiumOnly: boolean
    imageLight: File | null
    imageDark: File | null
}

function toFormData(data: StoreItemFormData): FormData {
    const fd = new FormData()
    fd.append('name', data.name)
    fd.append('description', data.description)
    fd.append('category', data.category)
    fd.append('priceCoins', String(data.priceCoins))
    if (data.price != null) fd.append('price', String(data.price))
    fd.append('premiumOnly', String(data.premiumOnly))
    if (data.imageLight) fd.append('imageLight', data.imageLight)
    if (data.imageDark) fd.append('imageDark', data.imageDark)
    return fd
}

export const adminStoreApi = {
    create: (data: StoreItemFormData) =>
        api.postMultipart<StoreItemResponse>('/admin/store/items', toFormData(data)),
    update: (id: number, data: StoreItemFormData) =>
        api.put<StoreItemResponse>(`/admin/store/items/${id}`, toFormData(data)),
    remove: (id: number) =>
        api.delete<void>(`/admin/store/items/${id}`),
}