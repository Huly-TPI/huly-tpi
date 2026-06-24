import { useEffect } from 'react'
import { preloadImages } from '../utils/preloadImages'
import { criticalImageUrls } from '../utils/criticalImageUrls'
import { restImageUrls } from '../utils/localImageUrls'

export function usePreloadImages(): void {
  useEffect(() => {
     const start = () => {
      void (async () => {
        await preloadImages(criticalImageUrls, { concurrency: 8, retries: 2 })
        await preloadImages(restImageUrls, { concurrency: 4, retries: 2 })
      })()
    }

    const idle = (
      window as typeof window & {
        requestIdleCallback?: (cb: () => void) => number
      }
    ).requestIdleCallback

    if (typeof idle === 'function') {
      idle(start)
    } else {
      const id = setTimeout(start, 200)
      return () => clearTimeout(id)
    }
  }, [])
}