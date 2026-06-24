import { api } from './client'

export interface StoreItemResponse {
    id: number
    name: string
    description: string
    category: string
    assetKey: string
    priceCoins: number
    price: number | null
    premiumOnly: boolean
}

export interface InventoryItemResponse {
    storeItemId: number
    name: string
    category: string
    assetKey: string
    equipped: boolean
}

export const storeApi = {
    getItems: () => api.get<StoreItemResponse[]>('/store/items'),
    getInventory: () => api.get<InventoryItemResponse[]>('/store/inventory'),
    buy: (id: number) => api.post<void>(`/store/items/${id}/buy`, {}),
    equip: (id: number) => api.post<void>(`/store/items/${id}/equip`, {}),
    unequip: (id: number) => api.post<void>(`/store/items/${id}/unequip`, {}),
}