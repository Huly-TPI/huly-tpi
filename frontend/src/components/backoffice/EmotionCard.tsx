import { ChevronRight, type LucideIcon } from 'lucide-react'
import { ProgressBar } from './ProgressBar'

export type EmotionSeverity = 'ALTA' | 'MEDIA' | 'BAJA'

export interface EmotionCategory {
  Icon: LucideIcon
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
  ALTA: { bar: 'bg-red-500', text: 'text-red-500' },
  MEDIA: { bar: 'bg-amber-400', text: 'text-amber-400' },
  BAJA: { bar: 'bg-gray-200 dark:bg-gray-700', text: 'text-gray-300 dark:text-gray-500' },
}

interface EmotionCardProps {
  category: EmotionCategory
  onManage?: () => void
}

export function EmotionCard({ category, onManage }: EmotionCardProps) {
  const sev = SEVERITY_STYLE[category.severity]
  const Icon = category.Icon

  return (
    <div className="flex flex-col rounded-2xl bg-white dark:bg-[#172033] p-5 shadow-sm transition-shadow hover:shadow-md cursor-default">
      <div
        className="flex h-14 w-14 items-center justify-center rounded-2xl"
        style={{ backgroundColor: category.iconBg }}
      >
        <Icon className="h-7 w-7" strokeWidth={2} style={{ color: category.badgeColor }} />
      </div>

      <p className="mt-4 text-[15px] font-bold text-[#2D3748] dark:text-gray-100">{category.name}</p>
      <p className="mt-1 text-xs text-[#A0AEC0] dark:text-gray-500">
        {category.flows} flujos · {category.detect}% detect.
      </p>

      <ProgressBar pct={category.detect} colorClass={sev.bar} height="h-[3px]" />

      <div className="mt-3 flex items-center justify-between">
        <span className={`text-xs font-bold ${sev.text}`}>
          {category.severity}
        </span>
        <button
          onClick={onManage}
          className="text-[#A0AEC0] dark:text-gray-500 hover:text-violeta dark:hover:text-violeta-claro transition-colors"
          aria-label={`Gestionar ${category.name}`}
        >
          <ChevronRight className="w-4 h-4" strokeWidth={2} />
        </button>
      </div>
    </div>
  )
}
