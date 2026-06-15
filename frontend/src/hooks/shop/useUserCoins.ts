import { useCallback, useState, useEffect } from 'react'
import { getUserCoins } from '../../api/auth'
import { useAuth } from '../../context/auth'

export function useUserCoins() {
  const { loading: authLoading } = useAuth()
  const [coins, setCoins] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(() => {
    return getUserCoins()
      .then(({ coins: c }) => setCoins(c))
      .catch(() => setError('No se pudieron cargar las monedas.'))
  }, [])

  useEffect(() => {
    if (authLoading) return
    refresh()
  }, [authLoading, refresh])

  return { coins, error, refresh }
}
