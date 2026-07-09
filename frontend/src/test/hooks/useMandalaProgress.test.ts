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
    getSessionStatus: vi.fn(),
  },
}))

const mockedGetProgress = vi.mocked(mandalasApi.getProgress)
const mockedSaveProgress = vi.mocked(mandalasApi.saveProgress)
const mockedClearProgress = vi.mocked(mandalasApi.clearProgress)
const mockedGetSessionStatus = vi.mocked(mandalasApi.getSessionStatus)

describe('useMandalaProgress', () => {
  beforeEach(() => {
    mockedGetProgress.mockReset()
    mockedSaveProgress.mockReset()
    mockedClearProgress.mockReset()
    mockedGetSessionStatus.mockReset()
  })

  it('carga progreso desde backend', () => {
    setupGetProgressResolved(getMockBlob())
    setupGetSessionStatusResolved({ sessionRegistered: true })
    setupHook('mandala-01')
    return waitForLoadingFinished().then(() => {
      verifyGetProgressCalledWith('mandala-01')
      verifyGetSessionStatusCalledWith('mandala-01')
      verifyPaintBlob(getMockBlob())
      verifySessionRegistered(true)
    })
  })

  it('interpreta 404 como progreso inexistente', () => {
    setupGetProgressRejected(new ApiError('No encontrado', {}, 404))
    setupGetSessionStatusResolved({ sessionRegistered: false })
    setupHook('mandala-01')
    return waitForLoadingFinished().then(() => {
      verifyPaintBlobIsNull()
      verifySessionRegistered(false)
    })
  })

  it('guarda y borra progreso en backend', () => {
    setupGetProgressRejected(new ApiError('No encontrado', {}, 404))
    setupGetSessionStatusResolved({ sessionRegistered: false })
    setupSaveProgressResolved()
    setupClearProgressResolved()
    setupHook('mandala-01')
    return waitForLoadingFinished()
      .then(() => callSaveAndClearProgress(getMockBlob()))
      .then(() => {
        verifySaveProgressCalledWith('mandala-01', getMockBlob())
        verifyClearProgressCalledWith('mandala-01')
        verifyPaintBlobIsNull()
        verifySessionRegistered(false)
      })
  })
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useMandalaProgress>, undefined>>

  /* helpers */

  const getMockBlob = () => {
    return new Blob(['paint'], { type: 'application/octet-stream' })
  }

  const setupHook = (id: string) => {
    rendered = renderHook(() => useMandalaProgress(id))
  }

  const setupGetProgressResolved = (val: Blob) => {
    mockedGetProgress.mockResolvedValue(val)
  }

  const setupGetProgressRejected = (err: Error) => {
    mockedGetProgress.mockRejectedValue(err)
  }

  const setupGetSessionStatusResolved = (val: { sessionRegistered: boolean }) => {
    mockedGetSessionStatus.mockResolvedValue(val)
  }

  const setupSaveProgressResolved = () => {
    mockedSaveProgress.mockResolvedValue(undefined)
  }

  const setupClearProgressResolved = () => {
    mockedClearProgress.mockResolvedValue(undefined)
  }

  const waitForLoadingFinished = () => {
    return waitFor(() => {
      expect(rendered.result.current.loading).toBe(false)
    })
  }

  const callSaveAndClearProgress = (blob: Blob) => {
    return act(() => {
      return rendered.result.current.saveProgress(blob).then(() => {
        return rendered.result.current.clearProgress()
      })
    })
  }

  const verifyGetProgressCalledWith = (id: string) => {
    expect(mockedGetProgress).toHaveBeenCalledWith(id)
  }

  const verifyGetSessionStatusCalledWith = (id: string) => {
    expect(mockedGetSessionStatus).toHaveBeenCalledWith(id)
  }

  const verifyPaintBlob = (expected: Blob) => {
    expect(rendered.result.current.paintBlob).toEqual(expected)
  }

  const verifyPaintBlobIsNull = () => {
    expect(rendered.result.current.paintBlob).toBeNull()
  }

  const verifySessionRegistered = (expected: boolean) => {
    expect(rendered.result.current.sessionRegistered).toBe(expected)
  }

  const verifySaveProgressCalledWith = (id: string, blob: Blob) => {
    expect(mockedSaveProgress).toHaveBeenCalledWith(id, blob)
  }

  const verifyClearProgressCalledWith = (id: string) => {
    expect(mockedClearProgress).toHaveBeenCalledWith(id)
  }
})
