export interface PreloadOptions { 
    concurrency?: number
    retries?: number
    loader?: (url: string) => Promise<void>
    onProgress?: (loaded: number, total: number) => void
}

export interface PreloadResult {
    loaded: string[]
    failed: string[]
}

export function loadImage(url: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve()
    img.onerror = () => reject(new Error(`No se pudo cargar la imagen: ${url}`))
    img.src = url
  })
}

async function loadWithRetries(
    url: string, 
    loader: (url: string) => Promise<void>, 
    retries: number,
    ): Promise<boolean> {
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      await loader(url)
      return true
    } catch {
      if (attempt === retries) return false
    }
  }
  return false
}

export async function preloadImages(urls: string[], options: PreloadOptions = {}): Promise<PreloadResult> {
     const { concurrency = 6, retries = 2, loader = loadImage, onProgress } = options
     const loaded: string[] = []
     const failed: string[] = []
     const total = urls.length
     let cursor = 0
     let completed = 0

      async function worker(): Promise<void> {
    while (cursor < urls.length) {
      const url = urls[cursor++]
      const ok = await loadWithRetries(url, loader, retries)
      if (ok) {
        loaded.push(url)
      } else {
        failed.push(url)
      }
      completed++
      onProgress?.(completed, total)
    }
  }

  const workerCount = Math.min(concurrency, urls.length)
  await Promise.all(Array.from({ length: workerCount }, () => worker()))

  return { loaded, failed }
}