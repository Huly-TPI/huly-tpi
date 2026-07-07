import { useState, useEffect, useCallback, useMemo } from 'react'
import {
  pendingApi,
  type PendingTaskResponse,
  type CreatePendingTaskRequest,
  type UpdatePendingTaskRequest,
} from '../api/pending'
import { SessionExpiredError } from '../api/client'

export function usePendingTasks() {
  const [tasks, setTasks] = useState<PendingTaskResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchTasks = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await pendingApi.list()
      setTasks(res)
    } catch (err) {
      if (err instanceof SessionExpiredError) {
        setError('Debés iniciar sesión para ver tus pendientes')
      } else {
        setError('No pudimos cargar tus pendientes. Intentá de nuevo más tarde.')
      }
    } finally {
      setLoading(false)
    }
  }, [])

  const silentRefetch = useCallback(async () => {
    try {
      const res = await pendingApi.list()
      setTasks(res)
    } catch {
      // silent
    }
  }, [])

  useEffect(() => {
    void fetchTasks()
  }, [fetchTasks])

  const createTask = useCallback(
    async (data: CreatePendingTaskRequest): Promise<PendingTaskResponse> => {
      const created = await pendingApi.create(data)
      await silentRefetch()
      return created
    },
    [silentRefetch],
  )

  const updateTask = useCallback(
    async (id: number, data: UpdatePendingTaskRequest) => {
      await pendingApi.update(id, data)
      await silentRefetch()
    },
    [silentRefetch],
  )

  const deleteTask = useCallback(
    async (id: number) => {
      await pendingApi.delete(id)
      await silentRefetch()
    },
    [silentRefetch],
  )

  const completeTask = useCallback(
    async (id: number) => {
      await pendingApi.complete(id)
      await silentRefetch()
    },
    [silentRefetch],
  )

  const placeTask = useCallback(
    async (id: number, positionX: number, positionY: number) => {
      const updated = await pendingApi.updatePosition(id, positionX, positionY)
      setTasks(prev => prev.map(task => (task.id === id ? updated : task)))
      await silentRefetch()
      return updated
    },
    [silentRefetch],
  )

  const addSubtask = useCallback(
    async (taskId: number, text: string) => {
      await pendingApi.addSubtask(taskId, text)
      await silentRefetch()
    },
    [silentRefetch],
  )

  const toggleSubtask = useCallback(
    async (taskId: number, subtaskId: number) => {
      setTasks(prev =>
        prev.map(task =>
          task.id === taskId
            ? {
                ...task,
                subtasks: task.subtasks.map(subtask =>
                  subtask.id === subtaskId ? { ...subtask, done: !subtask.done } : subtask,
                ),
              }
            : task,
        ),
      )
      try {
        await pendingApi.toggleSubtask(taskId, subtaskId)
      } finally {
        await silentRefetch()
      }
    },
    [silentRefetch],
  )

  const deleteSubtask = useCallback(
    async (taskId: number, subtaskId: number) => {
      await pendingApi.deleteSubtask(taskId, subtaskId)
      await silentRefetch()
    },
    [silentRefetch],
  )

  const unplacedTasks = useMemo(
    () => tasks.filter(task => task.status === 'PENDING' && task.positionX === null),
    [tasks],
  )
  const placedTasks = useMemo(
    () => tasks.filter(task => task.positionX !== null),
    [tasks],
  )

  return {
    tasks,
    unplacedTasks,
    placedTasks,
    loading,
    error,
    createTask,
    updateTask,
    deleteTask,
    completeTask,
    placeTask,
    addSubtask,
    toggleSubtask,
    deleteSubtask,
    reload: fetchTasks,
  }
}
