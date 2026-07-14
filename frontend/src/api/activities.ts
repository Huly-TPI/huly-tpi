import { api, type RequestOptions } from './client'

export enum ActivityType {
  BREATHING = 'BREATHING',
  DIARY = 'DIARY',
  LANTERN = 'LANTERN',
  BUBBLE = 'BUBBLE',
  CHALLENGE = 'CHALLENGE',
  ZEN_GARDEN = 'ZEN_GARDEN',
  MANDALA = 'MANDALA',
  STONES = 'STONES',
  PENDING = 'PENDING',
}

export interface RegisterActivitySessionRequest {
  activityType: ActivityType
  contextId?: string
}

export const registerActivitySession = (
  data: RegisterActivitySessionRequest,
  options?: RequestOptions,
) => api.post<null>('/activities/sessions', data, options)
