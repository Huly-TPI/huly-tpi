import { BarChart3, TrendingUp, CirclePlay } from 'lucide-react'
import KPICard from '../KPICard'
import PopularitySection from './PopularitySection'
import HeatmapSection from './HeatmapSection'
import ImpactSection from './ImpactSection'
import { ActivityType } from '../../../api/activities'
import type { 
  AdminActivitiesKpiResponse, 
  AdminActivityPopularityResponse, 
  AdminActivityCorrelationResponse, 
  AdminActivityImpactResponse, 
  AdminActivityConfig 
} from '../../../api/adminActivities'

interface ActivitiesMetricsTabProps {
  kpis: AdminActivitiesKpiResponse | null
  popularity: AdminActivityPopularityResponse[]
  correlation: AdminActivityCorrelationResponse[]
  impact: AdminActivityImpactResponse[]
  configs: AdminActivityConfig[]
  activityIcons: Record<string, React.ComponentType<any>>
  activityThemes: Record<string, { bg: string; text: string; bar: string; bullet: string }>
}

const ACTIVITY_NAMES_ES: Record<string, string> = {
  BREATHING: 'Respiración guiada',
  DIARY: 'Diario emocional',
  LANTERN: 'Farolitos de papel',
  BUBBLE: 'Burbujas relajantes',
  CHALLENGE: 'Reto diario',
  ZEN_GARDEN: 'Jardín Zen de arena',
  MANDALA: 'Mandalas para colorear',
}

export default function ActivitiesMetricsTab({
  kpis,
  popularity,
  correlation,
  impact,
  configs,
  activityIcons,
  activityThemes,
}: ActivitiesMetricsTabProps) {
  if (!kpis) return null

  return (
    <div className="flex flex-col gap-6">
      {/* Top 3 KPI Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
        <KPICard
          title="Total de Sesiones"
          value={kpis.totalSessions.toLocaleString('es-ES')}
          description="Sesiones activas en todas las actividades"
          type="violeta"
          icon={<BarChart3 className="h-5 w-5" strokeWidth={2} />}
        />

        <KPICard
          title="Actividad Más Popular"
          value={kpis.topActivity.type ? (ACTIVITY_NAMES_ES[kpis.topActivity.type] || kpis.topActivity.type) : 'Sin registros'}
          description={
            kpis.topActivity.type
              ? `${kpis.topActivity.sessions.toLocaleString('es-ES')} sesiones`
              : 'No hay sesiones registradas'
          }
          type="emerald"
          icon={
            kpis.topActivity.type ? (
              (() => {
                const Icon = activityIcons[kpis.topActivity.type as ActivityType] || CirclePlay
                return <Icon className="h-5 w-5" strokeWidth={2} />
              })()
            ) : (
              <CirclePlay className="h-5 w-5" strokeWidth={2} />
            )
          }
        />

        <KPICard
          title="Mejora Emocional Media"
          value={`${kpis.averageMoodImprovement.toFixed(1)}%`}
          description="Sesiones con incremento en valencia"
          type="blue"
          icon={<TrendingUp className="h-5 w-5" strokeWidth={2} />}
        />
      </div>

      {/* Central Row: Popularity (Left) & Heatmap (Right) */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <PopularitySection
          popularity={popularity}
          totalSessions={kpis.totalSessions}
          activityIcons={activityIcons}
          activityThemes={activityThemes}
        />
        <HeatmapSection
          correlation={correlation}
          activityIcons={activityIcons}
          activityThemes={activityThemes}
        />
      </div>

      {/* Bottom Card: Expected Effects / Metrics Transitions */}
      <ImpactSection
        configs={configs}
        impact={impact}
        activityIcons={activityIcons}
        activityThemes={activityThemes}
      />
    </div>
  )
}
