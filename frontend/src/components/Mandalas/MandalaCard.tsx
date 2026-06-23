import { Paintbrush, Sparkles } from 'lucide-react'
import type { MandalaCatalogItem, MandalaUnlockSource } from './mandalaTypes'

interface MandalaCardProps {
  mandala: MandalaCatalogItem
  onSelect: (mandala: MandalaCatalogItem) => void
}

const SOURCE_LABELS: Record<MandalaUnlockSource, string> = {
  free: 'Gratis',
  purchased: 'Tuya',
  premiumPlan: 'Incluida',
}

const SOURCE_CLASS_NAMES: Record<MandalaUnlockSource, string> = {
  free: 'mandala-card__badge--available',
  purchased: 'mandala-card__badge--available',
  premiumPlan: 'mandala-card__badge--included',
}

function getStatusIcon(source: MandalaUnlockSource) {
  if (source === 'premiumPlan') return <Sparkles aria-hidden="true" size={15} />
  return <Paintbrush aria-hidden="true" size={15} />
}

const STATUS_CLASS_NAMES = {
  available: 'mandala-card__badge--available',
  included: 'mandala-card__badge--included',
}

export default function MandalaCard({ mandala, onSelect }: MandalaCardProps) {
  const previewSrc = mandala.previewSrc ?? mandala.src
  const badgeClassName = SOURCE_CLASS_NAMES[mandala.unlockSource] ?? STATUS_CLASS_NAMES[mandala.accessStatus]

  return (
    <article className="mandala-card">
      <div className="mandala-card__preview" aria-hidden="true">
        <img src={previewSrc} alt="" draggable={false} />
      </div>

      <div className="mandala-card__body">
        <div className="mandala-card__title-row">
          <h2>{mandala.title}</h2>
          <span className={`mandala-card__badge ${badgeClassName}`}>
            {getStatusIcon(mandala.unlockSource)}
            {SOURCE_LABELS[mandala.unlockSource]}
          </span>
        </div>

        {mandala.description && <p>{mandala.description}</p>}

        <button
          className="mandala-card__action"
          onClick={() => onSelect(mandala)}
          type="button"
        >
          Pintar
        </button>
      </div>
    </article>
  )
}
