import { Lock, Paintbrush, Sparkles } from 'lucide-react'
import type { MandalaAccessStatus, MandalaCatalogItem } from './mandalaTypes'
import { isMandalaPaintable } from './mandalaTypes'

interface MandalaCardProps {
  mandala: MandalaCatalogItem
  onSelect: (mandala: MandalaCatalogItem) => void
}

const STATUS_LABELS: Record<MandalaAccessStatus, string> = {
  available: 'Gratis',
  locked: 'Bloqueada',
  premium: 'Premium',
  included: 'Incluida',
  comingSoon: 'Proximamente',
}

const STATUS_CLASS_NAMES: Record<MandalaAccessStatus, string> = {
  available: 'mandala-card__badge--available',
  locked: 'mandala-card__badge--locked',
  premium: 'mandala-card__badge--premium',
  included: 'mandala-card__badge--included',
  comingSoon: 'mandala-card__badge--soon',
}

function getActionLabel(status: MandalaAccessStatus) {
  if (status === 'available' || status === 'included') return 'Pintar'
  if (status === 'comingSoon') return 'Proximamente'
  return 'Bloqueada'
}

function getStatusIcon(status: MandalaAccessStatus) {
  if (status === 'premium' || status === 'included') return <Sparkles aria-hidden="true" size={15} />
  if (status === 'locked' || status === 'comingSoon') return <Lock aria-hidden="true" size={15} />
  return <Paintbrush aria-hidden="true" size={15} />
}

export default function MandalaCard({ mandala, onSelect }: MandalaCardProps) {
  const paintable = isMandalaPaintable(mandala)
  const previewSrc = mandala.previewSrc ?? mandala.src

  return (
    <article className={`mandala-card ${paintable ? '' : 'mandala-card--disabled'}`}>
      <div className="mandala-card__preview" aria-hidden="true">
        <img src={previewSrc} alt="" draggable={false} />
      </div>

      <div className="mandala-card__body">
        <div className="mandala-card__title-row">
          <h2>{mandala.title}</h2>
          <span className={`mandala-card__badge ${STATUS_CLASS_NAMES[mandala.accessStatus]}`}>
            {getStatusIcon(mandala.accessStatus)}
            {STATUS_LABELS[mandala.accessStatus]}
          </span>
        </div>

        {mandala.description && <p>{mandala.description}</p>}

        <button
          className="mandala-card__action"
          disabled={!paintable}
          onClick={() => onSelect(mandala)}
          type="button"
        >
          {getActionLabel(mandala.accessStatus)}
        </button>
      </div>
    </article>
  )
}
