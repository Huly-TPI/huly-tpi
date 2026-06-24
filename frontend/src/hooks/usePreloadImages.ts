import { useEffect } from 'react'
import { preloadImages } from '../utils/preloadImages'
import { localImageUrls } from '../utils/localImageUrls'

export function usePreloadImages(): void {
  useEffect(() => {
    const start = () => {
      void preloadImages(localImageUrls, { concurrency: 6, retries: 2 })
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