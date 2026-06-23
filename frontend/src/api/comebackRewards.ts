import { api } from './client'

export interface ComebackRewardStatus {
  /** true si el usuario califica para reclamar la recompensa de regreso ahora. */
  available: boolean
  /** Días transcurridos desde su última actividad registrada. */
  daysInactive: number
  /** Monedas que otorga la recompensa (monto configurado). */
  coins: number
  /** Días de inactividad requeridos para que la recompensa esté disponible. */
  thresholdDays: number
}

export interface ClaimComebackRewardResult {
  /** true si se otorgó la recompensa (el usuario calificaba). */
  granted: boolean
  /** Monedas acreditadas (0 si no se otorgó). */
  coins: number
  /** Días de inactividad detectados al momento del reclamo. */
  daysInactive: number
}

export const getComebackRewardStatus = () =>
  api.get<ComebackRewardStatus>('/comeback-rewards/status')

export const claimComebackReward = () =>
  api.post<ClaimComebackRewardResult>('/comeback-rewards/claim', undefined)
