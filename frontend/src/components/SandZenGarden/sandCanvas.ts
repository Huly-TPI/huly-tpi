export interface SandPoint {
  x: number
  y: number
  intensity: number
}

export type SandStroke = SandPoint[]

interface CanvasSize {
  width: number
  height: number
}

const clamp = (value: number, min: number, max: number) =>
  Math.min(max, Math.max(min, value))

const createSeededRandom = (seed: number) => {
  let state = seed >>> 0

  return () => {
    state += 0x6d2b79f5
    let value = state
    value = Math.imul(value ^ (value >>> 15), value | 1)
    value ^= value + Math.imul(value ^ (value >>> 7), value | 61)
    return ((value ^ (value >>> 14)) >>> 0) / 4294967296
  }
}

export const getCanvasPoint = (
  event: Pick<PointerEvent, 'clientX' | 'clientY'>,
  canvas: HTMLCanvasElement,
): SandPoint => {
  const rect = canvas.getBoundingClientRect()

  return {
    x: clamp((event.clientX - rect.left) / rect.width, 0, 1),
    y: clamp((event.clientY - rect.top) / rect.height, 0, 1),
    intensity: 1,
  }
}

export const prepareCanvas = (
  canvas: HTMLCanvasElement,
  context: CanvasRenderingContext2D,
): CanvasSize => {
  const rect = canvas.getBoundingClientRect()
  const width = Math.max(1, rect.width)
  const height = Math.max(1, rect.height)
  const pixelRatio = Math.min(window.devicePixelRatio || 1, 2)
  const physicalWidth = Math.round(width * pixelRatio)
  const physicalHeight = Math.round(height * pixelRatio)

  if (canvas.width !== physicalWidth || canvas.height !== physicalHeight) {
    canvas.width = physicalWidth
    canvas.height = physicalHeight
  }

  context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0)
  context.imageSmoothingEnabled = true

  return { width, height }
}

export const drawSandTexture = (
  context: CanvasRenderingContext2D,
  { width, height }: CanvasSize,
  seed: number,
) => {
  const gradient = context.createLinearGradient(0, 0, width, height)
  gradient.addColorStop(0, '#ead39d')
  gradient.addColorStop(0.48, '#d9b978')
  gradient.addColorStop(1, '#cfa962')

  context.fillStyle = gradient
  context.fillRect(0, 0, width, height)

  const random = createSeededRandom(seed)
  const grainCount = Math.min(9000, Math.round((width * height) / 115))

  for (let index = 0; index < grainCount; index += 1) {
    const x = random() * width
    const y = random() * height
    const radius = 0.25 + random() * 0.85
    const isLight = random() > 0.52
    const alpha = 0.06 + random() * 0.14

    context.beginPath()
    context.fillStyle = isLight
      ? `rgba(255, 244, 207, ${alpha})`
      : `rgba(111, 76, 37, ${alpha})`
    context.arc(x, y, radius, 0, Math.PI * 2)
    context.fill()
  }

  const vignette = context.createRadialGradient(
    width / 2,
    height / 2,
    Math.min(width, height) * 0.15,
    width / 2,
    height / 2,
    Math.max(width, height) * 0.72,
  )
  vignette.addColorStop(0, 'rgba(255, 248, 222, 0.08)')
  vignette.addColorStop(1, 'rgba(103, 67, 29, 0.16)')
  context.fillStyle = vignette
  context.fillRect(0, 0, width, height)
}

const strokeLine = (
  context: CanvasRenderingContext2D,
  startX: number,
  startY: number,
  endX: number,
  endY: number,
  width: number,
  color: string,
) => {
  context.beginPath()
  context.moveTo(startX, startY)
  context.lineTo(endX, endY)
  context.lineWidth = width
  context.strokeStyle = color
  context.lineCap = 'round'
  context.lineJoin = 'round'
  context.stroke()
}

export const drawGrooveSegment = (
  context: CanvasRenderingContext2D,
  start: SandPoint,
  end: SandPoint,
  { width, height }: CanvasSize,
) => {
  const startX = start.x * width
  const startY = start.y * height
  const endX = end.x * width
  const endY = end.y * height
  const deltaX = endX - startX
  const deltaY = endY - startY
  const distance = Math.hypot(deltaX, deltaY)

  if (distance < 0.1) return

  const normalX = -deltaY / distance
  const normalY = deltaX / distance
  const baseWidth =
    clamp(Math.min(width, height) * 0.022, 11, 19) *
    ((start.intensity + end.intensity) / 2)
  const ridgeOffset = baseWidth * 0.5

  strokeLine(
    context,
    startX,
    startY,
    endX,
    endY,
    baseWidth * 1.35,
    'rgba(98, 62, 27, 0.3)',
  )
  strokeLine(
    context,
    startX,
    startY,
    endX,
    endY,
    baseWidth * 0.78,
    'rgba(151, 101, 49, 0.5)',
  )
  strokeLine(
    context,
    startX - normalX * ridgeOffset,
    startY - normalY * ridgeOffset,
    endX - normalX * ridgeOffset,
    endY - normalY * ridgeOffset,
    baseWidth * 0.3,
    'rgba(91, 57, 25, 0.38)',
  )
  strokeLine(
    context,
    startX + normalX * ridgeOffset,
    startY + normalY * ridgeOffset,
    endX + normalX * ridgeOffset,
    endY + normalY * ridgeOffset,
    baseWidth * 0.34,
    'rgba(255, 239, 190, 0.68)',
  )
  strokeLine(
    context,
    startX,
    startY,
    endX,
    endY,
    baseWidth * 0.14,
    'rgba(104, 66, 29, 0.36)',
  )
}

const drawGrooveStart = (
  context: CanvasRenderingContext2D,
  point: SandPoint,
  size: CanvasSize,
) => {
  const radius =
    clamp(Math.min(size.width, size.height) * 0.011, 5.5, 9.5) *
    point.intensity
  const x = point.x * size.width
  const y = point.y * size.height
  const gradient = context.createRadialGradient(
    x - radius * 0.25,
    y - radius * 0.25,
    radius * 0.15,
    x,
    y,
    radius,
  )

  gradient.addColorStop(0, 'rgba(103, 65, 29, 0.52)')
  gradient.addColorStop(0.65, 'rgba(145, 96, 45, 0.42)')
  gradient.addColorStop(1, 'rgba(255, 239, 190, 0.55)')
  context.beginPath()
  context.fillStyle = gradient
  context.arc(x, y, radius, 0, Math.PI * 2)
  context.fill()
}

export const renderSandGarden = (
  canvas: HTMLCanvasElement,
  strokes: SandStroke[],
  seed: number,
) => {
  const context = canvas.getContext('2d')
  if (!context) return

  const size = prepareCanvas(canvas, context)
  context.clearRect(0, 0, size.width, size.height)
  drawSandTexture(context, size, seed)

  for (const stroke of strokes) {
    if (stroke.length === 0) continue
    drawGrooveStart(context, stroke[0], size)

    for (let index = 1; index < stroke.length; index += 1) {
      drawGrooveSegment(context, stroke[index - 1], stroke[index], size)
    }
  }
}

export const drawLatestGroove = (
  canvas: HTMLCanvasElement,
  start: SandPoint,
  end: SandPoint,
) => {
  const context = canvas.getContext('2d')
  if (!context) return

  const rect = canvas.getBoundingClientRect()
  drawGrooveSegment(context, start, end, {
    width: rect.width,
    height: rect.height,
  })
}
