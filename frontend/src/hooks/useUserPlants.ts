import { useState, useEffect, useCallback } from 'react'
import { userPlantsApi } from '../api/userPlants'
import { type UserPlantSummaryResponse } from '../api/userGoals'
import { SessionExpiredError } from '../api/client'

export function useUserPlants() {
  const [plants, setPlants] = useState<UserPlantSummaryResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchPlants = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await userPlantsApi.getAll()
      setPlants(data)
    } catch (err) {
      if (err instanceof SessionExpiredError) {
        setError('Debés iniciar sesión para ver tu vivero')
      } else {
        setError('No pudimos cargar tus plantas. Intentá de nuevo más tarde.')
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void fetchPlants()
  }, [fetchPlants])

  return { plants, loading, error, reload: fetchPlants }
}
