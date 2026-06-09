import { api, type RequestOptions } from './client'

export enum ActivityType {
  RESPIRACION = 'RESPIRACION',
  DIARIO = 'DIARIO',
  NUBE = 'NUBE',
  BURBUJA = 'BURBUJA',
  RETO = 'RETO',
}

export interface RegisterActivitySessionRequest {
  activityType: ActivityType
}

export const registerActivitySession = (
  data: RegisterActivitySessionRequest,
  options?: RequestOptions,
) => api.post<null>('/activities/sessions', data, options)
