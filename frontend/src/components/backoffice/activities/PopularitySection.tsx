import { CirclePlay } from 'lucide-react'
import { AdminActivityPopularityResponse } from '../../../api/adminActivities'

interface PopularitySectionProps {
  popularity: AdminActivityPopularityResponse[]
  totalSessions: number
  activityIcons: Record<string, any>
  activityThemes: Record<string, any>
}

export default function PopularitySection({
  popularity,
  totalSessions,
  activityIcons,
  activityThemes,
}: PopularitySectionProps) {
  const maxSessions = popularity.length > 0 ? Math.max(...popularity.map(m => m.totalSessions), 1) : 1

  return (
    <div className="flex flex-col gap-5 rounded-3xl border border-gray-100 bg-white dark:bg-[#161F30] dark:border-gray-800/50 p-6 shadow-sm h-full">
      <div>
        <h3 className="text-base font-extrabold text-gray-800 dark:text-white">Popularidad por Sesiones</h3>
        <p className="text-xs text-gray-400 mt-0.5">Total de sesiones iniciadas por actividad</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-5 mt-2">
        {[...popularity]
          .sort((a, b) => b.totalSessions - a.totalSessions)
          .map((item, index) => {
            const Icon = activityIcons[item.activityType] || CirclePlay
            const theme = activityThemes[item.activityType] || { bg: 'bg-gray-100', text: 'text-gray-600', bar: 'bg-gray-400' }
            const barWidthPercentage = maxSessions > 0 ? Math.round((item.totalSessions * 100) / maxSessions) : 0
            const sharePercentage = totalSessions > 0 ? Math.round((item.totalSessions * 100) / totalSessions) : 0

            return (
              <div key={item.activityType} className="flex flex-col gap-1.5">
                <div className="flex items-center justify-between text-xs font-bold">
                  <div className="flex items-center gap-2.5">
                    <div className={`flex h-7 w-7 items-center justify-center rounded-lg ${theme.bg}`}>
                      <Icon className="h-4 w-4" />
                    </div>
                    <span className="text-gray-700 dark:text-gray-300">
                      {item.activityName}
                    </span>
                    {index === 0 && item.totalSessions > 0 && (
                      <span className="rounded-md bg-teal-500/10 px-1.5 py-0.5 text-[11px] font-black tracking-widest text-teal-400 uppercase">
                        TOP
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-2 font-mono text-gray-400 text-[12px]">
                    <span>{item.totalSessions.toLocaleString('es-ES')}</span>
                    <span className="text-gray-505 font-normal">|</span>
                    <span className={theme.text}>{sharePercentage}%</span>
                  </div>
                </div>
                <div className="h-1.5 w-full bg-gray-100 dark:bg-gray-800/40 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full ${theme.bar}`}
                    style={{ width: `${barWidthPercentage}%` }}
                  ></div>
                </div>
              </div>
            )
          })}
      </div>
    </div>
  )
}
