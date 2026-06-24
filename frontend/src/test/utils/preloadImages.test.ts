import { describe, it, expect, vi } from 'vitest'
import { preloadImages } from '../../utils/preloadImages'

describe('preloadImages', () => {
  it('carga todas las urls y las devuelve en loaded', async () => {
    const loader = vi.fn().mockResolvedValue(undefined)
    const urls = ['a.webp', 'b.webp', 'c.webp']

    const result = await preloadImages(urls, { loader })

    expect(loader).toHaveBeenCalledTimes(3)
    expect(result.loaded).toEqual(expect.arrayContaining(urls))
    expect(result.failed).toEqual([])
  })

  it('nunca supera el límite de concurrencia', async () => {
    let inFlight = 0
    let maxInFlight = 0
    const resolvers: Array<() => void> = []
    const loader = vi.fn().mockImplementation(() => {
      inFlight++
      maxInFlight = Math.max(maxInFlight, inFlight)
      return new Promise<void>((resolve) => {
        resolvers.push(() => {
          inFlight--
          resolve()
        })
      })
    })

    const urls = Array.from({ length: 10 }, (_, i) => `img-${i}.webp`)
     let done = false
    const promise = preloadImages(urls, { loader, concurrency: 3 }).then((r) => {
      done = true
      return r
    })

    expect(maxInFlight).toBe(3)

    while (!done) {
      if (resolvers.length > 0) {
        resolvers.shift()!()
      }
      await Promise.resolve()
    }
    await promise

    expect(maxInFlight).toBe(3)
    expect(loader).toHaveBeenCalledTimes(10)
  })

  it('reintenta las que fallan y las cuenta como cargadas si el reintento tiene éxito', async () => {
    const loader = vi
      .fn()
      .mockRejectedValueOnce(new Error('fail'))
      .mockResolvedValue(undefined)

    const result = await preloadImages(['flaky.webp'], { loader, retries: 1 })

    expect(loader).toHaveBeenCalledTimes(2) 
    expect(result.loaded).toEqual(['flaky.webp'])
    expect(result.failed).toEqual([])
  })

  it('manda a failed las que agotan los reintentos', async () => {
    const loader = vi.fn().mockRejectedValue(new Error('fail'))

    const result = await preloadImages(['broken.webp'], { loader, retries: 2 })

    expect(loader).toHaveBeenCalledTimes(3)
    expect(result.loaded).toEqual([])
    expect(result.failed).toEqual(['broken.webp'])
  })

  it('reporta progreso a medida que cargan', async () => {
    const loader = vi.fn().mockResolvedValue(undefined)
    const onProgress = vi.fn()

    await preloadImages(['a.webp', 'b.webp'], { loader, onProgress })

    expect(onProgress).toHaveBeenLastCalledWith(2, 2)
  })
})