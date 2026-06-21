import MandalaCard from './MandalaCard'
import type { MandalaCatalogItem } from './mandalaTypes'

interface MandalaGalleryProps {
  mandalas: MandalaCatalogItem[]
  onSelectMandala: (mandala: MandalaCatalogItem) => void
}

export default function MandalaGallery({ mandalas, onSelectMandala }: MandalaGalleryProps) {
  return (
    <section className="mandala-gallery" aria-labelledby="mandala-gallery-title">
      <div className="mandala-gallery__header">
        <p className="mandala-gallery__eyebrow">Colorear</p>
        <h1 id="mandala-gallery-title">Elegí un mandala</h1>
        <p>
          Probá distintas formas y colores. Cada mandala guarda su progreso en este navegador.
        </p>
      </div>

      <div className="mandala-gallery__grid">
        {mandalas.map(mandala => (
          <MandalaCard key={mandala.id} mandala={mandala} onSelect={onSelectMandala} />
        ))}
      </div>
    </section>
  )
}
