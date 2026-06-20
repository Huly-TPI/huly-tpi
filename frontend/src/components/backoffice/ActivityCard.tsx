import { ProgressBar } from './ProgressBar'

export interface Activity {
  emoji: string
  name: string
  pct: number
  barColor: string
  iconBg: string
}

interface ActivityCardProps {
  activity: Activity
}

export function ActivityCard({ activity }: ActivityCardProps) {
  return (
    <div className="flex items-center gap-3 rounded-xl border border-gray-100 dark:border-gray-800/60 p-3">
      <div className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-lg ${activity.iconBg}`}>
        {activity.emoji}
      </div>
      <div className="min-w-0 flex-1">
        <p className="truncate text-xs font-semibold text-gray-700 dark:text-gray-200">{activity.name}</p>
        <div className="mt-1.5 flex items-center gap-2">
          <ProgressBar pct={activity.pct} colorClass={activity.barColor} />
          <span className="shrink-0 text-[10px] font-semibold text-gray-400 dark:text-gray-500">{activity.pct}%</span>
        </div>
      </div>
    </div>
  )
}
