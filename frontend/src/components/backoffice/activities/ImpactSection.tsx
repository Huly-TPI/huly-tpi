import { CirclePlay, ArrowUpRight, ArrowDownRight } from 'lucide-react'
import { AdminActivityConfig, AdminActivityImpactResponse } from '../../../api/adminActivities'
import InfoPopup from '../InfoPopup'

interface ImpactSectionProps {
  configs: AdminActivityConfig[]
  impact: AdminActivityImpactResponse[]
  activityIcons: Record<string, any>
  activityThemes: Record<string, any>
}

export default function ImpactSection({
  configs,
  impact,
  activityIcons,
  activityThemes,
}: ImpactSectionProps) {
  const formatDelta = (val: number) => {
    return val >= 0 ? `+${val.toFixed(2)}` : `${val.toFixed(2)}`
  }

  return (
    <div className="flex flex-col gap-4 rounded-3xl border border-gray-100 bg-white dark:bg-[#161F30] dark:border-gray-800/50 p-6 shadow-sm relative">
      <div className="flex items-start justify-between">
        <div>
          <div className="flex items-center gap-1.5">
            <h3 className="text-base font-extrabold text-gray-800 dark:text-white">Impacto Emocional</h3>
            <InfoPopup title="Rendimiento e Impacto Emocional">
              <p className="text-gray-650 dark:text-gray-300">
                Muestra la variación en el estado anímico del usuario tras realizar cada actividad.
              </p>
              <ul className="space-y-1.5 pl-3 list-disc text-gray-500 dark:text-gray-400">
                <li><strong>✓ (Check verde):</strong> Indica que el impacto fue calculado en base a métricas reales y el posterior seguimiento de eventos del usuario.</li>
                <li><strong>ⓘ (Info gris):</strong> Indica que aún no hay suficientes datos reales de uso, mostrando por defecto los valores base de configuración.</li>
                <li><strong>Calmante (ΔA ≤ 0):</strong> Actividades orientadas a calmar y desacelerar la alerta fisiológica.</li>
                <li><strong>Activante (ΔA &gt; 0):</strong> Actividades orientadas a reactivar la energía y el foco del usuario.</li>
              </ul>
            </InfoPopup>
          </div>
          <p className="text-xs text-gray-400 mt-0.5">Transición del estado anímico antes ➜ después de la actividad</p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-9">
        {configs.map((config) => {
          const Icon = activityIcons[config.type] || CirclePlay
          const theme = activityThemes[config.type] || { bg: 'bg-gray-100 text-gray-500', text: 'text-gray-500' }
          
          // Find stats for this activity
          const item = impact.find((i) => i.activityType === config.type)
          const realValenceChange = item ? item.averageValenceChange : config.effectValence
          const realArousalChange = item ? item.averageArousalChange : config.effectArousal
          const basedOnMetrics = item ? item.basedOnMetrics : false
          const isCalmante = realArousalChange <= 0

          return (
            <div
              key={config.type}
              className="flex flex-col items-center gap-3 p-4 rounded-2xl border border-gray-100 dark:border-gray-800/20 bg-gray-55/20 dark:bg-gray-900/10 text-center relative group"
            >
              {/* Origin Badge */}
              <span className={`absolute top-2 right-2 text-[9px] font-bold ${
                basedOnMetrics ? 'text-teal-400' : 'text-gray-400'
              }`} title={basedOnMetrics ? 'Basado en métricas de uso reales' : 'Valores predefinidos de configuración'}>
                {basedOnMetrics ? '✓' : 'ⓘ'}
              </span>

              <div className={`flex h-8 w-8 items-center justify-center rounded-lg ${theme.bg}`}>
                <Icon className="h-[18px] w-[18px]" />
              </div>

              <span className="text-[11px] font-extrabold tracking-tight text-gray-700 dark:text-gray-300 leading-tight line-clamp-1">
                {config.title}
              </span>

              <div className="w-full flex items-center justify-center gap-1 rounded-xl bg-teal-500/5 border border-teal-500/20 px-2 py-1.5 text-teal-400 font-mono text-xs font-bold" title="Cambio promedio en Valencia">
                <ArrowUpRight className="h-3 w-3 shrink-0" />
                <span>{formatDelta(realValenceChange)} V</span>
              </div>

              <div className={`w-full flex items-center justify-center gap-1 rounded-xl px-2 py-1.5 font-mono text-xs font-bold border ${
                isCalmante
                  ? 'bg-blue-500/5 border-blue-500/20 text-blue-400'
                  : 'bg-orange-500/5 border-orange-500/20 text-orange-400'
              }`} title="Cambio promedio en Arousal">
                {isCalmante ? <ArrowDownRight className="h-3 w-3 shrink-0" /> : <ArrowUpRight className="h-3 w-3 shrink-0" />}
                <span>{formatDelta(realArousalChange)} A</span>
              </div>

              <span className={`rounded-full px-2.5 py-0.5 text-[9px] font-black uppercase tracking-wider ${
                isCalmante
                  ? 'bg-blue-500/10 text-blue-400'
                  : 'bg-orange-500/10 text-orange-400'
              }`}>
                {isCalmante ? 'Calmante' : 'Activante'}
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}
