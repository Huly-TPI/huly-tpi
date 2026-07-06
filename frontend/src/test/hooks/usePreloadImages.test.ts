import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { usePreloadImages } from '../../hooks/usePreloadImages'
import { preloadImages } from '../../utils/preloadImages'
import { criticalImageUrls } from '../../utils/criticalImageUrls'
import { restImageUrls } from '../../utils/localImageUrls'

vi.mock('../../utils/preloadImages', () => ({
  preloadImages: vi.fn().mockResolvedValue({ loaded: [], failed: [] }),
}))
vi.mock('../../utils/criticalImageUrls', () => ({
  criticalImageUrls: ['critical-1.webp', 'critical-2.webp'],
}))
vi.mock('../../utils/localImageUrls', () => ({
  localImageUrls: ['critical-1.webp', 'critical-2.webp', 'rest-1.webp'],
  restImageUrls: ['rest-1.webp'],
}))

const mockedPreloadImages = vi.mocked(preloadImages)

describe('usePreloadImages', () => {
  beforeEach(() => {
    clearAllMocks()
    vi.stubGlobal('requestIdleCallback', (cb: () => void) => {
      cb()
      return 1
    })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('precarga primero las imágenes críticas y después el resto', () => {
    setupHook()
    return waitForPreloadImagesCalledTimes(2).then(() => {
      verifyPreloadImagesCalledWithAtIndex(1, criticalImageUrls)
      verifyPreloadImagesCalledWithAtIndex(2, restImageUrls)
    })
  })

  it('usa el fallback con setTimeout si no existe requestIdleCallback', () => {
    setupFakeTimers()
    setupRequestIdleCallbackUndefined()
    setupHook()
    verifyPreloadImagesNotCalled()
    return runFakeTimers().then(() => {
      verifyPreloadImagesCalledWith(criticalImageUrls)
      restoreRealTimers()
    })
  })

  /* helpers */

  const setupHook = () => {
    renderHook(() => usePreloadImages())
  }

  const setupFakeTimers = () => {
    vi.useFakeTimers()
  }

  const restoreRealTimers = () => {
    vi.useRealTimers()
  }

  const setupRequestIdleCallbackUndefined = () => {
    vi.stubGlobal('requestIdleCallback', undefined)
  }

  const runFakeTimers = () => {
    return vi.runAllTimersAsync()
  }

  const waitForPreloadImagesCalledTimes = (times: number) => {
    return waitFor(() => {
      expect(mockedPreloadImages).toHaveBeenCalledTimes(times)
    })
  }

  const verifyPreloadImagesCalledWithAtIndex = (index: number, urls: string[]) => {
    expect(mockedPreloadImages).toHaveBeenNthCalledWith(index, urls, expect.any(Object))
  }

  const verifyPreloadImagesNotCalled = () => {
    expect(mockedPreloadImages).not.toHaveBeenCalled()
  }

  const verifyPreloadImagesCalledWith = (urls: string[]) => {
    expect(mockedPreloadImages).toHaveBeenCalledWith(urls, expect.any(Object))
  }
})