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

describe('usePreloadImages', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.stubGlobal('requestIdleCallback', (cb: () => void) => {
      cb()
      return 1
    })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('precarga primero las imágenes críticas y después el resto', async () => {
    renderHook(() => usePreloadImages())

    await waitFor(() => expect(preloadImages).toHaveBeenCalledTimes(2))
    expect(preloadImages).toHaveBeenNthCalledWith(1, criticalImageUrls, expect.any(Object))
    expect(preloadImages).toHaveBeenNthCalledWith(2, restImageUrls, expect.any(Object))
  })

  it('usa el fallback con setTimeout si no existe requestIdleCallback', async () => {
    vi.useFakeTimers()
    vi.stubGlobal('requestIdleCallback', undefined)

    renderHook(() => usePreloadImages())
    expect(preloadImages).not.toHaveBeenCalled()

    await vi.runAllTimersAsync()
    expect(preloadImages).toHaveBeenCalledWith(criticalImageUrls, expect.any(Object))

    vi.useRealTimers()
  })
})