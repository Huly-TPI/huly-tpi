import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useUserGoals } from '../../hooks/useUserGoals'
import { userGoalsApi } from '../../api/userGoals'

vi.mock('../../api/userGoals', () => ({
  userGoalsApi: {
    getForCurrentUser: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    complete: vi.fn(),
  },
}))

const mockedGetForCurrentUser = vi.mocked(userGoalsApi.getForCurrentUser)
const mockedCreate = vi.mocked(userGoalsApi.create)
const mockedUpdate = vi.mocked(userGoalsApi.update)
const mockedDelete = vi.mocked(userGoalsApi.delete)
const mockedComplete = vi.mocked(userGoalsApi.complete)

const makeGoal = (overrides = {}) => ({
  id: 1,
  userId: 10,
  title: 'Reto de prueba',
  description: null,
  status: 'PENDING' as 'PENDING' | 'COMPLETED',
  createdAt: '2026-01-01T00:00:00Z',
  activityId: null,
  ...overrides,
})

const makePageResponse = (goals: ReturnType<typeof makeGoal>[] = []) => ({
  content: goals,
  pageNumber: 0,
  pageSize: 50,
  totalElements: goals.length,
  totalPages: 1,
  first: true,
  last: true,
})

const makeListResponse = (
  pendientes: ReturnType<typeof makeGoal>[] = [],
  completados: ReturnType<typeof makeGoal>[] = [],
) => ({
  pendientes: makePageResponse(pendientes),
  completados: makePageResponse(completados),
})

describe('useUserGoals', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('carga los goals al montar', async () => {
    const goal = makeGoal()
    mockedGetForCurrentUser.mockResolvedValueOnce(makeListResponse([goal], []) as never)

    const { result } = renderHook(() => useUserGoals())

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(mockedGetForCurrentUser).toHaveBeenCalledWith()
    expect(result.current.pendientes?.content).toEqual([goal])
    expect(result.current.completados?.content).toEqual([])
  })

  it('setea loading en true durante el fetch inicial', async () => {
    let resolvePromise: (v: unknown) => void
    mockedGetForCurrentUser.mockReturnValueOnce(new Promise(r => { resolvePromise = r }) as never)

    const { result } = renderHook(() => useUserGoals())
    expect(result.current.loading).toBe(true)

    act(() => resolvePromise(makeListResponse()))
    await waitFor(() => expect(result.current.loading).toBe(false))
  })

  it('setea error si el fetch falla', async () => {
    mockedGetForCurrentUser.mockRejectedValueOnce(new Error('Error de red'))

    const { result } = renderHook(() => useUserGoals())

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.error).toBe('Error de red')
  })

  it('createGoal llama a la API y refresca los datos', async () => {
    mockedGetForCurrentUser
      .mockResolvedValueOnce(makeListResponse([], []) as never)
      .mockResolvedValueOnce(makeListResponse([makeGoal()], []) as never)
    mockedCreate.mockResolvedValueOnce(makeGoal() as never)

    const { result } = renderHook(() => useUserGoals())
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.createGoal({ title: 'Nuevo reto' })
    })

    expect(mockedCreate).toHaveBeenCalledWith({ title: 'Nuevo reto' })
    expect(mockedGetForCurrentUser).toHaveBeenCalledTimes(2)
  })

  it('updateGoal llama a la API con el id y datos correctos', async () => {
    mockedGetForCurrentUser.mockResolvedValue(makeListResponse([makeGoal()], []) as never)
    mockedUpdate.mockResolvedValueOnce(makeGoal() as never)

    const { result } = renderHook(() => useUserGoals())
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.updateGoal(1, { title: 'Actualizado' })
    })

    expect(mockedUpdate).toHaveBeenCalledWith(1, { title: 'Actualizado' })
  })

  it('deleteGoal llama a la API y refresca los datos', async () => {
    mockedGetForCurrentUser
      .mockResolvedValueOnce(makeListResponse([makeGoal()], []) as never)
      .mockResolvedValueOnce(makeListResponse([], []) as never)
    mockedDelete.mockResolvedValueOnce(undefined as never)

    const { result } = renderHook(() => useUserGoals())
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.deleteGoal(1)
    })

    expect(mockedDelete).toHaveBeenCalledWith(1)
    expect(mockedGetForCurrentUser).toHaveBeenCalledTimes(2)
  })

  it('completeGoal elimina el goal de pendientes de forma optimista', async () => {
    const goal = makeGoal({ id: 5 })
    mockedGetForCurrentUser.mockResolvedValueOnce(makeListResponse([goal], []) as never)
    mockedComplete.mockResolvedValueOnce({ ...goal, status: 'COMPLETED' } as never)
    mockedGetForCurrentUser.mockResolvedValueOnce(makeListResponse([], [{ ...goal, status: 'COMPLETED' as const }]) as never)

    const { result } = renderHook(() => useUserGoals())
    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.pendientes?.content).toHaveLength(1)

    await act(async () => {
      await result.current.completeGoal(5)
    })

    expect(result.current.pendientes?.content).toHaveLength(0)
    expect(mockedComplete).toHaveBeenCalledWith(5)
  })

  it('completeGoal refresca silenciosamente tras el error y relanza la excepción', async () => {
    const goal = makeGoal({ id: 5 })
    mockedGetForCurrentUser.mockResolvedValue(makeListResponse([goal], []) as never)
    mockedComplete.mockRejectedValueOnce(new Error('Fallo al completar'))

    const { result } = renderHook(() => useUserGoals())
    await waitFor(() => expect(result.current.loading).toBe(false))

    await expect(
      act(async () => {
        await result.current.completeGoal(5)
      })
    ).rejects.toThrow('Fallo al completar')
  })
})
