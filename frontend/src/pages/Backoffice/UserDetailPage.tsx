import { useUsers } from '../../hooks/backoffice/useUsers'
import { SectionCard } from '../../components/backoffice/SectionCard'
import { ChevronLeft, ShieldAlert, Brain, Activity, DollarSign, Check, X } from 'lucide-react'
import { UsageTab } from './components/UsageTab'
import { AiDiagnosticsTab } from './components/AiDiagnosticsTab'
import { AntiScrollTab } from './components/AntiScrollTab'
import { FinanceTab } from './components/FinanceTab'

export default function UserDetailPage() {
  const {
    selectedUser,
    selectedWeek,
    setSelectedWeek,
    selectedDay,
    setSelectedDay,
    DAYS,
    getDailyTime,
    filteredTotalTime,
    domainList,
    maxDomainTime,
    hasUsageData,
    handleBack,
    activityTimeframe,
    setActivityTimeframe,
    activeTab,
    setActiveTab,
    activities,
    activitiesLoading,
    activitiesError,
    aiDiagnostics,
    aiLoading,
    aiError,
    financials,
    financialsLoading,
    financialsError,
    antiscrollStats,
    antiscrollLoading,
    antiscrollError,
  } = useUsers()

  if (!selectedUser) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-violeta border-t-transparent"></div>
        <p className="text-xs text-gray-550 dark:text-gray-400 font-bold">Cargando información del usuario...</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4 animate-fadeIn lg:h-full h-auto">
      {/* Header */}
      <div className="flex flex-col gap-0.5 shrink-0">
        <div className="flex items-center gap-2">
          <button
            onClick={handleBack}
            className="text-gray-400 dark:text-gray-555 hover:text-violeta dark:hover:text-violeta-claro transition duration-150 flex items-center justify-center p-0.5"
            aria-label="volver"
          >
            <ChevronLeft className="h-5 w-5" strokeWidth={2.5} />
          </button>
          <h1 className="text-[30px] font-extrabold leading-tight text-violeta dark:text-violeta-claro">Detalle de usuario</h1>
        </div>
        <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">Ver información general y estadísticas de navegación</p>
      </div>

      {/* Main Grid: stretches to fill remaining height on lg+ */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[360px_1fr] lg:flex-1 lg:min-h-0">
        
        {/* User Profile Sidebar: 100% Height */}
        <SectionCard className="flex flex-col gap-6 bg-white dark:bg-[#172033] lg:h-full h-auto justify-between shadow-sm border border-gray-100 dark:border-gray-800/40">
          <div className="flex flex-col gap-6 w-full">
            <div className="flex flex-col items-center text-center">
              <div className="flex h-20 w-20 items-center justify-center rounded-full bg-violeta-claro/30 dark:bg-[#2A233C] text-[32px] font-black text-violeta dark:text-violeta-claro mb-3 shadow-inner">
                {selectedUser.name ? selectedUser.name.charAt(0).toUpperCase() : 'u'}
              </div>
              <h2 className="text-xl font-bold text-gray-800 dark:text-gray-100 truncate w-full px-2">
                {selectedUser.name || 'sin nombre'}
              </h2>
              <p className="text-sm text-gray-400 dark:text-gray-500 truncate w-full px-2">
                {selectedUser.email}
              </p>
            </div>

            <div className="space-y-4 border-t border-gray-50 dark:border-gray-800 pt-4">
              <div className="flex justify-between items-center text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-555">Estado</span>
                <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold leading-5 ${
                  selectedUser.status === 'ACTIVE' || selectedUser.status === 'ACTIVO'
                    ? 'bg-green-100 dark:bg-green-950/30 text-green-800 dark:text-green-400'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300'
                }`}>
                  {selectedUser.status === 'ACTIVE' || selectedUser.status === 'ACTIVO' 
                    ? 'Activo' 
                    : selectedUser.status.charAt(0).toUpperCase() + selectedUser.status.slice(1).toLowerCase()}
                </span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-555">Antiscroll</span>
                <span className={`inline-flex items-center font-bold ${selectedUser.antiScrollEnabled ? 'text-green-600 dark:text-green-400' : 'text-red-500 dark:text-red-400'}`}>
                  {selectedUser.antiScrollEnabled ? (
                    <Check className="h-4 w-4 stroke-[3]" />
                  ) : (
                    <X className="h-4 w-4 stroke-[3]" />
                  )}
                </span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-555">Compartir datos (Antiscroll)</span>
                <span className={`inline-flex items-center font-bold ${selectedUser.dataSharingConsent ? 'text-green-600 dark:text-green-400' : 'text-red-500 dark:text-red-400'}`}>
                  {selectedUser.dataSharingConsent ? (
                    <Check className="h-4 w-4 stroke-[3]" />
                  ) : (
                    <X className="h-4 w-4 stroke-[3]" />
                  )}
                </span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-555">Monedas</span>
                <span className="font-bold text-amber-500 dark:text-amber-400">
                  {selectedUser.coins ?? 0}
                </span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-555">Plan</span>
                <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold leading-5 ${
                  selectedUser.plan && selectedUser.plan !== 'Gratuito' && selectedUser.plan !== 'FREE'
                    ? 'bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300'
                }`}>
                  {selectedUser.plan}
                </span>
              </div>
            </div>
          </div>
        </SectionCard>

        {/* Tabbed statistics container: 100% Height */}
        <div className="flex flex-col gap-4 bg-white dark:bg-[#172033] rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-800/40 lg:h-full h-auto lg:min-h-0">
          
          {/* Tabs header: stays fixed at the top */}
          <div className="flex gap-2 border-b border-gray-100 dark:border-gray-800/60 pb-3 overflow-x-auto shrink-0">
            <button
              onClick={() => setActiveTab('usage')}
              className={`px-4 py-2.5 text-xs font-bold rounded-xl transition duration-155 flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'usage'
                  ? 'bg-violeta text-white shadow-sm'
                  : 'text-gray-500 dark:text-gray-400 hover:bg-gray-55 dark:hover:bg-gray-800/50'
              }`}
            >
              <Activity className="h-4 w-4" />
              Uso y Actividades
            </button>
            <button
              onClick={() => setActiveTab('ai')}
              className={`px-4 py-2.5 text-xs font-bold rounded-xl transition duration-155 flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'ai'
                  ? 'bg-violeta text-white shadow-sm'
                  : 'text-gray-500 dark:text-gray-400 hover:bg-gray-55 dark:hover:bg-gray-800/50'
              }`}
            >
              <Brain className="h-4 w-4" />
              IA y Decisiones
            </button>
            <button
              onClick={() => setActiveTab('antiscroll')}
              className={`px-4 py-2.5 text-xs font-bold rounded-xl transition duration-155 flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'antiscroll'
                  ? 'bg-violeta text-white shadow-sm'
                  : 'text-gray-500 dark:text-gray-400 hover:bg-gray-55 dark:hover:bg-gray-800/50'
              }`}
            >
              <ShieldAlert className="h-4 w-4" />
              Antiscroll
            </button>
            <button
              onClick={() => setActiveTab('finance')}
              className={`px-4 py-2.5 text-xs font-bold rounded-xl transition duration-155 flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'finance'
                  ? 'bg-violeta text-white shadow-sm'
                  : 'text-gray-500 dark:text-gray-400 hover:bg-gray-55 dark:hover:bg-gray-800/50'
              }`}
            >
              <DollarSign className="h-4 w-4" />
              Métricas Financieras
            </button>
          </div>

          {/* Tab content panel: expands and scrolls internally */}
          <div className="animate-fadeIn lg:flex-grow lg:min-h-0 lg:overflow-y-auto pr-1 lg:flex lg:flex-col">
            {activeTab === 'antiscroll' && (
              <AntiScrollTab
                antiscrollStats={antiscrollStats}
                antiscrollLoading={antiscrollLoading}
                antiscrollError={antiscrollError}
                DAYS={DAYS}
                selectedWeek={selectedWeek}
                setSelectedWeek={setSelectedWeek}
                selectedDay={selectedDay}
                setSelectedDay={setSelectedDay}
                getDailyTime={getDailyTime}
                domainList={domainList}
                maxDomainTime={maxDomainTime}
                filteredTotalTime={filteredTotalTime}
                hasUsageData={hasUsageData}
              />
            )}

            {activeTab === 'usage' && (
              <UsageTab
                activities={activities}
                activitiesLoading={activitiesLoading}
                activitiesError={activitiesError}
                activityTimeframe={activityTimeframe}
                setActivityTimeframe={setActivityTimeframe}
              />
            )}

            {activeTab === 'ai' && (
              <AiDiagnosticsTab
                aiDiagnostics={aiDiagnostics}
                aiLoading={aiLoading}
                aiError={aiError}
              />
            )}

            {activeTab === 'finance' && (
              <FinanceTab
                financials={financials}
                financialsLoading={financialsLoading}
                financialsError={financialsError}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
