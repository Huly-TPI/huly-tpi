import { ActivityType } from '../../../api/activities'
import { AdminActivityCorrelationResponse } from '../../../api/adminActivities'

const HEATMAP_EMOTIONS = [
  'Ansiedad',
  'Estrés',
  'Tristeza',
  'Duelo',
  'Enojo / Rabia',
  'Miedo',
]

interface HeatmapSectionProps {
  correlation: AdminActivityCorrelationResponse[]
  activityIcons: Record<string, any>
  activityThemes: Record<string, any>
}

export default function HeatmapSection({
  correlation,
  activityIcons,
  activityThemes,
}: HeatmapSectionProps) {
  // Find max count for opacity scale
  const maxCount = correlation.length > 0 ? Math.max(...correlation.map((c) => c.suggestionsCount), 1) : 1

  return (
    <div className="flex flex-col gap-5 rounded-3xl border border-gray-100 bg-white dark:bg-[#161F30] dark:border-gray-800/50 p-6 shadow-sm h-full">
      <div>
        <h3 className="text-base font-extrabold text-gray-800 dark:text-white">Correlación Emocional y Aceptación</h3>
        <p className="text-xs text-gray-400 mt-0.5">Qué emociones disparan cada actividad y su porcentaje de aceptación</p>
      </div>

      <div className="flex flex-col gap-3.5 mt-2">
        {/* X-Axis headers */}
        <div className="grid grid-cols-9 gap-1 items-center pl-24">
          {Object.keys(activityIcons).map((actKey) => {
            const Icon = activityIcons[actKey]
            const theme = activityThemes[actKey] || { bg: 'bg-gray-100 text-gray-500' }
            return (
              <div key={actKey} className="flex justify-center" title={actKey}>
                <div className={`flex h-7 w-7 items-center justify-center rounded-lg ${theme.bg}`}>
                  <Icon className="h-[18px] w-[18px]" />
                </div>
              </div>
            )
          })}
        </div>

        {/* Heatmap rows */}
        <div className="flex flex-col gap-3">
          {HEATMAP_EMOTIONS.map((emotionLabel) => {
            return (
              <div key={emotionLabel} className="grid grid-cols-9 gap-1 items-center relative pl-24">
                <div className="absolute left-0 w-24 text-xs font-bold text-gray-450 dark:text-gray-400 truncate">
                  {emotionLabel}
                </div>

                {Object.keys(activityIcons).map((actKey) => {
                  const actType = actKey as ActivityType
                  const theme = activityThemes[actType] || { bullet: 'bg-gray-400' }
                  
                  // Find stats for this cell
                  const cell = correlation.find(
                    (c) => c.activityType === actKey && c.emotion === emotionLabel
                  )
                  const count = cell ? cell.suggestionsCount : 0
                  const rate = cell ? cell.acceptanceRate : 0.0

                  // Calculate opacity based on suggestion count, but color representation can reflect acceptance rate
                  const opacity = count > 0 ? Math.min(1.0, 0.2 + (count * 0.8) / maxCount) : 0.08

                  return (
                    <div key={actKey} className="flex justify-center group/cell">
                      <div
                        className={`h-[22px] w-[22px] rounded-full ${theme.bullet} transition-all duration-300 scale-100 group-hover/cell:scale-115 relative cursor-pointer`}
                        style={{ opacity }}
                      >
                        {/* Interactive tooltip */}
                        <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-1.5 hidden group-hover/cell:flex flex-col items-center z-20">
                          <div className="rounded-xl bg-gray-900 text-white text-[11px] p-2.5 font-semibold whitespace-nowrap shadow-xl border border-gray-800 flex flex-col gap-1 text-center">
                            <span className="font-extrabold text-gray-300">{emotionLabel}</span>
                            <span>{count} recomendaciones</span>
                            {count > 0 && (
                              <span className="font-bold text-teal-400">{rate.toFixed(1)}% aceptadas</span>
                            )}
                          </div>
                          <div className="w-1.5 h-1.5 bg-gray-900 rotate-45 -mt-1"></div>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            )
          })}
        </div>
      </div>

      <div className="flex items-center gap-1.5 justify-end mt-4 text-[11px] font-bold text-gray-400 uppercase tracking-widest">
        <span>Pocas sugerencias</span>
        <div className="flex gap-1 items-center px-1">
          <span className="h-2 w-2 rounded-full bg-violeta-claro opacity-15"></span>
          <span className="h-2.5 w-2.5 rounded-full bg-violeta-claro opacity-40"></span>
          <span className="h-3 w-3 rounded-full bg-violeta-claro opacity-70"></span>
          <span className="h-3.5 w-3.5 rounded-full bg-violeta-claro opacity-100"></span>
        </div>
        <span>Muchas sugerencias</span>
      </div>
    </div>
  )
}
