import { Brush, Pencil } from 'lucide-react'

interface MandalaToolbarProps {
  brushSize: number
  colors: string[]
  selectedColor: string
  onBrushSizeChange: (value: number) => void
  onColorChange: (color: string) => void
}

export default function MandalaToolbar({
  brushSize,
  colors,
  selectedColor,
  onBrushSizeChange,
  onColorChange,
}: MandalaToolbarProps) {
  return (
    <aside className="mandala-toolbar" aria-label="Herramientas de mandala">
      <div className="mandala-toolbar__section">
        <div className="mandala-toolbar__heading">
          <span>Colores</span>
        </div>

        <div className="mandala-toolbar__group" aria-label="Colores de pintura">
          {colors.map(color => (
            <button
              aria-label={`Usar color ${color}`}
              aria-pressed={selectedColor === color}
              className="mandala-toolbar__swatch"
              key={color}
              onClick={() => onColorChange(color)}
              style={{ backgroundColor: color }}
              type="button"
            >
              <span aria-hidden="true" className="mandala-toolbar__swatch-inner" />
            </button>
          ))}
        </div>
      </div>

      <div className="mandala-toolbar__divider" />

      <div className="mandala-toolbar__section">
        <div className="mandala-toolbar__heading">
          <span>Herramientas</span>
        </div>

        <div className="mandala-toolbar__tools" aria-label="Herramientas de dibujo">
          <button
            aria-label="Pincel"
            aria-pressed={brushSize === 40}
            className={`mandala-toolbar__tool-btn ${brushSize === 40 ? 'is-active' : ''}`}
            onClick={() => onBrushSizeChange(40)}
            style={brushSize === 40 ? {
              backgroundColor: selectedColor,
              color: '#fff',
              borderColor: 'white',
              boxShadow: `0 0 0 2px var(--easel-bg, white), 0 0 0 4px ${selectedColor}, 0 4px 12px rgba(45, 38, 56, 0.16)`
            } : {}}
            type="button"
          >
            <Brush aria-hidden="true" size={22} />
            <span className="mandala-toolbar__tool-label">Pincel</span>
          </button>

          <button
            aria-label="Lápiz"
            aria-pressed={brushSize === 3}
            className={`mandala-toolbar__tool-btn ${brushSize === 3 ? 'is-active' : ''}`}
            onClick={() => onBrushSizeChange(3)}
            style={brushSize === 3 ? {
              backgroundColor: selectedColor,
              color: '#fff',
              borderColor: 'white',
              boxShadow: `0 0 0 2px var(--easel-bg, white), 0 0 0 4px ${selectedColor}, 0 4px 12px rgba(45, 38, 56, 0.16)`
            } : {}}
            type="button"
          >
            <Pencil aria-hidden="true" size={22} />
            <span className="mandala-toolbar__tool-label">Lápiz</span>
          </button>
        </div>
      </div>
    </aside>
  )
}
