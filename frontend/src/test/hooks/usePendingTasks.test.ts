import { clearAllMocks } from '../testHelpers'
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

describe('usePendingTasks', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('carga las tareas al montar', () => {
    setupListResolved([makeTask()])
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyTasksLength(1)
    })
  })

  it('separa tareas ubicadas de no ubicadas', () => {
    setupListResolved([
      makeTask({ id: 1, positionX: null, positionY: null }),
      makeTask({ id: 2, positionX: 40, positionY: 50 }),
    ])
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyUnplacedTaskIds([1])
      verifyPlacedTaskIds([2])
    })
  })

  it('setea mensaje de sesión si el fetch lanza SessionExpiredError', () => {
    setupListRejected(new SessionExpiredError())
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyError('Debés iniciar sesión para ver tus pendientes')
    })
  })

  it('createTask llama a la API, refresca y devuelve la tarea creada', () => {
    const created = makeTask({ id: 2 })
    setupListMultiple([], [created])
    setupCreateResolved(created)
    setupHook()

    return waitForLoadingFinished()
      .then(() => callCreateTask({ title: 'Nueva tarea' }))
      .then(returned => {
        verifyCreateCalledWith({ title: 'Nueva tarea' })
        expect(returned).toEqual(created)
        verifyListCalledTimes(2)
      })
  })

  it('placeTask actualiza la tarea localmente con la respuesta del servidor', () => {
    const task = makeTask({ id: 1 })
    const placed = makeTask({ id: 1, positionX: 30, positionY: 40, rotationDeg: 2.5, pinnedAt: '2026-01-01T00:00:00Z' })
    setupListMultiple([task], [placed])
    setupUpdatePositionResolved(placed)
    setupHook()

    return waitForLoadingFinished()
      .then(() => callPlaceTask(1, 30, 40))
      .then(() => {
        verifyUpdatePositionCalledWith(1, 30, 40)
        verifyFirstTaskRotation(2.5)
      })
  })

  let rendered: ReturnType<typeof renderHook<ReturnType<typeof usePendingTasks>, undefined>>

  /* helpers */

  const setupHook = () => {
    rendered = renderHook(() => usePendingTasks())
  }

  const setupListResolved = (tasks: PendingTaskResponse[]) => {
    mockedList.mockResolvedValueOnce(tasks)
  }

  const setupListMultiple = (first: PendingTaskResponse[], second: PendingTaskResponse[]) => {
    mockedList.mockResolvedValueOnce(first).mockResolvedValue(second)
  }

  const setupListRejected = (err: Error) => {
    mockedList.mockRejectedValueOnce(err)
  }

  const setupCreateResolved = (val: PendingTaskResponse) => {
    mockedCreate.mockResolvedValueOnce(val)
  }

  const setupUpdatePositionResolved = (val: PendingTaskResponse) => {
    mockedUpdatePosition.mockResolvedValueOnce(val)
  }

  const waitForLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.loading).toBe(false))
  }

  const callCreateTask = async (data: { title: string }): Promise<PendingTaskResponse> => {
    let returned: PendingTaskResponse | undefined
    await act(async () => {
      returned = await rendered.result.current.createTask(data)
    })
    return returned as PendingTaskResponse
  }

  const callPlaceTask = async (id: number, positionX: number, positionY: number) => {
    await act(async () => {
      await rendered.result.current.placeTask(id, positionX, positionY)
    })
  }

  const verifyTasksLength = (len: number) => {
    expect(rendered.result.current.tasks).toHaveLength(len)
  }

  const verifyUnplacedTaskIds = (ids: number[]) => {
    expect(rendered.result.current.unplacedTasks.map(t => t.id)).toEqual(ids)
  }

  const verifyPlacedTaskIds = (ids: number[]) => {
    expect(rendered.result.current.placedTasks.map(t => t.id)).toEqual(ids)
  }

  const verifyError = (expected: string) => {
    expect(rendered.result.current.error).toBe(expected)
  }

  const verifyCreateCalledWith = (expected: any) => {
    expect(mockedCreate).toHaveBeenCalledWith(expected)
  }

  const verifyListCalledTimes = (times: number) => {
    expect(mockedList).toHaveBeenCalledTimes(times)
  }

  const verifyUpdatePositionCalledWith = (id: number, positionX: number, positionY: number) => {
    expect(mockedUpdatePosition).toHaveBeenCalledWith(id, positionX, positionY)
  }

  const verifyFirstTaskRotation = (expected: number) => {
    expect(rendered.result.current.tasks[0].rotationDeg).toBe(expected)
  }
})

function makeTask(overrides: Partial<PendingTaskResponse> = {}): PendingTaskResponse {
  return {
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
  }
}
