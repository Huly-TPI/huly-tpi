import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { usePendingTasks } from '../../hooks/usePendingTasks'
import { pendingApi, type PendingTaskResponse } from '../../api/pending'
import { SessionExpiredError } from '../../api/client'

vi.mock('../../api/pending', () => ({
  pendingApi: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    complete: vi.fn(),
    addSubtask: vi.fn(),
    toggleSubtask: vi.fn(),
    deleteSubtask: vi.fn(),
    updatePosition: vi.fn(),
  },
}))

const mockedList = vi.mocked(pendingApi.list)
const mockedCreate = vi.mocked(pendingApi.create)
const mockedUpdatePosition = vi.mocked(pendingApi.updatePosition)

const makeTask = (overrides: Partial<PendingTaskResponse> = {}): PendingTaskResponse => ({
  id: 1,
  title: 'Lavar platos',
  description: null,
  dueDate: null,
  estimatedDuration: null,
  category: null,
  status: 'PENDING',
  subtasks: [],
  positionX: null,
  positionY: null,
  rotationDeg: null,
  pinnedAt: null,
  recommended: false,
  createdAt: '2026-01-01T00:00:00Z',
  completedAt: null,
  ...overrides,
})

describe('usePendingTasks', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('carga las tareas al montar', async () => {
    mockedList.mockResolvedValueOnce([makeTask()])

    const { result } = renderHook(() => usePendingTasks())

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.tasks).toHaveLength(1)
  })

  it('separa tareas ubicadas de no ubicadas', async () => {
    mockedList.mockResolvedValueOnce([
      makeTask({ id: 1, positionX: null, positionY: null }),
      makeTask({ id: 2, positionX: 40, positionY: 50 }),
    ])

    const { result } = renderHook(() => usePendingTasks())
    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.unplacedTasks.map(t => t.id)).toEqual([1])
    expect(result.current.placedTasks.map(t => t.id)).toEqual([2])
  })

  it('setea mensaje de sesión si el fetch lanza SessionExpiredError', async () => {
    mockedList.mockRejectedValueOnce(new SessionExpiredError())

    const { result } = renderHook(() => usePendingTasks())

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.error).toBe('Debés iniciar sesión para ver tus pendientes')
  })

  it('createTask llama a la API, refresca y devuelve la tarea creada', async () => {
    const created = makeTask({ id: 2 })
    mockedList.mockResolvedValueOnce([]).mockResolvedValueOnce([created])
    mockedCreate.mockResolvedValueOnce(created)

    const { result } = renderHook(() => usePendingTasks())
    await waitFor(() => expect(result.current.loading).toBe(false))

    let returned: PendingTaskResponse | undefined
    await act(async () => {
      returned = await result.current.createTask({ title: 'Nueva tarea' })
    })

    expect(mockedCreate).toHaveBeenCalledWith({ title: 'Nueva tarea' })
    expect(returned).toEqual(created)
    expect(mockedList).toHaveBeenCalledTimes(2)
  })

  it('placeTask actualiza la tarea localmente con la respuesta del servidor', async () => {
    const task = makeTask({ id: 1 })
    const placed = makeTask({ id: 1, positionX: 30, positionY: 40, rotationDeg: 2.5, pinnedAt: '2026-01-01T00:00:00Z' })
    mockedList.mockResolvedValueOnce([task]).mockResolvedValue([placed])
    mockedUpdatePosition.mockResolvedValueOnce(placed)

    const { result } = renderHook(() => usePendingTasks())
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.placeTask(1, 30, 40)
    })

    expect(mockedUpdatePosition).toHaveBeenCalledWith(1, 30, 40)
    expect(result.current.tasks[0].rotationDeg).toBe(2.5)
  })
})
