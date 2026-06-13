import { useState, useEffect } from 'react'
import { getPlans, type Plan } from '../../api/payment'
import { useAuth } from '../../context/auth'

export function usePlans() {
  const { loading: authLoading } = useAuth()
  const [plans, setPlans] = useState<Plan[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (authLoading) return
    getPlans()
      .then(setPlans)
      .catch(() => setError('No se pudieron cargar los planes.'))
      .finally(() => setLoading(false))
  }, [authLoading])

  return { plans, loading, error }
}
