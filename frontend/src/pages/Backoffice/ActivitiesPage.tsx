import { BarChart3, Settings } from 'lucide-react'
import { useAdminActivities } from '../../components/backoffice/activities/useAdminActivities'
import ActivityConfigModal from '../../components/backoffice/ActivityConfigModal'
import { ActivityType, ACTIVITY_ICONS } from '../../api/activities'

// Import extracted tab components
import ActivitiesMetricsTab from '../../components/backoffice/activities/ActivitiesMetricsTab'
import ActivitiesConfigTab from '../../components/backoffice/activities/ActivitiesConfigTab'

const ACTIVITY_THEMES: Record<string, { bg: string; text: string; bar: string; bullet: string }> = {
  [ActivityType.BREATHING]: {
    bg: 'bg-teal-500/10 text-teal-600 dark:text-teal-400',
    text: 'text-teal-600 dark:text-teal-400',
    bar: 'bg-gradient-to-r from-teal-500/40 to-teal-400',
    bullet: 'bg-teal-500 dark:bg-teal-400',
  },
  [ActivityType.DIARY]: {
    bg: 'bg-orange-500/10 text-orange-600 dark:text-orange-400',
    text: 'text-orange-600 dark:text-orange-400',
    bar: 'bg-gradient-to-r from-orange-500/40 to-orange-400',
    bullet: 'bg-orange-500 dark:bg-orange-400',
  },
  [ActivityType.LANTERN]: {
    bg: 'bg-yellow-500/10 text-amber-600 dark:text-yellow-400',
    text: 'text-amber-600 dark:text-yellow-400',
    bar: 'bg-gradient-to-r from-yellow-500/40 to-yellow-400',
    bullet: 'bg-amber-500 dark:bg-yellow-400',
  },
  [ActivityType.BUBBLE]: {
    bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400',
    text: 'text-emerald-600 dark:text-emerald-400',
    bar: 'bg-gradient-to-r from-emerald-500/40 to-emerald-400',
    bullet: 'bg-emerald-500 dark:bg-emerald-400',
  },
  [ActivityType.CHALLENGE]: {
    bg: 'bg-rose-500/10 text-rose-600 dark:text-rose-400',
    text: 'text-rose-600 dark:text-rose-400',
    bar: 'bg-gradient-to-r from-rose-500/40 to-rose-400',
    bullet: 'bg-rose-500 dark:bg-rose-400',
  },
  [ActivityType.ZEN_GARDEN]: {
    bg: 'bg-cyan-500/10 text-cyan-600 dark:text-cyan-400',
    text: 'text-cyan-600 dark:text-cyan-400',
    bar: 'bg-gradient-to-r from-cyan-500/40 to-cyan-400',
    bullet: 'bg-cyan-500 dark:bg-cyan-400',
  },
  [ActivityType.MANDALA]: {
    bg: 'bg-violeta/10 text-violeta dark:text-violeta-claro',
    text: 'text-violeta dark:text-violeta-claro',
    bar: 'bg-gradient-to-r from-violeta/40 to-violeta-claro',
    bullet: 'bg-violeta dark:bg-violeta-claro',
  },
}

export default function ActivitiesPage() {
  const {
    activeTab,
    setActiveTab,
    timeframe,
    setTimeframe,
    configs,
    kpis,
    popularity,
    correlation,
    impact,
    loading,
    editingConfig,
    setEditingConfig,
    saveLoading,
    handleEditClick,
    handleUpdateConfigSubmit,
  } = useAdminActivities()

  return (
    <div className="flex flex-col gap-6 text-gray-800 dark:text-gray-100 animate-fadeIn h-[calc(100vh-160px)] min-h-0">
      {/* Header */}
      <div className="flex flex-col gap-1 shrink-0">
        <h1 className="text-[30px] font-extrabold leading-tight text-violeta dark:text-violeta-claro">Actividades</h1>
        <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">
          Monitorea el rendimiento e interactividad de las actividades — ajusta sus disparadores VAD
        </p>
      </div>

      {/* Tabs and Timeframe selector */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 shrink-0">
        <div className="flex gap-2 rounded-2xl bg-white/40 dark:bg-gray-900/20 p-1 self-start shadow-sm border border-gray-100 dark:border-gray-800/40">
          <button
            onClick={() => setActiveTab('metrics')}
            className={`flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-bold transition-all ${
              activeTab === 'metrics'
                ? 'bg-violeta text-white shadow-sm'
                : 'text-gray-600 hover:bg-[#D1CAEF]/20 dark:text-gray-300 dark:hover:bg-gray-800/30'
            }`}
          >
            <BarChart3 className="h-4 w-4" />
            Rendimiento y Métricas
          </button>
          <button
            onClick={() => setActiveTab('config')}
            className={`flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-bold transition-all ${
              activeTab === 'config'
                ? 'bg-violeta text-white shadow-sm'
                : 'text-gray-600 hover:bg-[#D1CAEF]/20 dark:text-gray-300 dark:hover:bg-gray-800/30'
            }`}
          >
            <Settings className="h-4 w-4" />
            Configuración Dinámica
          </button>
        </div>

        {activeTab === 'metrics' && (
          <div className="flex items-center gap-2 self-start sm:self-auto">
            <span className="text-xs font-bold text-gray-400 dark:text-gray-555 uppercase tracking-wider">Período:</span>
            <select
              value={timeframe}
              onChange={(e) => setTimeframe(e.target.value as 'total' | 'month' | 'week' | 'today')}
              className="rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-[#09111f] py-1.5 px-3 text-xs font-bold text-gray-600 dark:text-gray-300 outline-none cursor-pointer focus:border-violeta dark:focus:border-violeta-claro disabled:opacity-50"
              disabled={loading}
            >
              <option value="total">Histórico total</option>
              <option value="month">Último mes</option>
              <option value="week">Última semana</option>
              <option value="today">Hoy</option>
            </select>
          </div>
        )}
      </div>

      {loading ? (
        <div className="flex h-64 items-center justify-center shrink-0">
          <span className="text-gray-500 text-sm font-semibold animate-pulse">Cargando datos...</span>
        </div>
      ) : activeTab === 'metrics' ? (
        <div className="flex-1 min-h-0 overflow-y-auto pr-1">
          <ActivitiesMetricsTab
            kpis={kpis}
            popularity={popularity}
            correlation={correlation}
            impact={impact}
            configs={configs}
            activityIcons={ACTIVITY_ICONS}
            activityThemes={ACTIVITY_THEMES}
          />
        </div>
      ) : (
        <ActivitiesConfigTab
          configs={configs}
          activityIcons={ACTIVITY_ICONS}
          activityThemes={ACTIVITY_THEMES}
          handleEditClick={handleEditClick}
        />
      )}

      {/* CONFIGURATION EDIT MODAL WITH SLIDERS */}
      <ActivityConfigModal
        isOpen={!!editingConfig}
        onClose={() => setEditingConfig(null)}
        config={editingConfig}
        onSave={handleUpdateConfigSubmit}
        saveLoading={saveLoading}
      />
    </div>
  )
}
