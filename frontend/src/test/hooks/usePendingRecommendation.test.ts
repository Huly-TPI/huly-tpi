import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { usePendingRecommendation } from '../../hooks/usePendingRecommendation'
import { pendingApi, type PendingRecommendationResponse } from '../../api/pending'

vi.mock('../../api/pending', () => ({
  pendingApi: {
    getTodayRecommendation: vi.fn(),
    respondToRecommendation: vi.fn(),
    generateRecommendation: vi.fn(),
  },
}))

const mockedGetToday = vi.mocked(pendingApi.getTodayRecommendation)
const mockedRespond = vi.mocked(pendingApi.respondToRecommendation)
const mockedGenerate = vi.mocked(pendingApi.generateRecommendation)

describe('usePendingRecommendation', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('no muestra banner cuando no hay recomendación', () => {
    setupGetTodayResolved(null)
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyHasUnrespondedRecommendation(false)
    })
  })

  it('muestra banner cuando hay una recomendación pendiente de respuesta', () => {
    setupGetTodayResolved(makeRecommendation())
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyHasUnrespondedRecommendation(true)
    })
  })

  it('accept actualiza el estado y expone los ids recomendados', () => {
    setupGetTodayResolved(makeRecommendation())
    setupRespondResolved(makeRecommendation({ decision: 'ACCEPTED' }))
    setupHook()

    return waitForLoadingFinished()
      .then(() => callAccept())
      .then(() => {
        verifyRespondCalledWith(1, 'ACCEPTED')
        verifyHasUnrespondedRecommendation(false)
        verifyRecommendedTaskIds(new Set([1, 2]))
      })
  })

  it('reject actualiza el estado y no expone ids recomendados', () => {
    setupGetTodayResolved(makeRecommendation())
    setupRespondResolved(makeRecommendation({ decision: 'REJECTED' }))
    setupHook()

    return waitForLoadingFinished()
      .then(() => callReject())
      .then(() => {
        verifyRespondCalledWith(1, 'REJECTED')
        verifyHasUnrespondedRecommendation(false)
        verifyRecommendedTaskIdsSize(0)
      })
  })

  it('requestOnDemand actualiza el estado y devuelve true cuando hay recomendación', () => {
    setupGetTodayResolved(null)
    setupGenerateResolved(makeRecommendation())
    setupHook()

    return waitForLoadingFinished()
      .then(() => callRequestOnDemand())
      .then(produced => {
        expect(produced).toBe(true)
        verifyHasUnrespondedRecommendation(true)
      })
  })

  it('requestOnDemand devuelve false cuando no hay suficientes tareas pendientes', () => {
    setupGetTodayResolved(null)
    setupGenerateResolved(null)
    setupHook()

    return waitForLoadingFinished()
      .then(() => callRequestOnDemand())
      .then(produced => {
        expect(produced).toBe(false)
        verifyHasUnrespondedRecommendation(false)
      })
  })

  let rendered: ReturnType<typeof renderHook<ReturnType<typeof usePendingRecommendation>, undefined>>

  /* helpers */

  const setupHook = () => {
    rendered = renderHook(() => usePendingRecommendation())
  }

  const setupGetTodayResolved = (val: PendingRecommendationResponse | null) => {
    mockedGetToday.mockResolvedValueOnce(val)
  }

  const setupRespondResolved = (val: PendingRecommendationResponse) => {
    mockedRespond.mockResolvedValueOnce(val)
  }

  const setupGenerateResolved = (val: PendingRecommendationResponse | null) => {
    mockedGenerate.mockResolvedValueOnce(val)
  }

  const waitForLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.loading).toBe(false))
  }

  const callAccept = async () => {
    await act(async () => {
      await rendered.result.current.accept()
    })
  }

  const callReject = async () => {
    await act(async () => {
      await rendered.result.current.reject()
    })
  }

  const callRequestOnDemand = async (): Promise<boolean> => {
    let produced: boolean | undefined
    await act(async () => {
      produced = await rendered.result.current.requestOnDemand()
    })
    return produced as boolean
  }

  const verifyHasUnrespondedRecommendation = (expected: boolean) => {
    expect(rendered.result.current.hasUnrespondedRecommendation).toBe(expected)
  }

  const verifyRecommendedTaskIds = (expected: Set<number>) => {
    expect(rendered.result.current.recommendedTaskIds).toEqual(expected)
  }

  const verifyRecommendedTaskIdsSize = (expected: number) => {
    expect(rendered.result.current.recommendedTaskIds.size).toBe(expected)
  }

  const verifyRespondCalledWith = (id: number, decision: 'ACCEPTED' | 'REJECTED') => {
    expect(mockedRespond).toHaveBeenCalledWith(id, decision)
  }
})

function makeRecommendation(overrides: Partial<PendingRecommendationResponse> = {}): PendingRecommendationResponse {
  return {
    recommendationId: 1,
    date: '2026-01-01',
    decision: 'PENDING',
    recommendedTaskIds: [1, 2],
    isNew: true,
    ...overrides,
  }
}
