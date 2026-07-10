import {
  useCallback,
  useEffect,
  useRef,
  type PointerEvent as ReactPointerEvent,
} from 'react'
import { ImageDown, Trash2 } from 'lucide-react'
import { useSandAudio } from '../../hooks/useSandAudio'
import { canvasToPngBlob, downloadImageBlob } from '../../utils/downloadImage'
import {
  drawLatestGroove,
  getCanvasPoint,
  renderSandGarden,
  type SandPoint,
  type SandStroke,
} from './sandCanvas'
import sandZenFrame from '../../assets/zen-sand/sand-zen-draw.webp'
import './ZenSandCanvas.css'

interface ActivePointer {
  id: number
  lastPoint: SandPoint
  lastTime: number
  strokeIndex: number
}

interface ZenSandCanvasProps {
  onDraw?: () => void
}

const getIntensity = (speed: number) =>
  Math.min(1.08, Math.max(0.72, 1.08 - speed * 0.2))

export default function ZenSandCanvas({ onDraw }: ZenSandCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const frameRef = useRef<HTMLDivElement>(null)
  const frameImageRef = useRef<HTMLImageElement>(null)
  const strokesRef = useRef<SandStroke[]>([])
  const activePointerRef = useRef<ActivePointer | null>(null)
  const textureSeedRef = useRef(Date.now())
  const { start: startAudio, stop: stopAudio, update: updateAudio } =
    useSandAudio()

  const redraw = useCallback(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    renderSandGarden(canvas, strokesRef.current, textureSeedRef.current)
  }, [])

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    redraw()
    const resizeObserver = new ResizeObserver(redraw)
    resizeObserver.observe(canvas)

    return () => resizeObserver.disconnect()
  }, [redraw])

  const finishDrawing = useCallback(
    (pointerId?: number) => {
      const activePointer = activePointerRef.current
      if (!activePointer) return
      if (pointerId !== undefined && activePointer.id !== pointerId) return

      const canvas = canvasRef.current
      if (canvas?.hasPointerCapture(activePointer.id)) {
        canvas.releasePointerCapture(activePointer.id)
      }

      activePointerRef.current = null
      stopAudio()
    },
    [stopAudio],
  )

  useEffect(
    () => () => {
      activePointerRef.current = null
      stopAudio()
    },
    [stopAudio],
  )

  const handlePointerDown = (event: ReactPointerEvent<HTMLCanvasElement>) => {
    if (!event.isPrimary || event.button !== 0 || activePointerRef.current) return

    const canvas = event.currentTarget
    const point = getCanvasPoint(event.nativeEvent, canvas)
    const strokeIndex = strokesRef.current.push([point]) - 1

    event.preventDefault()
    canvas.setPointerCapture(event.pointerId)
    activePointerRef.current = {
      id: event.pointerId,
      lastPoint: point,
      lastTime: event.timeStamp,
      strokeIndex,
    }

    startAudio()
    redraw()
  }

  const handlePointerMove = (event: ReactPointerEvent<HTMLCanvasElement>) => {
    const activePointer = activePointerRef.current
    if (!activePointer || activePointer.id !== event.pointerId) return

    const canvas = event.currentTarget
    const rect = canvas.getBoundingClientRect()
    const pointerEvents = event.nativeEvent.getCoalescedEvents?.() ?? [
      event.nativeEvent,
    ]

    for (const pointerEvent of pointerEvents) {
      const point = getCanvasPoint(pointerEvent, canvas)
      const distance = Math.hypot(
        (point.x - activePointer.lastPoint.x) * rect.width,
        (point.y - activePointer.lastPoint.y) * rect.height,
      )
      const elapsed = Math.max(1, pointerEvent.timeStamp - activePointer.lastTime)

      if (distance < 0.5) continue

      onDraw?.()

      const speed = distance / elapsed
      point.intensity = getIntensity(speed)
      strokesRef.current[activePointer.strokeIndex].push(point)
      drawLatestGroove(canvas, activePointer.lastPoint, point)
      updateAudio(speed)

      activePointer.lastPoint = point
      activePointer.lastTime = pointerEvent.timeStamp
    }
  }

  const handleClear = () => {
    finishDrawing()
    strokesRef.current = []
    textureSeedRef.current += 1
    redraw()
  }

  const exportImage = async () => {
    const canvas = canvasRef.current
    const frame = frameRef.current
    const frameImage = frameImageRef.current
    if (!canvas || !frame || !frameImage) return null

    const frameRect = frame.getBoundingClientRect()
    const canvasRect = canvas.getBoundingClientRect()
    if (frameRect.width <= 0 || frameRect.height <= 0) return null

    const exportCanvas = document.createElement('canvas')
    exportCanvas.width = frameImage.naturalWidth || 1448
    exportCanvas.height = frameImage.naturalHeight || 1086

    const exportContext = exportCanvas.getContext('2d')
    if (!exportContext) return null

    exportContext.drawImage(frameImage, 0, 0, exportCanvas.width, exportCanvas.height)
    exportContext.drawImage(
      canvas,
      ((canvasRect.left - frameRect.left) / frameRect.width) * exportCanvas.width,
      ((canvasRect.top - frameRect.top) / frameRect.height) * exportCanvas.height,
      (canvasRect.width / frameRect.width) * exportCanvas.width,
      (canvasRect.height / frameRect.height) * exportCanvas.height,
    )

    return canvasToPngBlob(exportCanvas)
  }

  const handleExport = async () => {
    const blob = await exportImage()
    if (!blob) return

    await downloadImageBlob(blob, 'arena-zen.png')
  }

  return (
    <section className="zen-sand" aria-labelledby="zen-sand-title">
      <div className="zen-sand__mobile-actions">
        <button
          aria-label="Limpiar la arena"
          className="zen-sand__mobile-actions__btn"
          onClick={handleClear}
          type="button"
        >
          <Trash2 aria-hidden="true" size={18} />
          <span>Limpiar</span>
        </button>

        <button
          aria-label="Guardar dibujo de arena"
          className="zen-sand__mobile-actions__btn"
          onClick={handleExport}
          type="button"
        >
          <ImageDown aria-hidden="true" size={18} />
          <span>Guardar</span>
        </button>
      </div>

      <div className="zen-sand__frame" ref={frameRef}>
        <canvas
          ref={canvasRef}
          className="zen-sand__canvas"
          aria-label="Superficie de arena interactiva para dibujar"
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={event => finishDrawing(event.pointerId)}
          onPointerCancel={event => finishDrawing(event.pointerId)}
          onPointerLeave={event => finishDrawing(event.pointerId)}
          onLostPointerCapture={event => finishDrawing(event.pointerId)}
        />
        <img
          ref={frameImageRef}
          alt=""
          aria-hidden="true"
          className="zen-sand__frame-image"
          draggable={false}
          src={sandZenFrame}
        />
        <button
          aria-label="Descargar dibujo de arena"
          className="zen-sand__export-button"
          onClick={handleExport}
          type="button"
        >
          <ImageDown aria-hidden="true" size={22} />
        </button>
        <button
          aria-label="Limpiar arena y borrar todos los trazos"
          className="zen-sand__clear-button"
          onClick={handleClear}
          type="button"
        >
          <Trash2 aria-hidden="true" size={22} />
        </button>
      </div>

    </section>
  )
}
