import { useCallback, useEffect, useState } from 'react'
import {
  claimComebackReward,
  getComebackRewardStatus,
  type ClaimComebackRewardResult,
  type ComebackRewardStatus,
} from '../../api/comebackRewards'
import { useAuth } from '../../context/auth'

export function useComebackReward() {
  const { loading: authLoading } = useAuth()
  const [status, setStatus] = useState<ComebackRewardStatus | null>(null)
  const [claiming, setClaiming] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (authLoading) return
    getComebackRewardStatus()
      .then(setStatus)
      .catch(() => {
        // Falla silenciosa: si el status no carga no interrumpimos el home.
      })
  }, [authLoading])

  const claim = useCallback(async (): Promise<ClaimComebackRewardResult | null> => {
    setClaiming(true)
    setError(null)
    try {
      return await claimComebackReward()
    } catch (e) {
      const message = (e as { message?: string })?.message
      setError(message ?? 'No se pudo reclamar la recompensa.')
      return null
    } finally {
      setClaiming(false)
    }
  }, [])

  return { status, claiming, error, claim }
}
