import { forwardRef, useImperativeHandle } from 'react'
import { useMandalaPainter } from '../../hooks/useMandalaPainter'

export interface MandalaCanvasHandle {
  clear: () => void
  exportImage: () => Promise<Blob | null>
}

interface MandalaCanvasProps {
  brushSize: number
  color: string
  initialPaintBlob?: Blob | null
  mandalaSrc: string
  onPaintChange?: (blob: Blob) => void
}

const MandalaCanvas = forwardRef<MandalaCanvasHandle, MandalaCanvasProps>(
  ({ brushSize, color, initialPaintBlob, mandalaSrc, onPaintChange }, ref) => {
    const painter = useMandalaPainter({
      brushSize,
      color,
      initialPaintBlob,
      mandalaSrc,
      onPaintChange,
    })

    useImperativeHandle(ref, () => ({
      clear: painter.clear,
      exportImage: painter.exportImage,
    }), [painter.clear, painter.exportImage])

    return (
      <div className="mandala-canvas" ref={painter.frameRef}>
        <canvas className="mandala-canvas__layer" ref={painter.paintCanvasRef} aria-hidden="true" />
        <canvas className="mandala-canvas__layer mandala-canvas__lines" ref={painter.lineCanvasRef} aria-hidden="true" />
        <canvas
          ref={painter.inputCanvasRef}
          className="mandala-canvas__layer mandala-canvas__input"
          aria-label="Mandala para colorear"
          onPointerCancel={painter.handlePointerCancel}
          onPointerDown={painter.handlePointerDown}
          onPointerLeave={painter.handlePointerLeave}
          onPointerMove={painter.handlePointerMove}
          onPointerUp={painter.handlePointerUp}
          role="img"
        />

        {painter.status !== 'ready' && (
          <div className="mandala-canvas__status" role="status">
            {painter.status === 'loading' ? 'Cargando mandala...' : 'No se pudo cargar el mandala'}
          </div>
        )}
      </div>
    )
  },
)

MandalaCanvas.displayName = 'MandalaCanvas'

export default MandalaCanvas
