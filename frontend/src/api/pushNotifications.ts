import { api } from './client'

interface SubscribeRequest {
    userId: number
    endpoint: string
    p256dh: string
    auth: string
}

export const pushNotificationsApi = {
    subscribe: (data: SubscribeRequest) => api.post<void>('/pushNotification/subscribe', data),
    unsubscribe: (endpoint: string) => api.delete<void>('/pushNotification/unsubscribe', { body: { endpoint } }),
}