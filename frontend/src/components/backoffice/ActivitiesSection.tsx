import { useActivities } from '../../hooks/backoffice/useActivities'
import { SectionCard, CardHeader } from './SectionCard'
import { Skeleton } from './Skeleton'
import { CirclePlay } from 'lucide-react'
import { ACTIVITY_ICONS, ACTIVITY_THEMES } from './activities/activityUiMetadata'
import { ActivityType } from '../../api/activities'

export function ActivitiesSection() {
  const { popularity, loading } = useActivities()

  const popularityList = Array.isArray(popularity) 
    ? popularity.filter(item => item && item.activityType) 
    : []
  const totalSessions = popularityList.reduce((acc, curr) => acc + (curr.totalSessions ?? 0), 0)
  const maxSessions = popularityList.length > 0 ? Math.max(...popularityList.map(m => m.totalSessions ?? 0), 1) : 1

  return (
    <SectionCard>
      <CardHeader
        title="Popularidad por Sesiones"
        subtitle="Total de sesiones iniciadas por actividad"
      />
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4 mt-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Skeleton className="h-7 w-7 rounded-lg" />
                  <Skeleton className="h-3 w-24" />
                </div>
                <Skeleton className="h-3 w-16" />
              </div>
              <Skeleton className="h-1.5 w-full rounded-full" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4 mt-2">
          {[...popularityList]
            .sort((a, b) => (b.totalSessions ?? 0) - (a.totalSessions ?? 0))
            .map((item, index) => {
              const type = item.activityType as ActivityType
              const Icon = ACTIVITY_ICONS[type] || CirclePlay
              const theme = ACTIVITY_THEMES[type] || {
                bg: 'bg-gray-100 dark:bg-gray-800/40',
                text: 'text-gray-600 dark:text-gray-400',
                bar: 'bg-gray-400',
                bullet: 'bg-gray-500',
              }
              const total = item.totalSessions ?? 0
              const barWidthPercentage = maxSessions > 0 ? Math.round((total * 100) / maxSessions) : 0
              const sharePercentage = totalSessions > 0 ? Math.round((total * 100) / totalSessions) : 0

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
                        <span className="rounded-md bg-teal-500/10 px-1.5 py-0.5 text-[9px] font-black tracking-widest text-teal-400 uppercase">
                          TOP
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-2 font-mono text-gray-400 text-[11px]">
                      <span>{item.totalSessions.toLocaleString('es-ES')}</span>
                      <span className="text-gray-500 font-normal">|</span>
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
      )}
    </SectionCard>
  )
}
