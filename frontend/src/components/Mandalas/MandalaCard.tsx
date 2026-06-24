import { Lock } from 'lucide-react'
import modalEleccionMandala from '../../assets/mandalas/modal-eleccion-mandala.webp'
import type { MandalaCatalogItem } from './mandalaTypes'

interface MandalaCardProps {
  mandala: MandalaCatalogItem
  onSelect: (mandala: MandalaCatalogItem) => void
  onLockedClick?: (accessType: MandalaCatalogItem['accessType']) => void
}

const unlockedBadge: Record<MandalaCatalogItem['unlockSource'], { label: string; className: string }> = {
  free: { label: 'Gratis', className: 'mandala-card__badge--free' },
  purchased: { label: 'Comprado', className: 'mandala-card__badge--purchased' },
  premiumPlan: { label: 'Suscripción', className: 'mandala-card__badge--premium' },
}

const lockedBadge: Record<MandalaCatalogItem['accessType'], { label: string; className: string }> = {
  free: { label: 'Gratis', className: 'mandala-card__badge--free' },
  subscription: { label: 'Suscripción', className: 'mandala-card__badge--premium' },
  purchasable: { label: 'Con monedas', className: 'mandala-card__badge--purchasable' },
}

export default function MandalaCard({ mandala, onSelect, onLockedClick }: MandalaCardProps) {
  const previewSrc = mandala.previewSrc ?? mandala.src
  const badge = mandala.isLocked ? lockedBadge[mandala.accessType] : unlockedBadge[mandala.unlockSource]

  const handleClick = () => {
    if (mandala.isLocked) {
      onLockedClick?.(mandala.accessType)
    } else {
      onSelect(mandala)
    }
  }

  return (
    <button
      aria-label={mandala.isLocked ? `${mandala.title} (bloqueado)` : `Elegir ${mandala.title} (${badge.label})`}
      className={`mandala-card${mandala.isLocked ? ' mandala-card--locked' : ''}`}
      onClick={handleClick}
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
      {mandala.isLocked && (
        <span className="mandala-card__lock-overlay" aria-hidden="true">
          <Lock size={26} strokeWidth={2.5} />
        </span>
      )}
      <span className={`mandala-card__badge ${badge.className}`} aria-hidden="true">
        {badge.label}
      </span>
    </button>
  )
}
