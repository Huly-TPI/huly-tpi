import { describe, it, expect, vi } from 'vitest'
import { preloadImages } from '../../hooks/preloadImages'

describe('preloadImages', () => {
  it('carga todas las urls y las devuelve en loaded', () => {
    setupLoaderMock()
    return callPreloadImages(['a.webp', 'b.webp', 'c.webp']).then((result) => {
      verifyLoaderCalledTimes(3)
      verifyResultLoadedContains(result, ['a.webp', 'b.webp', 'c.webp'])
      verifyResultFailedIsEmpty(result)
    })
  })

  it('nunca supera el límite de concurrencia', () => {
    setupConcurrencyLoaderMock()
    const promise = callPreloadImagesWithConcurrency(10, 3)
    verifyMaxInFlight(3)
    return resolveAllPromises(promise).then(() => {
      verifyMaxInFlight(3)
      verifyLoaderCalledTimes(10)
    })
  })

  it('reintenta las que fallan y las cuenta como cargadas si el reintento tiene éxito', () => {
    setupFlakyLoaderMock()
    return callPreloadImagesWithRetries(['flaky.webp'], 1).then((result) => {
      verifyLoaderCalledTimes(2)
      verifyResultLoadedContains(result, ['flaky.webp'])
      verifyResultFailedIsEmpty(result)
    })
  })

  it('manda a failed las que agotan los reintentos', () => {
    setupAlwaysFailingLoaderMock()
    return callPreloadImagesWithRetries(['broken.webp'], 2).then((result) => {
      verifyLoaderCalledTimes(3)
      verifyResultLoadedIsEmpty(result)
      verifyResultFailedContains(result, ['broken.webp'])
    })
  })

  it('reporta progreso a medida que cargan', () => {
    setupLoaderMock()
    setupProgressMock()
    return callPreloadImagesWithProgress(['a.webp', 'b.webp']).then(() => {
      verifyProgressReportedWith(2, 2)
    })
  })

  /* helpers */

  let sharedLoader = vi.fn()
  let sharedOnProgress = vi.fn()
  let inFlight = 0
  let maxInFlight = 0
  let resolvers: Array<() => void> = []
  let done = false

  const setupLoaderMock = () => {
    sharedLoader = vi.fn().mockResolvedValue(undefined)
  }

  const setupProgressMock = () => {
    sharedOnProgress = vi.fn()
  }

  const setupConcurrencyLoaderMock = () => {
    inFlight = 0
    maxInFlight = 0
    resolvers = []
    done = false
    sharedLoader = vi.fn().mockImplementation(() => {
      inFlight++
      maxInFlight = Math.max(maxInFlight, inFlight)
      return new Promise<void>((resolve) => {
        resolvers.push(() => {
          inFlight--
          resolve()
        })
      })
    })
  }

  const setupFlakyLoaderMock = () => {
    sharedLoader = vi
      .fn()
      .mockRejectedValueOnce(new Error('fail'))
      .mockResolvedValue(undefined)
  }

  const setupAlwaysFailingLoaderMock = () => {
    sharedLoader = vi.fn().mockRejectedValue(new Error('fail'))
  }

  const callPreloadImages = (urls: string[]) => {
    return preloadImages(urls, { loader: sharedLoader })
  }

  const callPreloadImagesWithConcurrency = (count: number, concurrency: number) => {
    const urls = Array.from({ length: count }, (_, i) => `img-${i}.webp`)
    return preloadImages(urls, { loader: sharedLoader, concurrency }).then((r) => {
      done = true
      return r
    })
  }

  const callPreloadImagesWithRetries = (urls: string[], retries: number) => {
    return preloadImages(urls, { loader: sharedLoader, retries })
  }

  const callPreloadImagesWithProgress = (urls: string[]) => {
    return preloadImages(urls, { loader: sharedLoader, onProgress: sharedOnProgress })
  }

  const resolveAllPromises = async (promise: Promise<any>) => {
    while (!done) {
      if (resolvers.length > 0) {
        resolvers.shift()!()
      }
      await Promise.resolve()
    }
    return promise
  }

  const verifyLoaderCalledTimes = (times: number) => {
    expect(sharedLoader).toHaveBeenCalledTimes(times)
  }

  const verifyResultLoadedContains = (result: any, urls: string[]) => {
    expect(result.loaded).toEqual(expect.arrayContaining(urls))
  }

  const verifyResultLoadedIsEmpty = (result: any) => {
    expect(result.loaded).toEqual([])
  }

  const verifyResultFailedIsEmpty = (result: any) => {
    expect(result.failed).toEqual([])
  }

  const verifyResultFailedContains = (result: any, urls: string[]) => {
    expect(result.failed).toEqual(expect.arrayContaining(urls))
  }

  const verifyMaxInFlight = (val: number) => {
    expect(maxInFlight).toBe(val)
  }

  const verifyProgressReportedWith = (current: number, total: number) => {
    expect(sharedOnProgress).toHaveBeenLastCalledWith(current, total)
  }
})
