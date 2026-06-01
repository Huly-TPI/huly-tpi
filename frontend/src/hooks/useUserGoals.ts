import { useState, useEffect, useCallback } from 'react'
import {
  userGoalsApi,
  type UserGoalPageResponse,
  type CreateUserGoalRequest,
  type UpdateUserGoalRequest,
} from '../api/userGoals'

export function useUserGoals(userId: number | null) {
  const [pendientes, setPending] = useState<UserGoalPageResponse | null>(null)
  const [completados, setCompleted] = useState<UserGoalPageResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchGoals = useCallback(async () => {
    if (!userId) return
    setLoading(true)
    setError(null)
    try {
      const res = await userGoalsApi.getByUser(userId)
      setPending(res.pendientes)
      setCompleted(res.completados)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar los retos')
    } finally {
      setLoading(false)
    }
  }, [userId])

  useEffect(() => {
    void fetchGoals()
  }, [fetchGoals])

  const createGoal = useCallback(
    async (data: Omit<CreateUserGoalRequest, 'userId'>) => {
      if (!userId) return
      await userGoalsApi.create({ ...data, userId })
      await fetchGoals()
    },
    [userId, fetchGoals],
  )

  const updateGoal = useCallback(
    async (id: number, data: UpdateUserGoalRequest) => {
      await userGoalsApi.update(id, data)
      await fetchGoals()
    },
    [fetchGoals],
  )

  const deleteGoal = useCallback(
    async (id: number) => {
      await userGoalsApi.delete(id)
      await fetchGoals()
    },
    [fetchGoals],
  )

  const completeGoal = useCallback(
    async (id: number) => {
      await userGoalsApi.complete(id)
      await fetchGoals()
    },
    [fetchGoals],
  )

  return {
    pendientes,
    completados,
    loading,
    error,
    createGoal,
    updateGoal,
    deleteGoal,
    completeGoal,
    reload: fetchGoals,
  }
}
