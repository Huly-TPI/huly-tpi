import { ActivityType, ACTIVITY_METADATA } from '../../../types/ai'
import { TrendingUp, Calendar } from 'lucide-react'
import { UserActivitiesResponse } from '../../../api/admin'

interface UsageTabProps {
  activities: UserActivitiesResponse | null
  activitiesLoading: boolean
  activitiesError: string | null
  activityTimeframe: 'total' | 'month' | 'week' | 'today'
  setActivityTimeframe: (timeframe: 'total' | 'month' | 'week' | 'today') => void
}

export function UsageTab({
  activities,
  activitiesLoading,
  activitiesError,
  activityTimeframe,
  setActivityTimeframe,
}: UsageTabProps) {
  if (activitiesLoading && !activities) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-violeta border-t-transparent"></div>
        <p className="text-xs text-gray-550 dark:text-gray-400 font-bold">Cargando actividades...</p>
      </div>
    )
  }

  if (activitiesError) {
    return (
      <div className="py-12 text-center text-sm text-red-500 font-semibold flex items-center justify-center">
        {activitiesError}
      </div>
    )
  }

  if (!activities) return null

  const sessions = activities.activitySessions
  const todayActivities = activities.todayActivitiesCount
  const favoriteActivity = activities.favoriteActivity as ActivityType | null
  const frequencyText = activities.averageSessionsText
  const distribution = activities.activityDistribution

  const counts: Record<ActivityType, number> = {
    [ActivityType.RESPIRACION]: distribution['RESPIRACION'] || 0,
    [ActivityType.DIARIO]: distribution['DIARIO'] || 0,
    [ActivityType.NUBE]: distribution['NUBE'] || 0,
    [ActivityType.BURBUJA]: distribution['BURBUJA'] || 0,
    [ActivityType.RETO]: distribution['RETO'] || 0,
  }
  const maxCount = Math.max(...Object.values(counts), 1)

  return (
    <div className="flex flex-col gap-6 animate-fadeIn">
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 shrink-0">
        <div className="bg-gradient-to-br from-violeta/10 to-violeta-claro/5 dark:from-[#2A233C]/20 dark:to-transparent border border-violeta/20 rounded-xl p-4 flex flex-col justify-between">
          <span className="text-xs font-bold text-gray-400 dark:text-gray-555">Sesiones de hoy</span>
          <span className="text-3xl font-black text-violeta dark:text-violeta-claro mt-2">{todayActivities}</span>
        </div>
        <div className={`bg-gradient-to-br from-emerald-500/10 to-emerald-500/5 dark:from-emerald-950/20 dark:to-transparent border border-emerald-500/20 rounded-xl p-4 flex flex-col justify-between transition-opacity duration-200 ${activitiesLoading ? 'opacity-40 pointer-events-none' : ''}`}>
          <span className="text-xs font-bold text-gray-400 dark:text-gray-555">Módulo favorito</span>
          <span className="text-lg font-extrabold text-emerald-600 dark:text-emerald-400 mt-2 truncate">
            {favoriteActivity && ACTIVITY_METADATA[favoriteActivity] ? ACTIVITY_METADATA[favoriteActivity].title : 'Ninguno'}
          </span>
        </div>
        <div className={`bg-gradient-to-br from-blue-500/10 to-blue-500/5 dark:from-blue-955/20 dark:to-transparent border border-blue-500/20 rounded-xl p-4 flex flex-col justify-between transition-opacity duration-200 ${activitiesLoading ? 'opacity-40 pointer-events-none' : ''}`}>
          <span className="text-xs font-bold text-gray-400 dark:text-gray-555">Promedio de sesiones</span>
          {(() => {
            const spaceIndex = frequencyText.indexOf(' ')
            if (spaceIndex !== -1) {
              const numberPart = frequencyText.substring(0, spaceIndex)
              const labelPart = frequencyText.substring(spaceIndex + 1)
              if (/^[0-9.]+$/.test(numberPart)) {
                return (
                  <div className="flex items-baseline gap-1 mt-2">
                    <span className="text-3xl font-black text-blue-600 dark:text-blue-400">{numberPart}</span>
                    <span className="text-xs font-bold text-gray-400 dark:text-gray-550">{labelPart}</span>
                  </div>
                )
              }
            }
            return (
              <span className="text-lg font-extrabold text-blue-600 dark:text-blue-400 mt-2 truncate">
                {frequencyText}
              </span>
            )
          })()}
        </div>
      </div>

      <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-5 shrink-0">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-4 pb-1">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
              <TrendingUp className="h-5 w-5" strokeWidth={1.8} />
            </div>
            <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Distribución de Uso de Funcionalidades</h2>
          </div>
          <div className="flex items-center gap-2">
            {activitiesLoading && (
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-violeta border-t-transparent" />
            )}
            <select
              value={activityTimeframe}
              onChange={(e) => setActivityTimeframe(e.target.value as 'total' | 'month' | 'week' | 'today')}
              className="rounded-lg border border-gray-200 dark:border-gray-800 bg-gray-55 dark:bg-[#09111f] py-1 px-2.5 text-xs font-bold text-gray-600 dark:text-gray-300 outline-none cursor-pointer focus:border-violeta dark:focus:border-violeta-claro disabled:opacity-50"
              disabled={activitiesLoading}
            >
              <option value="total">Histórico total</option>
              <option value="month">Último mes</option>
              <option value="week">Última semana</option>
              <option value="today">Hoy</option>
            </select>
          </div>
        </div>
        <div className={`space-y-4 transition-opacity duration-200 ${activitiesLoading ? 'opacity-40 pointer-events-none' : ''}`}>
          {(Object.entries(counts) as [ActivityType, number][]).map(([key, count]) => {
            const pct = (count / maxCount) * 100
            const metadata = ACTIVITY_METADATA[key]
            return (
              <div key={key} className="space-y-1">
                <div className="flex justify-between text-xs font-bold text-gray-600 dark:text-gray-400">
                  <span>{metadata.title}</span>
                  <span className="text-gray-800 dark:text-gray-200">{count} veces</span>
                </div>
                <div className="w-full h-2.5 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                  <div
                    className={`h-full bg-gradient-to-r ${metadata.color} rounded-full transition-all duration-500`}
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </div>
            )
          })}
        </div>
      </div>

      <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-5">
        <div className="flex items-center gap-2 mb-4">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
            <Calendar className="h-5 w-5" strokeWidth={1.8} />
          </div>
          <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Historial de uso reciente</h2>
        </div>
        {sessions.length === 0 ? (
          <div className="py-8 text-center text-xs text-gray-400">No hay sesiones de actividad registradas.</div>
        ) : (
          <div className={`space-y-3 pr-1 transition-opacity duration-200 ${activitiesLoading ? 'opacity-40 pointer-events-none' : ''}`}>
            {sessions.map((s) => (
              <div key={s.id} className="flex justify-between items-center bg-white dark:bg-[#172033] px-4 py-2.5 rounded-xl border border-gray-100 dark:border-gray-800/60 shadow-sm text-xs animate-fadeIn">
                <div className="flex items-center gap-2">
                  <span className="h-2 w-2 rounded-full bg-violeta animate-pulse" />
                  <span className="font-extrabold text-gray-700 dark:text-gray-300">
                    {ACTIVITY_METADATA[s.activityType as ActivityType]?.title || s.activityType}
                  </span>
                </div>
                <span className="text-gray-400 dark:text-gray-550 font-medium">
                  {new Date(s.createdAt).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' })}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
