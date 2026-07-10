import { type LucideIcon } from 'lucide-react'
import { ProgressBar } from './ProgressBar'

export type EmotionSeverity = 'ALTA' | 'MEDIA' | 'BAJA'

export interface EmotionCategory {
  Icon: LucideIcon
  name: string
  detections: number
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
}

export function EmotionCard({ category }: EmotionCardProps) {
  const sev = SEVERITY_STYLE[category.severity]
  const Icon = category.Icon

  return (
    <div className="flex flex-col rounded-2xl bg-gray-50 dark:bg-[#09111f] p-4 shadow-sm transition-shadow hover:shadow-md cursor-default">
      <div
        className="flex h-11 w-11 items-center justify-center rounded-xl"
        style={{ backgroundColor: category.iconBg }}
      >
        <Icon className="h-5 w-5" strokeWidth={2} style={{ color: category.badgeColor }} />
      </div>

      <p className="mt-3 text-[13px] font-bold text-[#2D3748] dark:text-gray-100 truncate" title={category.name}>{category.name}</p>
      <p className="mt-0.5 text-[11px] text-[#A0AEC0] dark:text-gray-500">
        {category.detections} {category.detections === 1 ? 'detección' : 'detecciones'}
      </p>
      <p className="mt-0.5 text-[11px] text-[#A0AEC0] dark:text-gray-500">
        {category.detect}% conf.
      </p>

      <ProgressBar pct={category.detect} colorClass={sev.bar} height="h-[3px]" className="mt-2" />

      <div className="mt-2 flex items-center justify-between">
        <span className={`text-[11px] font-bold ${sev.text}`}>
          {category.severity}
        </span>
      </div>
    </div>
  )
}
