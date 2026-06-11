import { useState, useEffect, useCallback } from 'react'
import {
  userGoalsApi,
  type UserGoalPageResponse,
  type CreateUserGoalRequest,
  type UpdateUserGoalRequest,
} from '../api/userGoals'
import { SessionExpiredError } from '../api/client'

export function useUserGoals() {
  const [pendientes, setPending] = useState<UserGoalPageResponse | null>(null)
  const [completados, setCompleted] = useState<UserGoalPageResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchGoals = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await userGoalsApi.getForCurrentUser()
      setPending(res.pendientes)
      setCompleted(res.completados)
    } catch (err) {
      if (err instanceof SessionExpiredError) {
        setError('Debés iniciar sesión para ver tus retos')
      } else {
        setError('No pudimos cargar tus retos. Intentá de nuevo más tarde.')
      }
    } finally {
      setLoading(false)
    }
  }, [])

  const silentRefetch = useCallback(async () => {
    try {
      const res = await userGoalsApi.getForCurrentUser()
      setPending(res.pendientes)
      setCompleted(res.completados)
    } catch {
      // silent
    }
  }, [])

  useEffect(() => {
    void fetchGoals()
  }, [fetchGoals])

  const createGoal = useCallback(
    async (data: CreateUserGoalRequest) => {
      await userGoalsApi.create(data)
      await silentRefetch()
    },
    [silentRefetch],
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
