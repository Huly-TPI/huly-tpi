import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useUserGoals } from '../../hooks/useUserGoals'
import { userGoalsApi } from '../../api/userGoals'
import { SessionExpiredError } from '../../api/client'

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







describe('useUserGoals', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('carga los goals al montar', () => {
    setupGetGoalsResolved(makeListResponse([makeGoal()], []))
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyGetGoalsCalled()
      verifyPendientes([makeGoal()])
      verifyCompletados([])
    })
  })

  it('setea loading en true durante el fetch inicial', () => {
    setupGetGoalsDeferred()
    setupHook()
    verifyLoading(true)
    resolveDeferredGetGoals(makeListResponse())
    return waitForLoadingFinished()
  })

  it('setea error genérico si el fetch falla', () => {
    setupGetGoalsRejected(new Error('Error de red'))
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyError('No pudimos cargar tus retos. Intentá de nuevo más tarde.')
    })
  })

  it('setea mensaje de sesión si el fetch lanza SessionExpiredError', () => {
    setupGetGoalsRejected(new SessionExpiredError())
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyError('Debés iniciar sesión para ver tus retos')
    })
  })

  it('createGoal llama a la API y refresca los datos', () => {
    setupGetGoalsMultiple(makeListResponse([], []), makeListResponse([makeGoal()], []))
    setupCreateResolved(makeGoal())
    setupHook()
    return waitForLoadingFinished()
      .then(() => callCreateGoal({ title: 'Nuevo reto' }))
      .then(() => {
        verifyCreateCalledWith({ title: 'Nuevo reto' })
        verifyGetGoalsCalledTimes(2)
      })
  })

  it('updateGoal llama a la API con el id y datos correctos', () => {
    setupGetGoalsResolved(makeListResponse([makeGoal()], []))
    setupUpdateResolved(makeGoal())
    setupHook()
    return waitForLoadingFinished()
      .then(() => callUpdateGoal(1, { title: 'Actualizado' }))
      .then(() => {
        verifyUpdateCalledWith(1, { title: 'Actualizado' })
      })
  })

  it('deleteGoal llama a la API y refresca los datos', () => {
    setupGetGoalsMultiple(makeListResponse([makeGoal()], []), makeListResponse([], []))
    setupDeleteResolved()
    setupHook()
    return waitForLoadingFinished()
      .then(() => callDeleteGoal(1))
      .then(() => {
        verifyDeleteCalledWith(1)
        verifyGetGoalsCalledTimes(2)
      })
  })

  it('completeGoal elimina el goal de pendientes de forma optimista', () => {
    const goal = makeGoal({ id: 5 })
    setupGetGoalsMultiple(
      makeListResponse([goal], []),
      makeListResponse([], [{ ...goal, status: 'COMPLETED' }])
    )
    setupCompleteResolved({ ...goal, status: 'COMPLETED' })
    setupHook()
    return waitForLoadingFinished()
      .then(() => {
        verifyPendientesLength(1)
        return callCompleteGoal(5)
      })
      .then(() => {
        verifyPendientesLength(0)
        verifyCompleteCalledWith(5, undefined)
      })
  })

  it('completeGoal refresca silenciosamente tras el error y relanza la excepción', () => {
    const goal = makeGoal({ id: 5 })
    setupGetGoalsResolved(makeListResponse([goal], []))
    setupCompleteRejected('Fallo al completar')
    setupHook()
    return waitForLoadingFinished()
      .then(() => {
        return verifyCompleteGoalRejects(5, 'Fallo al completar')
      })
  })
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useUserGoals>, undefined>>
  let deferredResolver: (v: unknown) => void

  /* helpers */

  const setupHook = () => {
    rendered = renderHook(() => useUserGoals())
  }

  const setupGetGoalsResolved = (val: any) => {
    mockedGetForCurrentUser.mockResolvedValue(val as never)
  }

  const setupGetGoalsMultiple = (res1: any, res2: any) => {
    mockedGetForCurrentUser.mockResolvedValueOnce(res1 as never).mockResolvedValueOnce(res2 as never)
  }

  const setupGetGoalsRejected = (err: Error) => {
    mockedGetForCurrentUser.mockRejectedValue(err as never)
  }

  const setupGetGoalsDeferred = () => {
    mockedGetForCurrentUser.mockReturnValueOnce(new Promise(r => { deferredResolver = r }) as never)
  }

  const resolveDeferredGetGoals = (val: any) => {
    act(() => {
      deferredResolver(val)
    })
  }

  const setupCreateResolved = (val: any) => {
    mockedCreate.mockResolvedValueOnce(val as never)
  }

  const setupUpdateResolved = (val: any) => {
    mockedUpdate.mockResolvedValueOnce(val as never)
  }

  const setupDeleteResolved = () => {
    mockedDelete.mockResolvedValueOnce(undefined as never)
  }

  const setupCompleteResolved = (val: any) => {
    mockedComplete.mockResolvedValueOnce(val as never)
  }

  const setupCompleteRejected = (msg: string) => {
    mockedComplete.mockRejectedValueOnce(new Error(msg))
  }

  const waitForLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.loading).toBe(false))
  }

  const callCreateGoal = (goalData: any) => {
    return act(() => rendered.result.current.createGoal(goalData))
  }

  const callUpdateGoal = (id: number, goalData: any) => {
    return act(() => rendered.result.current.updateGoal(id, goalData))
  }

  const callDeleteGoal = (id: number) => {
    return act(() => rendered.result.current.deleteGoal(id))
  }

  const callCompleteGoal = (id: number) => {
    return act(() => rendered.result.current.completeGoal(id))
  }

  const verifyCompleteGoalRejects = (id: number, expectedMsg: string) => {
    return expect(
      act(() => rendered.result.current.completeGoal(id))
    ).rejects.toThrow(expectedMsg)
  }

  const verifyGetGoalsCalled = () => {
    expect(mockedGetForCurrentUser).toHaveBeenCalled()
  }

  const verifyGetGoalsCalledTimes = (times: number) => {
    expect(mockedGetForCurrentUser).toHaveBeenCalledTimes(times)
  }

  const verifyPendientes = (expected: any[]) => {
    expect(rendered.result.current.pendientes?.content).toEqual(expected)
  }

  const verifyPendientesLength = (len: number) => {
    expect(rendered.result.current.pendientes?.content).toHaveLength(len)
  }

  const verifyCompletados = (expected: any[]) => {
    expect(rendered.result.current.completados?.content).toEqual(expected)
  }

  const verifyLoading = (expected: boolean) => {
    expect(rendered.result.current.loading).toBe(expected)
  }

  const verifyError = (expected: string) => {
    expect(rendered.result.current.error).toBe(expected)
  }

  const verifyCreateCalledWith = (expected: any) => {
    expect(mockedCreate).toHaveBeenCalledWith(expected)
  }

  const verifyUpdateCalledWith = (id: number, expected: any) => {
    expect(mockedUpdate).toHaveBeenCalledWith(id, expected)
  }

  const verifyDeleteCalledWith = (id: number) => {
    expect(mockedDelete).toHaveBeenCalledWith(id)
  }

  const verifyCompleteCalledWith = (id: number, secondArg: any) => {
    expect(mockedComplete).toHaveBeenCalledWith(id, secondArg)
  }
})

function makeGoal(overrides = {}) {
  return ({
  id: 1,
  userId: 10,
  title: 'Reto de prueba',
  description: null,
  status: 'PENDING' as 'PENDING' | 'COMPLETED',
  createdAt: '2026-01-01T00:00:00Z',
  activityId: null,
  ...overrides,
})
}

function makePageResponse(goals: ReturnType<typeof makeGoal>[] = []) {
  return ({
  content: goals,
  pageNumber: 0,
  pageSize: 50,
  totalElements: goals.length,
  totalPages: 1,
  first: true,
  last: true,
})
}

function makeListResponse(pendientes: ReturnType<typeof makeGoal>[] = [], completados: ReturnType<typeof makeGoal>[] = []) {
  return ({
  pendientes: makePageResponse(pendientes),
  completados: makePageResponse(completados),
})
}
