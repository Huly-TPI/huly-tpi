import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type PointerEvent as ReactPointerEvent,
} from 'react'
import {
  createMandalaAnalysis,
  createRegionMask,
  drawMaskToCanvas,
  getPointerCanvasPoint,
  renderLineOverlay,
  type CanvasPoint,
  type MandalaAnalysis,
} from '../components/Mandalas/mandalaCanvas'
import { canvasToPngBlob } from '../utils/downloadImage'

type PainterStatus = 'loading' | 'ready' | 'error'

interface UseMandalaPainterOptions {
  brushSize: number
  color: string
  initialPaintBlob?: Blob | null
  mandalaSrc: string
  onPaintChange?: (blob: Blob) => void
}

const MAX_PIXEL_RATIO = 2

const loadImage = (src: string) =>
  new Promise<HTMLImageElement>((resolve, reject) => {
    const image = new Image()
    image.decoding = 'async'
    image.onload = () => resolve(image)
    image.onerror = reject
    image.src = src
  })

const setupCanvas = (canvas: HTMLCanvasElement, width: number, height: number) => {
  if (canvas.width !== width) canvas.width = width
  if (canvas.height !== height) canvas.height = height
}

export function useMandalaPainter({
  brushSize,
  color,
  initialPaintBlob,
  mandalaSrc,
  onPaintChange,
}: UseMandalaPainterOptions) {
  const frameRef = useRef<HTMLDivElement>(null)
  const paintCanvasRef = useRef<HTMLCanvasElement>(null)
  const lineCanvasRef = useRef<HTMLCanvasElement>(null)
  const inputCanvasRef = useRef<HTMLCanvasElement>(null)
  const maskCanvasRef = useRef<HTMLCanvasElement>(document.createElement('canvas'))
  const strokeCanvasRef = useRef<HTMLCanvasElement>(document.createElement('canvas'))
  const imageRef = useRef<HTMLImageElement | null>(null)
  const analysisRef = useRef<MandalaAnalysis | null>(null)
  const lastPointRef = useRef<CanvasPoint | null>(null)
  const activePointerIdRef = useRef<number | null>(null)
  const hasActiveStrokeRef = useRef(false)
  const brushSizeRef = useRef(brushSize)
  const colorRef = useRef(color)
  const onPaintChangeRef = useRef(onPaintChange)
  const [status, setStatus] = useState<PainterStatus>('loading')

  useEffect(() => {
    brushSizeRef.current = brushSize
  }, [brushSize])

  useEffect(() => {
    colorRef.current = color
  }, [color])

  useEffect(() => {
    onPaintChangeRef.current = onPaintChange
  }, [onPaintChange])

  const canvases = useMemo(
    () => [paintCanvasRef, lineCanvasRef, inputCanvasRef],
    [],
  )

  const renderMandala = useCallback(() => {
    const frame = frameRef.current
    const image = imageRef.current
    const paintCanvas = paintCanvasRef.current
    const lineCanvas = lineCanvasRef.current
    const inputCanvas = inputCanvasRef.current
    if (!frame || !image || !paintCanvas || !lineCanvas || !inputCanvas) return

    const frameRect = frame.getBoundingClientRect()
    const cssSize = Math.max(1, Math.floor(Math.min(frameRect.width, frameRect.height)))
    const pixelRatio = Math.min(window.devicePixelRatio || 1, MAX_PIXEL_RATIO)
    const size = Math.max(1, Math.round(cssSize * pixelRatio))

    const previousPaint = document.createElement('canvas')
    if (paintCanvas.width > 0 && paintCanvas.height > 0) {
      previousPaint.width = paintCanvas.width
      previousPaint.height = paintCanvas.height
      previousPaint.getContext('2d')?.drawImage(paintCanvas, 0, 0)
    }

    for (const canvasRef of canvases) {
      const canvas = canvasRef.current
      if (canvas) setupCanvas(canvas, size, size)
    }

    setupCanvas(maskCanvasRef.current, size, size)
    setupCanvas(strokeCanvasRef.current, size, size)

    const paintContext = paintCanvas.getContext('2d')
    if (paintContext) {
      paintContext.clearRect(0, 0, size, size)
      if (previousPaint.width > 0 && previousPaint.height > 0) {
        paintContext.drawImage(previousPaint, 0, 0, size, size)
      }
    }

    const analysisCanvas = document.createElement('canvas')
    setupCanvas(analysisCanvas, size, size)
    const analysisContext = analysisCanvas.getContext('2d', { willReadFrequently: true })
    if (!analysisContext) {
      setStatus('error')
      return
    }

    analysisContext.clearRect(0, 0, size, size)
    analysisContext.drawImage(image, 0, 0, size, size)

    const imageData = analysisContext.getImageData(0, 0, size, size)
    analysisRef.current = createMandalaAnalysis(imageData, 1)
    renderLineOverlay(imageData, lineCanvas)
    setStatus('ready')
  }, [canvases])

  useEffect(() => {
    let cancelled = false
    setStatus('loading')
    imageRef.current = null
    analysisRef.current = null

    loadImage(mandalaSrc)
      .then(image => {
        if (cancelled) return
        imageRef.current = image
        renderMandala()
      })
      .catch(() => {
        if (!cancelled) setStatus('error')
      })

    return () => {
      cancelled = true
    }
  }, [mandalaSrc, renderMandala])

  useEffect(() => {
    const frame = frameRef.current
    if (!frame) return

    const resizeObserver = new ResizeObserver(renderMandala)
    resizeObserver.observe(frame)

    return () => resizeObserver.disconnect()
  }, [renderMandala])

  const clear = useCallback(() => {
    const paintCanvas = paintCanvasRef.current
    const paintContext = paintCanvas?.getContext('2d')
    if (!paintCanvas || !paintContext) return

    paintContext.clearRect(0, 0, paintCanvas.width, paintCanvas.height)
    lastPointRef.current = null
    activePointerIdRef.current = null
    hasActiveStrokeRef.current = false
  }, [])

  const exportImage = useCallback(async () => {
    const paintCanvas = paintCanvasRef.current
    const lineCanvas = lineCanvasRef.current
    if (!paintCanvas || !lineCanvas || status !== 'ready') return null
    if (paintCanvas.width <= 0 || paintCanvas.height <= 0) return null

    const exportCanvas = document.createElement('canvas')
    exportCanvas.width = paintCanvas.width
    exportCanvas.height = paintCanvas.height

    const exportContext = exportCanvas.getContext('2d')
    if (!exportContext) return null

    exportContext.fillStyle = '#ffffff'
    exportContext.fillRect(0, 0, exportCanvas.width, exportCanvas.height)
    exportContext.drawImage(paintCanvas, 0, 0)
    exportContext.drawImage(lineCanvas, 0, 0)

    return canvasToPngBlob(exportCanvas)
  }, [status])

  useEffect(() => {
    const paintCanvas = paintCanvasRef.current
    const paintContext = paintCanvas?.getContext('2d')
    if (!paintCanvas || !paintContext || status !== 'ready' || !initialPaintBlob) return

    let cancelled = false
    const objectUrl = URL.createObjectURL(initialPaintBlob)
    const image = new Image()
    image.onload = () => {
      if (!cancelled) {
        paintContext.clearRect(0, 0, paintCanvas.width, paintCanvas.height)
        paintContext.drawImage(image, 0, 0, paintCanvas.width, paintCanvas.height)
      }
      URL.revokeObjectURL(objectUrl)
    }
    image.onerror = () => URL.revokeObjectURL(objectUrl)
    image.src = objectUrl

    return () => {
      cancelled = true
      URL.revokeObjectURL(objectUrl)
    }
  }, [initialPaintBlob, status])

  const emitPaintChange = useCallback(() => {
    const paintCanvas = paintCanvasRef.current
    const onPaintChangeHandler = onPaintChangeRef.current
    if (!paintCanvas || !onPaintChangeHandler) return

    paintCanvas.toBlob(blob => {
      if (blob) onPaintChangeHandler(blob)
    }, 'image/png')
  }, [])

  const drawMaskedSegment = useCallback((from: CanvasPoint, to: CanvasPoint) => {
    const paintCanvas = paintCanvasRef.current
    const strokeCanvas = strokeCanvasRef.current
    const maskCanvas = maskCanvasRef.current
    if (!paintCanvas) return

    const paintContext = paintCanvas.getContext('2d')
    const strokeContext = strokeCanvas.getContext('2d')
    if (!paintContext || !strokeContext) return

    strokeContext.clearRect(0, 0, strokeCanvas.width, strokeCanvas.height)
    strokeContext.save()
    strokeContext.strokeStyle = colorRef.current
    strokeContext.fillStyle = colorRef.current
    strokeContext.lineWidth = brushSizeRef.current
    strokeContext.lineCap = 'round'
    strokeContext.lineJoin = 'round'

    const distance = Math.hypot(to.x - from.x, to.y - from.y)
    if (distance < 0.5) {
      strokeContext.beginPath()
      strokeContext.arc(to.x, to.y, brushSizeRef.current / 2, 0, Math.PI * 2)
      strokeContext.fill()
    } else {
      strokeContext.beginPath()
      strokeContext.moveTo(from.x, from.y)
      strokeContext.lineTo(to.x, to.y)
      strokeContext.stroke()
    }

    strokeContext.globalCompositeOperation = 'destination-in'
    strokeContext.drawImage(maskCanvas, 0, 0)
    strokeContext.restore()

    paintContext.drawImage(strokeCanvas, 0, 0)
  }, [])

  const finishStroke = useCallback((pointerId?: number) => {
    const inputCanvas = inputCanvasRef.current
    const activePointerId = activePointerIdRef.current
    if (activePointerId === null) return
    if (pointerId !== undefined && pointerId !== activePointerId) return

    const shouldSave = hasActiveStrokeRef.current

    if (inputCanvas?.hasPointerCapture(activePointerId)) {
      inputCanvas.releasePointerCapture(activePointerId)
    }

    activePointerIdRef.current = null
    lastPointRef.current = null
    hasActiveStrokeRef.current = false

    if (shouldSave) emitPaintChange()
  }, [emitPaintChange])

  useEffect(() => () => finishStroke(), [finishStroke])

  const handlePointerDown = useCallback((event: ReactPointerEvent<HTMLCanvasElement>) => {
    if (!event.isPrimary || activePointerIdRef.current !== null) return
    if (event.pointerType === 'mouse' && event.button !== 0) return

    const analysis = analysisRef.current
    const inputCanvas = inputCanvasRef.current
    if (!analysis || !inputCanvas) return

    const point = getPointerCanvasPoint(event.nativeEvent, inputCanvas)
    if (!point) return

    const mask = createRegionMask(point, analysis)
    if (!mask) return

    event.preventDefault()
    inputCanvas.setPointerCapture(event.pointerId)
    drawMaskToCanvas(mask, maskCanvasRef.current, analysis.width, analysis.height)
    activePointerIdRef.current = event.pointerId
    lastPointRef.current = point
    hasActiveStrokeRef.current = true
    drawMaskedSegment(point, point)
  }, [drawMaskedSegment])

  const handlePointerMove = useCallback((event: ReactPointerEvent<HTMLCanvasElement>) => {
    if (activePointerIdRef.current !== event.pointerId) return

    const inputCanvas = inputCanvasRef.current
    const lastPoint = lastPointRef.current
    if (!inputCanvas || !lastPoint) return

    const pointerEvents = event.nativeEvent.getCoalescedEvents?.() ?? [event.nativeEvent]
    let nextLastPoint = lastPoint

    for (const pointerEvent of pointerEvents) {
      const point = getPointerCanvasPoint(pointerEvent, inputCanvas)
      if (!point) continue

      if (Math.hypot(point.x - nextLastPoint.x, point.y - nextLastPoint.y) < 0.5) continue

      drawMaskedSegment(nextLastPoint, point)
      hasActiveStrokeRef.current = true
      nextLastPoint = point
    }

    lastPointRef.current = nextLastPoint
  }, [drawMaskedSegment])

  return {
    clear,
    exportImage,
    frameRef,
    handlePointerCancel: (event: ReactPointerEvent<HTMLCanvasElement>) => finishStroke(event.pointerId),
    handlePointerDown,
    handlePointerLeave: (event: ReactPointerEvent<HTMLCanvasElement>) => finishStroke(event.pointerId),
    handlePointerMove,
    handlePointerUp: (event: ReactPointerEvent<HTMLCanvasElement>) => finishStroke(event.pointerId),
    inputCanvasRef,
    lineCanvasRef,
    paintCanvasRef,
    status,
  }
}
