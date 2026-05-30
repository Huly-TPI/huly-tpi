import { Emoji } from '../icons/Emoji'
import { ProgressBar } from './ProgressBar'

export type EmotionSeverity = 'ALTA' | 'MEDIA' | 'BAJA'

export interface EmotionCategory {
  emoji: string
  iconSrc?: string
  name: string
  flows: number
  detect: number
  severity: EmotionSeverity
  /** Hex color with alpha for icon background, e.g. "#EF444433" */
  iconBg: string
  /** Hex color for the color strip, e.g. "#EF4444" */
  barColor: string
  /** Hex color for the severity label, e.g. "#EF4444" */
  badgeColor: string
}

const SEVERITY_STYLE: Record<EmotionSeverity, { bar: string; text: string }> = {
  ALTA:  { bar: 'bg-red-500',   text: 'text-red-500'   },
  MEDIA: { bar: 'bg-amber-400', text: 'text-amber-400' },
  BAJA:  { bar: 'bg-gray-200',  text: 'text-gray-300'  },
}

interface EmotionCardProps {
  category: EmotionCategory
  onManage?: () => void
}

export function EmotionCard({ category, onManage }: EmotionCardProps) {
  const sev = SEVERITY_STYLE[category.severity]
  return (
    <div className="flex flex-col rounded-2xl bg-white p-5 shadow-sm transition-shadow hover:shadow-md cursor-default">
      {category.iconSrc
        ? <img src={category.iconSrc} alt={category.name} className="w-14 h-14 object-contain" />
        : (
          <div
            className="flex h-14 w-14 items-center justify-center rounded-2xl"
            style={{ backgroundColor: category.iconBg }}
          >
            <Emoji emoji={category.emoji} className="w-8 h-8" />
          </div>
        )
      }

      <p className="mt-4 text-[15px] font-bold text-[#2D3748]">{category.name}</p>
      <p className="mt-1 text-xs text-[#A0AEC0]">
        {category.flows} flujos · {category.detect}% detect.
      </p>

      <ProgressBar pct={category.detect} colorClass={sev.bar} height="h-[3px]" />

      <div className="mt-3 flex items-center justify-between">
        <span className={`text-xs font-bold ${sev.text}`}>
          {category.severity}
        </span>
        <button
          onClick={onManage}
          className="text-[#A0AEC0] hover:text-[#8869AC] transition-colors"
          aria-label={`Gestionar ${category.name}`}
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  )
}
