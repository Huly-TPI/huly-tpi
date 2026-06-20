import { Clock, Monitor, HelpCircle, ShieldAlert } from 'lucide-react'
import { useTimeFormatter } from '../../../hooks/useTimeFormatter'
import DomainFavicon from '../../../components/backoffice/DomainFavicon'
import { UserAntiScrollResponse } from '../../../api/admin'

interface AntiScrollTabProps {
  antiscrollStats: UserAntiScrollResponse | null
  antiscrollLoading: boolean
  antiscrollError: string | null
  DAYS: { key: string; label: string }[]
  selectedWeek: string
  setSelectedWeek: (week: string) => void
  selectedDay: string
  setSelectedDay: (day: string) => void
  getDailyTime: (dayKey: string) => number
  domainList: { domain: string; seconds: number }[]
  maxDomainTime: number
  filteredTotalTime: number
  hasUsageData: boolean
}

export function AntiScrollTab({
  antiscrollStats,
  antiscrollLoading,
  antiscrollError,
  DAYS,
  selectedWeek,
  setSelectedWeek,
  selectedDay,
  setSelectedDay,
  getDailyTime,
  domainList,
  maxDomainTime,
  filteredTotalTime,
  hasUsageData,
}: AntiScrollTabProps) {
  const { formatConsumptionTime } = useTimeFormatter()
  if (antiscrollLoading && !antiscrollStats) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-violeta border-t-transparent"></div>
        <p className="text-xs text-gray-550 dark:text-gray-400 font-bold">Cargando estadísticas de Antiscroll...</p>
      </div>
    )
  }

  if (antiscrollError) {
    return (
      <div className="py-12 text-center text-sm text-red-500 font-semibold flex items-center justify-center">
        {antiscrollError}
      </div>
    )
  }

  if (!antiscrollStats) return null

  return (
    <div className="flex flex-col gap-4 animate-fadeIn">
      {antiscrollStats.antiScrollEnabled ? (
        antiscrollStats.dataSharingConsent ? (
          hasUsageData ? (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 w-full">
              <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-4">
                <div className="flex items-center gap-2 mb-2">
                  <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
                    <Clock className="h-5 w-5" strokeWidth={2} />
                  </div>
                  <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Tiempo scrolleando por día</h2>
                </div>
                <p className="text-xs text-gray-400 dark:text-gray-500 mb-4">Haz clic en un día para filtrar el tiempo por dominio</p>

                <div className="space-y-2.5">
                  {DAYS.map((day) => {
                    const dailyTime = getDailyTime(day.key)
                    const isSelected = selectedDay === day.key
                    return (
                      <button
                        key={day.key}
                        onClick={() => setSelectedDay(isSelected ? 'all' : day.key)}
                        className={`flex w-full items-center justify-between rounded-xl px-4 py-2.5 text-sm font-semibold transition-all border ${
                          isSelected
                            ? 'bg-violeta text-white border-violeta shadow-sm'
                            : 'text-gray-600 dark:text-gray-300 bg-gray-50/55 dark:bg-[#09111f]/60 hover:bg-gray-55 dark:hover:bg-[#09111f] border-gray-100 dark:border-gray-800 hover:border-gray-200 dark:hover:border-gray-700'
                        }`}
                      >
                        <span className="flex items-center gap-2">
                          <span className={`h-2.5 w-2.5 rounded-full ${isSelected ? 'bg-white' : 'bg-violeta/55'}`} />
                          {day.label}
                        </span>
                        <span className={isSelected ? 'text-white' : 'text-gray-800 dark:text-gray-200 font-bold'}>
                          {formatConsumptionTime(dailyTime)}
                        </span>
                      </button>
                    )
                  })}
                </div>
              </div>

              <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-4 flex flex-col justify-between">
                <div>
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-4 border-b border-gray-50 dark:border-gray-800/60 pb-3">
                    <div className="flex items-center gap-2">
                      <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
                        <Monitor className="h-5 w-5" strokeWidth={2} />
                      </div>
                      <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Tiempo en cada dominio</h2>
                    </div>
                    <div className="flex gap-2">
                      <select
                        value={selectedWeek}
                        onChange={(e) => setSelectedWeek(e.target.value)}
                        className="rounded-lg border border-gray-200 dark:border-gray-800 bg-gray-55 dark:bg-[#09111f] py-1 px-2.5 text-xs font-bold text-gray-600 dark:text-gray-300 outline-none cursor-pointer focus:border-violeta dark:focus:border-violeta-claro"
                      >
                        <option value="current">Esta semana</option>
                        <option value="previous">Semana anterior</option>
                      </select>
                    </div>
                  </div>

                  <div className="space-y-4">
                    {domainList.length > 0 && domainList.some((d) => d.seconds > 0) ? (
                      domainList.map((app) => {
                        const percentage = (app.seconds / maxDomainTime) * 100
                        return (
                          <div key={app.domain} className="space-y-1">
                            <div className="flex justify-between text-xs font-semibold text-gray-600 dark:text-gray-400">
                              <span className="font-mono flex items-center gap-1.5 truncate">
                                <DomainFavicon
                                  domain={app.domain}
                                  size={14}
                                  className="shrink-0 shadow-sm bg-gray-55 dark:bg-[#09111f]"
                                />
                                {app.domain}
                              </span>
                              <span>{formatConsumptionTime(app.seconds)}</span>
                            </div>
                            <div className="w-full h-2 bg-gray-50 dark:bg-gray-800 rounded-full overflow-hidden">
                              <div
                                className="h-full bg-gradient-to-r from-violeta to-violeta-claro rounded-full"
                                style={{ width: `${percentage}%` }}
                              />
                            </div>
                          </div>
                        )
                      })
                    ) : (
                      <div className="py-8 text-center text-sm text-gray-450 dark:text-gray-500">No hay datos de consumo para este día</div>
                    )}
                  </div>
                </div>

                <div className="mt-4 border-t border-gray-50 dark:border-gray-800/60 pt-3 flex justify-between items-center text-xs font-bold text-gray-400 dark:text-gray-555">
                  <span>Total filtrado</span>
                  <span className="text-gray-700 dark:text-gray-200 text-sm font-extrabold">{formatConsumptionTime(filteredTotalTime)}</span>
                </div>
              </div>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center text-center py-12 px-4 border border-dashed border-gray-200 dark:border-gray-800 rounded-xl w-full">
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-50 dark:bg-blue-955/20 text-blue-500 mb-3">
                <HelpCircle className="h-6 w-6" strokeWidth={2} />
              </div>
              <h4 className="text-sm font-bold text-gray-700 dark:text-gray-200 mb-1">Sin estadísticas de uso</h4>
              <p className="text-xs text-gray-400 dark:text-gray-500 max-w-sm">
                El usuario ha aprobado compartir sus datos de navegación, pero aún no se registran eventos activos desde la extensión.
              </p>
            </div>
          )
        ) : (
          <div className="flex flex-col items-center justify-center text-center py-12 px-4 border border-dashed border-gray-200 dark:border-gray-800 rounded-xl w-full">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-50 dark:bg-red-955/20 text-red-500 mb-3">
              <ShieldAlert className="h-6 w-6" strokeWidth={2} />
            </div>
            <h4 className="text-sm font-bold text-gray-700 dark:text-gray-200 mb-1">Estadísticas privadas</h4>
            <p className="text-xs text-gray-400 dark:text-gray-555 max-w-sm">
              Este usuario no ha autorizado compartir sus estadísticas de bienestar o no tiene iniciada su sesión en la extensión de Chrome.
            </p>
          </div>
        )
      ) : (
        <div className="flex flex-col items-center justify-center text-center py-12 px-4 border border-dashed border-gray-200 dark:border-gray-800 rounded-xl w-full">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-50 dark:bg-gray-800 text-gray-400 dark:text-gray-555 mb-3">
            <ShieldAlert className="h-6 w-6" strokeWidth={2} />
          </div>
          <h4 className="text-sm font-bold text-gray-700 dark:text-gray-200 mb-1">Antiscroll desactivado</h4>
          <p className="text-xs text-gray-400 dark:text-gray-555 max-w-sm">
            Este usuario no tiene activado el módulo de Antiscroll.
          </p>
        </div>
      )}
    </div>
  )
}
