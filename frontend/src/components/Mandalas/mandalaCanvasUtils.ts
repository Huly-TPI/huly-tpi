export interface CanvasPoint {
  x: number
  y: number
}

export interface MandalaAnalysis {
  boundaryMap: Uint8Array
  exteriorMap: Uint8Array
  width: number
  height: number
}

interface BoundaryOptions {
  alphaThreshold?: number
  darkThreshold?: number
}

const DEFAULT_ALPHA_THRESHOLD = 16
const DEFAULT_DARK_THRESHOLD = 220

export function getPointerCanvasPoint(
  event: Pick<PointerEvent, 'clientX' | 'clientY'>,
  canvas: HTMLCanvasElement,
): CanvasPoint | null {
  const rect = canvas.getBoundingClientRect()
  if (rect.width <= 0 || rect.height <= 0 || canvas.width <= 0 || canvas.height <= 0) {
    return null
  }

  return {
    x: Math.min(canvas.width - 1, Math.max(0, Math.floor(((event.clientX - rect.left) / rect.width) * canvas.width))),
    y: Math.min(canvas.height - 1, Math.max(0, Math.floor(((event.clientY - rect.top) / rect.height) * canvas.height))),
  }
}

export function isLinePixel(
  data: Uint8ClampedArray,
  offset: number,
  options: BoundaryOptions = {},
) {
  const alpha = data[offset + 3]
  if (alpha <= (options.alphaThreshold ?? DEFAULT_ALPHA_THRESHOLD)) return false

  const red = data[offset]
  const green = data[offset + 1]
  const blue = data[offset + 2]
  const luminance = red * 0.2126 + green * 0.7152 + blue * 0.0722
  const maxChannel = Math.max(red, green, blue)
  const minChannel = Math.min(red, green, blue)
  const chroma = maxChannel - minChannel

  if (luminance < (options.darkThreshold ?? DEFAULT_DARK_THRESHOLD)) return true

  return chroma > 72 && luminance < 235
}

export function buildBoundaryMap(
  imageData: ImageData,
  options?: BoundaryOptions,
) {
  const { data, width, height } = imageData
  const boundaryMap = new Uint8Array(width * height)

  for (let index = 0; index < boundaryMap.length; index += 1) {
    if (isLinePixel(data, index * 4, options)) {
      boundaryMap[index] = 1
    }
  }

  return boundaryMap
}

export function dilateMap(
  source: Uint8Array,
  width: number,
  height: number,
  radius = 1,
) {
  const result = new Uint8Array(source)

  for (let y = 0; y < height; y += 1) {
    for (let x = 0; x < width; x += 1) {
      const index = y * width + x
      if (source[index] !== 1) continue

      for (let dy = -radius; dy <= radius; dy += 1) {
        const nextY = y + dy
        if (nextY < 0 || nextY >= height) continue

        for (let dx = -radius; dx <= radius; dx += 1) {
          const nextX = x + dx
          if (nextX < 0 || nextX >= width) continue
          result[nextY * width + nextX] = 1
        }
      }
    }
  }

  return result
}

export function floodFillExterior(
  boundaryMap: Uint8Array,
  width: number,
  height: number,
) {
  const exteriorMap = new Uint8Array(width * height)
  const queue = new Int32Array(width * height)
  let head = 0
  let tail = 0

  const enqueue = (index: number) => {
    if (boundaryMap[index] || exteriorMap[index]) return
    exteriorMap[index] = 1
    queue[tail] = index
    tail += 1
  }

  for (let x = 0; x < width; x += 1) {
    enqueue(x)
    enqueue((height - 1) * width + x)
  }

  for (let y = 0; y < height; y += 1) {
    enqueue(y * width)
    enqueue(y * width + width - 1)
  }

  while (head < tail) {
    const index = queue[head]
    head += 1

    const x = index % width
    const y = Math.floor(index / width)

    if (x > 0) enqueue(index - 1)
    if (x < width - 1) enqueue(index + 1)
    if (y > 0) enqueue(index - width)
    if (y < height - 1) enqueue(index + width)
  }

  return exteriorMap
}

export function createRegionMask(
  point: CanvasPoint,
  analysis: MandalaAnalysis,
) {
  const { boundaryMap, exteriorMap, width, height } = analysis
  if (point.x < 0 || point.y < 0 || point.x >= width || point.y >= height) return null

  const startIndex = point.y * width + point.x
  if (boundaryMap[startIndex] || exteriorMap[startIndex]) return null

  const mask = new Uint8Array(width * height)
  const queue = new Int32Array(width * height)
  let head = 0
  let tail = 0

  mask[startIndex] = 1
  queue[tail] = startIndex
  tail += 1

  const enqueue = (index: number) => {
    if (boundaryMap[index] || exteriorMap[index] || mask[index]) return
    mask[index] = 1
    queue[tail] = index
    tail += 1
  }

  while (head < tail) {
    const index = queue[head]
    head += 1

    const x = index % width
    const y = Math.floor(index / width)

    if (x > 0) enqueue(index - 1)
    if (x < width - 1) enqueue(index + 1)
    if (y > 0) enqueue(index - width)
    if (y < height - 1) enqueue(index + width)
  }

  return mask
}

export function drawMaskToCanvas(
  mask: Uint8Array,
  canvas: HTMLCanvasElement,
  width: number,
  height: number,
) {
  canvas.width = width
  canvas.height = height

  const context = canvas.getContext('2d')
  if (!context) return

  const imageData = context.createImageData(width, height)
  for (let index = 0; index < mask.length; index += 1) {
    if (!mask[index]) continue
    imageData.data[index * 4 + 3] = 255
  }

  context.putImageData(imageData, 0, 0)
}

export function renderLineOverlay(
  sourceImageData: ImageData,
  canvas: HTMLCanvasElement,
) {
  const { width, height, data } = sourceImageData
  canvas.width = width
  canvas.height = height

  const context = canvas.getContext('2d')
  if (!context) return

  const lineImageData = context.createImageData(width, height)
  for (let index = 0; index < width * height; index += 1) {
    const sourceOffset = index * 4
    if (!isLinePixel(data, sourceOffset)) continue

    lineImageData.data[sourceOffset] = 20
    lineImageData.data[sourceOffset + 1] = 20
    lineImageData.data[sourceOffset + 2] = 24
    lineImageData.data[sourceOffset + 3] = Math.max(data[sourceOffset + 3], 210)
  }

  context.putImageData(lineImageData, 0, 0)
}

export function createMandalaAnalysis(
  imageData: ImageData,
  boundaryDilationRadius = 1,
): MandalaAnalysis {
  const rawBoundaryMap = buildBoundaryMap(imageData)
  const boundaryMap = dilateMap(rawBoundaryMap, imageData.width, imageData.height, boundaryDilationRadius)
  const exteriorMap = floodFillExterior(boundaryMap, imageData.width, imageData.height)

  return {
    boundaryMap,
    exteriorMap,
    width: imageData.width,
    height: imageData.height,
  }
}
