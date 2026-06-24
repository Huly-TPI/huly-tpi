import modalEleccionMandala from '../../assets/mandalas/modal-eleccion-mandala.webp'
import type { MandalaCatalogItem } from './mandalaTypes'

interface MandalaCardProps {
  mandala: MandalaCatalogItem
  onSelect: (mandala: MandalaCatalogItem) => void
}

const unlockBadgeBySource: Record<MandalaCatalogItem['unlockSource'], { label: string; className: string }> = {
  free: { label: 'Gratis', className: 'mandala-card__badge--free' },
  purchased: { label: 'Comprado', className: 'mandala-card__badge--purchased' },
  premiumPlan: { label: 'Suscripción', className: 'mandala-card__badge--premium' },
}

export default function MandalaCard({ mandala, onSelect }: MandalaCardProps) {
  const previewSrc = mandala.previewSrc ?? mandala.src
  const badge = unlockBadgeBySource[mandala.unlockSource]

  return (
    <button
      aria-label={`Elegir ${mandala.title} (${badge.label})`}
      className="mandala-card"
      onClick={() => onSelect(mandala)}
      type="button"
    >
      <img
        alt=""
        aria-hidden="true"
        className="mandala-card__frame"
        draggable={false}
        src={modalEleccionMandala}
      />
      <span className="mandala-card__preview" aria-hidden="true">
        <img src={previewSrc} alt="" draggable={false} />
      </span>
      <span className={`mandala-card__badge ${badge.className}`} aria-hidden="true">
        {badge.label}
      </span>
    </button>
  )
}
