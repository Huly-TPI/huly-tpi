import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook } from '@testing-library/react'
import { usePreloadImages } from '../../hooks/usePreloadImages'
import { preloadImages } from '../../utils/preloadImages'
import { localImageUrls } from '../../utils/localImageUrls'

vi.mock('../../utils/preloadImages', () => ({
  preloadImages: vi.fn().mockResolvedValue({ loaded: [], failed: [] }),
}))
vi.mock('../../utils/localImageUrls', () => ({
  localImageUrls: ['a.webp', 'b.webp', 'c.webp'],
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

  it('dispara la precarga de todas las imágenes locales al montar', () => {
    renderHook(() => usePreloadImages())

    expect(preloadImages).toHaveBeenCalledTimes(1)
    expect(preloadImages).toHaveBeenCalledWith(localImageUrls, expect.any(Object))
  })

  it('usa el fallback con setTimeout si no existe requestIdleCallback', () => {
    vi.useFakeTimers()
    vi.stubGlobal('requestIdleCallback', undefined)

    renderHook(() => usePreloadImages())
    expect(preloadImages).not.toHaveBeenCalled()

    vi.runAllTimers()
    expect(preloadImages).toHaveBeenCalledTimes(1)

    vi.useRealTimers()
  })
})