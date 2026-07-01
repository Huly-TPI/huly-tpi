import { api, type RequestOptions } from './client'

export enum ActivityType {
  RESPIRACION = 'RESPIRACION',
  DIARIO = 'DIARIO',
  NUBE = 'NUBE',
  BURBUJA = 'BURBUJA',
  RETO = 'RETO',
  ARENA_ZEN = 'ARENA_ZEN',
  MANDALA = 'MANDALA',
}

export interface RegisterActivitySessionRequest {
  activityType: ActivityType
  contextId?: string
}

export const registerActivitySession = (
  data: RegisterActivitySessionRequest,
  options?: RequestOptions,
) => api.post<null>('/activities/sessions', data, options)
