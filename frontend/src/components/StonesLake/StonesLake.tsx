import {
  useCallback,
  useEffect,
  useRef,
  type PointerEvent as ReactPointerEvent,
} from 'react'
import { useTheme } from '../../context/theme'
import { useWaterAudio } from '../../hooks/useWaterAudio'
import {
  buildWaterMask,
  createAmbientRipple,
  createRipple,
  createStone,
  drawRipples,
  drawStone,
  getLakePoint,
  LAKE_PALETTES,
  stoneProgress,
  toImagePoint,
  type Ripple,
  type Stone,
} from './lakeCanvas'
import lakeBackground from '../../assets/stones-lake/light-theme/lake-background.webp'
import './StonesLake.css'

const MAX_RIPPLES = 80
const MAX_STONES = 30
const AMBIENT_INTERVAL = 650

interface StonesLakeProps {
  onStoneThrown?: () => void
}

export default function StonesLake({ onStoneThrown }: StonesLakeProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const backgroundRef = useRef<HTMLImageElement>(null)
  const waterMaskRef = useRef<((imageX: number, imageY: number) => boolean) | null>(null)
  const ripplesRef = useRef<Ripple[]>([])
  const stonesRef = useRef<Stone[]>([])
  const lastAmbientRef = useRef(0)
  const frameRef = useRef<number | null>(null)
  const { theme } = useTheme()
  const themeRef = useRef(theme)
  const { playDrop } = useWaterAudio()
  const playDropRef = useRef(playDrop)

  useEffect(() => {
    playDropRef.current = playDrop
  }, [playDrop])

  const handleBackgroundLoad = () => {
    const image = backgroundRef.current
    if (image) {
      waterMaskRef.current = buildWaterMask(image)
    }
  }

  const isWaterAtView = useCallback(
    (viewX: number, viewY: number, viewWidth: number, viewHeight: number) => {
      const mask = waterMaskRef.current
      const background = backgroundRef.current
      if (!mask || !background) return true

      const imagePoint = toImagePoint(
        viewX,
        viewY,
        viewWidth,
        viewHeight,
        background.naturalWidth,
        background.naturalHeight,
      )
      return mask(imagePoint.x, imagePoint.y)
    },
    [],
  )

  useEffect(() => {
    themeRef.current = theme
  }, [theme])

  const resizeCanvas = useCallback((canvas: HTMLCanvasElement) => {
    const rect = canvas.getBoundingClientRect()
    const dpr = window.devicePixelRatio || 1
    canvas.width = Math.max(1, Math.floor(rect.width * dpr))
    canvas.height = Math.max(1, Math.floor(rect.height * dpr))
    const context = canvas.getContext('2d')
    if (context) {
      context.setTransform(dpr, 0, 0, dpr, 0, 0)
    }
  }, [])

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    resizeCanvas(canvas)

    const renderFrame = () => {
      const context = canvas.getContext('2d')
      if (!context) return

      const rect = canvas.getBoundingClientRect()
      context.clearRect(0, 0, rect.width, rect.height)

      const now = performance.now()
      const viewSize = Math.min(rect.width, rect.height)
      const palette = LAKE_PALETTES[themeRef.current === 'dark' ? 'dark' : 'light']

      if (now - lastAmbientRef.current > AMBIENT_INTERVAL) {
        lastAmbientRef.current = now
        const ambientX = rect.width * (0.12 + Math.random() * 0.76)
        const ambientY = rect.height * (0.12 + Math.random() * 0.76)
        if (isWaterAtView(ambientX, ambientY, rect.width, rect.height)) {
          ripplesRef.current.push(createAmbientRipple(ambientX, ambientY, now, viewSize))
        }
      }

      const stillFalling: Stone[] = []
      for (const stone of stonesRef.current) {
        const progress = stoneProgress(stone, now)
        if (progress >= 1) {
          ripplesRef.current.push(createRipple(stone.x, stone.y, now, viewSize))
          playDropRef.current()
          continue
        }
        stillFalling.push(stone)
        drawStone(context, stone, progress, palette)
      }
      stonesRef.current = stillFalling

      if (ripplesRef.current.length > MAX_RIPPLES) {
        ripplesRef.current.splice(0, ripplesRef.current.length - MAX_RIPPLES)
      }
      ripplesRef.current = drawRipples(context, ripplesRef.current, palette, now)

      frameRef.current = requestAnimationFrame(renderFrame)
    }

    frameRef.current = requestAnimationFrame(renderFrame)

    const handleResize = () => resizeCanvas(canvas)
    const resizeObserver = new ResizeObserver(handleResize)
    resizeObserver.observe(canvas)

    return () => {
      if (frameRef.current !== null) cancelAnimationFrame(frameRef.current)
      resizeObserver.disconnect()
    }
  }, [resizeCanvas, isWaterAtView])

  const throwStone = (event: ReactPointerEvent<HTMLCanvasElement>) => {
    if (event.button > 0 || event.isPrimary === false) return

    const canvas = event.currentTarget
    const point = getLakePoint(event.nativeEvent, canvas)
    const rect = canvas.getBoundingClientRect()

    if (!isWaterAtView(point.x, point.y, rect.width, rect.height)) return

    const stones = stonesRef.current
    stones.push(createStone(point.x, point.y, performance.now(), rect.width, rect.height))
    if (stones.length > MAX_STONES) {
      stones.splice(0, stones.length - MAX_STONES)
    }

    onStoneThrown?.()
  }

  return (
    <>
      <img
        ref={backgroundRef}
        src={lakeBackground}
        alt=""
        aria-hidden="true"
        className="stones-lake__background"
        draggable={false}
        onLoad={handleBackgroundLoad}
      />
      <canvas
        ref={canvasRef}
        className="stones-lake__canvas"
        aria-label="Lago de agua. Hacé clic para lanzar piedras y ver las ondas expandirse"
        role="img"
        onPointerDown={throwStone}
      />
    </>
  )
}
