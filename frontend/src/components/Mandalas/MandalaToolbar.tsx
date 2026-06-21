import { Brush, Trash2 } from 'lucide-react'

interface MandalaToolbarProps {
  brushSize: number
  colors: string[]
  selectedColor: string
  onBrushSizeChange: (value: number) => void
  onClear: () => void
  onColorChange: (color: string) => void
}

export default function MandalaToolbar({
  brushSize,
  colors,
  selectedColor,
  onBrushSizeChange,
  onClear,
  onColorChange,
}: MandalaToolbarProps) {
  return (
    <aside className="mandala-toolbar" aria-label="Herramientas de mandala">
      <div className="mandala-toolbar__group" aria-label="Colores">
        {colors.map(color => (
          <button
            aria-label={`Usar color ${color}`}
            aria-pressed={selectedColor === color}
            className="mandala-toolbar__swatch"
            key={color}
            onClick={() => onColorChange(color)}
            style={{ backgroundColor: color }}
            type="button"
          />
        ))}
      </div>

      <label className="mandala-toolbar__field">
        <span className="mandala-toolbar__brush-label">
          <Brush aria-hidden="true" size={18} />
          Grosor {brushSize}
        </span>
        <input
          className="mandala-toolbar__range"
          max={48}
          min={4}
          onChange={event => onBrushSizeChange(Number(event.target.value))}
          type="range"
          value={brushSize}
        />
      </label>

      <button className="mandala-toolbar__clear" onClick={onClear} type="button">
        <Trash2 aria-hidden="true" size={18} />
        Limpiar
      </button>
    </aside>
  )
}
