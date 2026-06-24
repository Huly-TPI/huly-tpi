import { act, renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/apiError'
import { mandalasApi } from '../../api/mandalas'
import { useMandalaProgress } from '../../hooks/useMandalaProgress'

vi.mock('../../api/mandalas', () => ({
  mandalasApi: {
    getProgress: vi.fn(),
    saveProgress: vi.fn(),
    clearProgress: vi.fn(),
  },
}))

describe('useMandalaProgress', () => {
  beforeEach(() => {
    vi.mocked(mandalasApi.getProgress).mockReset()
    vi.mocked(mandalasApi.saveProgress).mockReset()
    vi.mocked(mandalasApi.clearProgress).mockReset()
  })

  it('carga progreso desde backend', async () => {
    const blob = new Blob(['paint'], { type: 'application/octet-stream' })
    vi.mocked(mandalasApi.getProgress).mockResolvedValue(blob)

    const { result } = renderHook(() => useMandalaProgress('mandala-01'))

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(mandalasApi.getProgress).toHaveBeenCalledWith('mandala-01')
    expect(result.current.paintBlob).toBe(blob)
  })

  it('interpreta 404 como progreso inexistente', async () => {
    vi.mocked(mandalasApi.getProgress).mockRejectedValue(new ApiError('No encontrado', {}, 404))

    const { result } = renderHook(() => useMandalaProgress('mandala-01'))

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.paintBlob).toBeNull()
  })

  it('guarda y borra progreso en backend', async () => {
    const blob = new Blob(['paint'], { type: 'application/octet-stream' })
    vi.mocked(mandalasApi.getProgress).mockRejectedValue(new ApiError('No encontrado', {}, 404))
    vi.mocked(mandalasApi.saveProgress).mockResolvedValue(undefined)
    vi.mocked(mandalasApi.clearProgress).mockResolvedValue(undefined)

    const { result } = renderHook(() => useMandalaProgress('mandala-01'))
    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.saveProgress(blob)
      await result.current.clearProgress()
    })

    expect(mandalasApi.saveProgress).toHaveBeenCalledWith('mandala-01', blob)
    expect(mandalasApi.clearProgress).toHaveBeenCalledWith('mandala-01')
    expect(result.current.paintBlob).toBeNull()
  })
})
