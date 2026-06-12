import { api } from './client'

export interface BadgeResponse { 
    id: number
    code: string
    name: string
    description: string | null
    imageUrl: string | null
    createdAt: string
}

export interface UserBadgeResponse {
    id: number
    badge: BadgeResponse
    obtainedAt: string
}

export const badgesApi = {
    getAll: () => api.get<BadgeResponse[]>('/badges'),
    getMyBadges: () => api.get<UserBadgeResponse[]>('/badges/my')
}


