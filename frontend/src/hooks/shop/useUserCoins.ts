import { useState, useEffect, useCallback } from 'react'
import { getUserCoins } from '../../api/auth'
import { useAuth } from '../../context/auth'

export function useUserCoins() {
  const { loading: authLoading } = useAuth()
  const [coins, setCoins] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const refetch = useCallback(() => {
    return getUserCoins()
      .then(({ coins: c }) => setCoins(c))
      .catch(() => setError('No se pudieron cargar las monedas.'))
  }, [])
  
  
  useEffect(() => {
    if (authLoading) return
    refetch() 
  }, [authLoading, refetch])

  return { coins, error, refetch }
}
