export interface Ripple {
  x: number
  y: number
  createdAt: number
  maxRadius: number
  life: number
  strength: number
  seed: number
}

export interface Stone {
  x: number
  y: number
  originX: number
  originY: number
  createdAt: number
  flightDuration: number
  arcHeight: number
  size: number
  startScale: number
  rotation: number
  spin: number
}

export interface LakePalette {
  ringLight: string
  ringShadow: string
  splash: string
  stoneTop: string
  stoneBottom: string
  dropShadow: string
}

export const LAKE_PALETTES: Record<'light' | 'dark', LakePalette> = {
  light: {
    ringLight: '255, 255, 255',
    ringShadow: '86, 132, 158',
    splash: '255, 255, 255',
    stoneTop: '#d8d2c6',
    stoneBottom: '#8f887b',
    dropShadow: '15, 51, 80',
  },
  dark: {
    ringLight: '186, 214, 232',
    ringShadow: '18, 38, 58',
    splash: '210, 232, 245',
    stoneTop: '#9aa0a8',
    stoneBottom: '#565c66',
    dropShadow: '3, 12, 22',
  },
}

const RING_COUNT = 3
const RING_SPACING = 26

export const isWaterColor = (r: number, g: number, b: number) =>
  b > 110 && b >= r && b >= g && b - r > 15

export const buildWaterMask = (
  image: HTMLImageElement,
): ((imageX: number, imageY: number) => boolean) | null => {
  const width = image.naturalWidth
  const height = image.naturalHeight
  if (!width || !height) return null

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const context = canvas.getContext('2d', { willReadFrequently: true })
  if (!context) return null

  let data: Uint8ClampedArray
  try {
    context.drawImage(image, 0, 0)
    data = context.getImageData(0, 0, width, height).data
  } catch {
    return null
  }

  return (imageX: number, imageY: number) => {
    const px = Math.floor(imageX)
    const py = Math.floor(imageY)
    if (px < 0 || py < 0 || px >= width || py >= height) return false
    const index = (py * width + px) * 4
    return isWaterColor(data[index], data[index + 1], data[index + 2])
  }
}

export const toImagePoint = (
  viewX: number,
  viewY: number,
  viewWidth: number,
  viewHeight: number,
  naturalWidth: number,
  naturalHeight: number,
): { x: number; y: number } => {
  const scale = Math.max(viewWidth / naturalWidth, viewHeight / naturalHeight)
  const drawnWidth = naturalWidth * scale
  const drawnHeight = naturalHeight * scale
  return {
    x: (viewX - (viewWidth - drawnWidth) / 2) / scale,
    y: (viewY - (viewHeight - drawnHeight) / 2) / scale,
  }
}

export const getLakePoint = (
  event: { clientX: number; clientY: number },
  canvas: HTMLCanvasElement,
): { x: number; y: number } => {
  const rect = canvas.getBoundingClientRect()
  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top,
  }
}

export const createRipple = (
  x: number,
  y: number,
  now: number,
  viewSize: number,
): Ripple => ({
  x,
  y,
  createdAt: now,
  maxRadius: Math.max(120, viewSize * 0.22),
  life: 4200,
  strength: 1,
  seed: Math.random() * Math.PI * 2,
})

export const createAmbientRipple = (
  x: number,
  y: number,
  now: number,
  viewSize: number,
): Ripple => ({
  x,
  y,
  createdAt: now,
  maxRadius: Math.max(38, viewSize * 0.055),
  life: 3400,
  strength: 0.32,
  seed: Math.random() * Math.PI * 2,
})

export const rippleProgress = (ripple: Ripple, now: number) =>
  (now - ripple.createdAt) / ripple.life

const easeOut = (t: number) => 1 - (1 - t) * (1 - t)


export const createStone = (
  x: number,
  y: number,
  now: number,
  viewWidth: number,
  viewHeight: number,
): Stone => ({
  x,
  y,
  originX: viewWidth / 2,
  originY: viewHeight * 1.08,
  createdAt: now,
  flightDuration: 520,
  arcHeight: Math.max(60, (viewHeight - y) * 0.22),
  size: Math.max(9, Math.min(viewWidth, viewHeight) * 0.017),
  startScale: 3.2,
  rotation: Math.random() * Math.PI,
  spin: (Math.random() * 2 - 1) * Math.PI * 1.5,
})

export const stoneProgress = (stone: Stone, now: number) =>
  (now - stone.createdAt) / stone.flightDuration

const lerp = (from: number, to: number, t: number) => from + (to - from) * t

export const drawStone = (
  context: CanvasRenderingContext2D,
  stone: Stone,
  progress: number,
  palette: LakePalette,
) => {
  const t = Math.min(Math.max(progress, 0), 1)
  const eased = 1 - (1 - t) * (1 - t) // desacelera al alejarse

  const posX = lerp(stone.originX, stone.x, eased)
  const posY = lerp(stone.originY, stone.y, eased) - stone.arcHeight * Math.sin(Math.PI * eased)
  const size = stone.size * lerp(stone.startScale, 1, eased)

  const shadowT = Math.max(0, (eased - 0.55) / 0.45)
  if (shadowT > 0) {
    const shadowScale = 0.3 + shadowT * 0.85
    context.beginPath()
    context.ellipse(
      stone.x,
      stone.y,
      stone.size * 0.95 * shadowScale,
      stone.size * 0.5 * shadowScale,
      0,
      0,
      Math.PI * 2,
    )
    context.fillStyle = `rgba(${palette.dropShadow}, ${shadowT * 0.35})`
    context.fill()
  }

  context.save()
  context.translate(posX, posY)
  context.rotate(stone.rotation + stone.spin * t)
  const gradient = context.createRadialGradient(
    -size * 0.32,
    -size * 0.32,
    size * 0.15,
    0,
    0,
    size,
  )
  gradient.addColorStop(0, palette.stoneTop)
  gradient.addColorStop(1, palette.stoneBottom)
  context.fillStyle = gradient
  context.beginPath()
  context.ellipse(0, 0, size, size * 0.82, 0, 0, Math.PI * 2)
  context.fill()
  context.restore()
}

const WOBBLE_STEPS = 44
const WOBBLE_LOBES = 7


const strokeWobblyRing = (
  context: CanvasRenderingContext2D,
  centerX: number,
  centerY: number,
  radius: number,
  amplitude: number,
  phase: number,
) => {
  context.beginPath()
  for (let step = 0; step <= WOBBLE_STEPS; step += 1) {
    const angle = (step / WOBBLE_STEPS) * Math.PI * 2
    const wobble = 1 + amplitude * Math.sin(WOBBLE_LOBES * angle + phase)
    const r = radius * wobble
    const x = centerX + Math.cos(angle) * r
    const y = centerY + Math.sin(angle) * r
    if (step === 0) context.moveTo(x, y)
    else context.lineTo(x, y)
  }
  context.stroke()
}

export const drawRipples = (
  context: CanvasRenderingContext2D,
  ripples: Ripple[],
  palette: LakePalette,
  now: number,
): Ripple[] => {
  const alive: Ripple[] = []

  for (const ripple of ripples) {
    const progress = rippleProgress(ripple, now)
    if (progress >= 1) continue
    alive.push(ripple)

    const fade = 1 - progress
    const baseRadius = easeOut(progress) * ripple.maxRadius
    const phase = now * 0.004 + ripple.seed

    for (let ring = 0; ring < RING_COUNT; ring += 1) {
      const radius = baseRadius - ring * RING_SPACING
      if (radius <= 0) continue

      const ringFade = fade * (1 - ring * 0.28) * ripple.strength
      if (ringFade <= 0.01) continue

      const amplitude = Math.min(0.045, 5 / radius)

      context.lineWidth = 2.4 - ring * 0.5
      context.strokeStyle = `rgba(${palette.ringLight}, ${ringFade * 0.5})`
      strokeWobblyRing(context, ripple.x, ripple.y, radius, amplitude, phase + ring)

      context.lineWidth = 1.4
      context.strokeStyle = `rgba(${palette.ringShadow}, ${ringFade * 0.32})`
      strokeWobblyRing(context, ripple.x, ripple.y, radius + 2.5, amplitude, phase + ring + 0.5)
    }

    if (progress < 0.16) {
      const splashFade = 1 - progress / 0.16
      context.beginPath()
      context.arc(ripple.x, ripple.y, 4 + progress * 40, 0, Math.PI * 2)
      context.fillStyle = `rgba(${palette.splash}, ${splashFade * 0.4 * ripple.strength})`
      context.fill()
    }
  }

  return alive
}
