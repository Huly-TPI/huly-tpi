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

const makeRecommendation = (overrides: Partial<PendingRecommendationResponse> = {}): PendingRecommendationResponse => ({
  recommendationId: 1,
  date: '2026-01-01',
  decision: 'PENDING',
  recommendedTaskIds: [1, 2],
  isNew: true,
  ...overrides,
})

describe('usePendingRecommendation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('no muestra banner cuando no hay recomendación', async () => {
    mockedGetToday.mockResolvedValueOnce(null)

    const { result } = renderHook(() => usePendingRecommendation())

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.hasUnrespondedRecommendation).toBe(false)
  })

  it('muestra banner cuando hay una recomendación pendiente de respuesta', async () => {
    mockedGetToday.mockResolvedValueOnce(makeRecommendation())

    const { result } = renderHook(() => usePendingRecommendation())

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.hasUnrespondedRecommendation).toBe(true)
  })

  it('accept actualiza el estado y expone los ids recomendados', async () => {
    mockedGetToday.mockResolvedValueOnce(makeRecommendation())
    mockedRespond.mockResolvedValueOnce(makeRecommendation({ decision: 'ACCEPTED' }))

    const { result } = renderHook(() => usePendingRecommendation())
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.accept()
    })

    expect(mockedRespond).toHaveBeenCalledWith(1, 'ACCEPTED')
    expect(result.current.hasUnrespondedRecommendation).toBe(false)
    expect(result.current.recommendedTaskIds).toEqual(new Set([1, 2]))
  })

  it('reject actualiza el estado y no expone ids recomendados', async () => {
    mockedGetToday.mockResolvedValueOnce(makeRecommendation())
    mockedRespond.mockResolvedValueOnce(makeRecommendation({ decision: 'REJECTED' }))

    const { result } = renderHook(() => usePendingRecommendation())
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.reject()
    })

    expect(mockedRespond).toHaveBeenCalledWith(1, 'REJECTED')
    expect(result.current.hasUnrespondedRecommendation).toBe(false)
    expect(result.current.recommendedTaskIds.size).toBe(0)
  })

  it('requestOnDemand actualiza el estado y devuelve true cuando hay recomendación', async () => {
    mockedGetToday.mockResolvedValueOnce(null)
    mockedGenerate.mockResolvedValueOnce(makeRecommendation())

    const { result } = renderHook(() => usePendingRecommendation())
    await waitFor(() => expect(result.current.loading).toBe(false))

    let produced: boolean | undefined
    await act(async () => {
      produced = await result.current.requestOnDemand()
    })

    expect(produced).toBe(true)
    expect(result.current.hasUnrespondedRecommendation).toBe(true)
  })

  it('requestOnDemand devuelve false cuando no hay suficientes tareas pendientes', async () => {
    mockedGetToday.mockResolvedValueOnce(null)
    mockedGenerate.mockResolvedValueOnce(null)

    const { result } = renderHook(() => usePendingRecommendation())
    await waitFor(() => expect(result.current.loading).toBe(false))

    let produced: boolean | undefined
    await act(async () => {
      produced = await result.current.requestOnDemand()
    })

    expect(produced).toBe(false)
    expect(result.current.hasUnrespondedRecommendation).toBe(false)
  })
})
