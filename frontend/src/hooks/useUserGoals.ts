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

  const silentRefetch = useCallback(async () => {
    if (!userId) return
    try {
      const res = await userGoalsApi.getByUser(userId)
      setPending(res.pendientes)
      setCompleted(res.completados)
    } catch {
      // silent
    }
  }, [userId])

  useEffect(() => {
    void fetchGoals()
  }, [fetchGoals])

  const createGoal = useCallback(
    async (data: Omit<CreateUserGoalRequest, 'userId'>) => {
      if (!userId) return
      await userGoalsApi.create({ ...data, userId })
      await silentRefetch()
    },
    [userId, silentRefetch],
  )

  const updateGoal = useCallback(
    async (id: number, data: UpdateUserGoalRequest) => {
      await userGoalsApi.update(id, data)
      await silentRefetch()
    },
    [silentRefetch],
  )

  const deleteGoal = useCallback(
    async (id: number) => {
      await userGoalsApi.delete(id)
      await silentRefetch()
    },
    [silentRefetch],
  )

  const completeGoal = useCallback(
    async (id: number) => {
      setPending(prev =>
        prev
          ? { ...prev, content: prev.content.filter(g => g.id !== id), totalElements: Math.max(0, prev.totalElements - 1) }
          : null
      )
      try {
        await userGoalsApi.complete(id)
        await silentRefetch()
      } catch (err) {
        await silentRefetch()
        throw err
      }
    },
    [silentRefetch],
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
