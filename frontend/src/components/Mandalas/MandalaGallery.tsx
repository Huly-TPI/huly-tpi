import { ChevronLeft, ChevronRight } from 'lucide-react'
import MandalaCard from './MandalaCard'
import type { MandalaCatalogItem } from './mandalaTypes'

interface MandalaGalleryProps {
  mandalas: MandalaCatalogItem[]
  page: number
  totalPages: number
  first: boolean
  last: boolean
  onPageChange: (page: number) => void
  onSelectMandala: (mandala: MandalaCatalogItem) => void
}

export default function MandalaGallery({
  mandalas,
  page,
  totalPages,
  first,
  last,
  onPageChange,
  onSelectMandala,
}: MandalaGalleryProps) {
  return (
    <section className="mandala-gallery" aria-label="Seleccionar mandala">
      <div className="mandala-gallery__grid">
        {mandalas.map(mandala => (
          <MandalaCard key={mandala.id} mandala={mandala} onSelect={onSelectMandala} />
        ))}
      </div>

      {totalPages > 1 && (
        <nav className="mandala-gallery__pagination" aria-label="Paginación de mandalas">
          <button
            aria-label="Página anterior"
            className="mandala-gallery__page-button"
            disabled={first}
            onClick={() => onPageChange(page - 1)}
            type="button"
          >
            <ChevronLeft aria-hidden="true" size={22} />
          </button>

          <div className="mandala-gallery__page-dots" aria-hidden="true">
            {Array.from({ length: totalPages }, (_, index) => (
              <span
                className={`mandala-gallery__page-dot ${
                  index === page ? 'mandala-gallery__page-dot--active' : ''
                }`}
                key={index}
              />
            ))}
          </div>

          <button
            aria-label="Página siguiente"
            className="mandala-gallery__page-button"
            disabled={last}
            onClick={() => onPageChange(page + 1)}
            type="button"
          >
            <ChevronRight aria-hidden="true" size={22} />
          </button>
        </nav>
      )}
    </section>
  )
}
