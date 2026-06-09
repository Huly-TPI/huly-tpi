import { useState, useEffect } from 'react'
import { getUserCoins } from '../../api/auth'

export function useUserCoins() {
  const [coins, setCoins] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getUserCoins()
      .then(({ coins: c }) => setCoins(c))
      .catch(() => setError('No se pudieron cargar las monedas.'))
  }, [])

  return { coins, error }
}
