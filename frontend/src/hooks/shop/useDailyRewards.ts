import { useCallback, useEffect, useState } from 'react'
import {
  claimDailyReward,
  getDailyRewardStatus,
  type ClaimDailyRewardResult,
  type DailyRewardStatus,
} from '../../api/dailyRewards'
import { useAuth } from '../../context/auth'

export function useDailyRewards() {
  const { loading: authLoading } = useAuth()
  const [status, setStatus] = useState<DailyRewardStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [claiming, setClaiming] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    return getDailyRewardStatus()
      .then(setStatus)
      .catch(() => setError('No se pudo cargar el calendario de recompensas.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (authLoading) return
    load()
  }, [authLoading, load])

  const claim = useCallback(async (): Promise<ClaimDailyRewardResult | null> => {
    setClaiming(true)
    setError(null)
    try {
      const result = await claimDailyReward()
      await load()
      return result
    } catch (e) {
      const message = (e as { message?: string })?.message
      setError(message ?? 'No se pudo reclamar la recompensa.')
      return null
    } finally {
      setClaiming(false)
    }
  }, [load])

  return {
    status,
    loading,
    claiming,
    error,
    claim,
    clearError: () => setError(null),
  }
}
