import { useAntiScrollDashboard } from '../../hooks/backoffice/useAntiScrollDashboard'
import { useTimeFormatter } from '../../hooks/useTimeFormatter'
import { SectionCard } from '../../components/backoffice/SectionCard'
import DomainFavicon from '../../components/backoffice/DomainFavicon'
import { RefreshCw, Monitor, Database, Link2, Settings, Clock, FileText, CheckCircle } from 'lucide-react'
import Button from '../../components/Buttons/Button/Button'

export default function AntiScrollPage() {
  const { formatConsumptionTime } = useTimeFormatter()
  const {
    dashboard,
    loading,
    error,
    defaultTime,
    setDefaultTime,
    terms,
    setTerms,
    configLoading,
    configSaving,
    configError,
    saveSuccess,
    handleSaveConfig,
    totalRedirects,
    totalIgnore,
    redirectRate,
    maxAppTime,
  } = useAntiScrollDashboard()

  if (loading) {
    return (
      <div className="flex h-[400px] items-center justify-center">
        <div className="text-center text-sm text-gray-400 dark:text-gray-500">Cargando datos de antiscroll...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-[400px] items-center justify-center">
        <div className="text-center text-sm text-red-500">{error}</div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6 animate-fadeIn">
      <div className="flex flex-col gap-1">
        <h1 className="text-[30px] font-extrabold leading-tight text-violeta dark:text-violeta-claro">Panel antiscroll</h1>
        <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">
          Monitoreo global de bienestar digital, retorno al jardín y estadísticas de navegación de la extensión.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[360px_1fr] w-full">
        <div className="flex flex-col gap-6">
          <SectionCard className="bg-white dark:bg-[#172033] flex flex-col justify-between hover:shadow-md transition duration-300 p-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
                  <RefreshCw className="h-5 w-5" strokeWidth={1.8} />
                </div>
                <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Tasa de retorno al jardín</h2>
              </div>
              <p className="text-xs text-gray-400 dark:text-gray-500 mb-4">
                Mide cuántos usuarios deciden pausar su navegación e ir a relajarse al jardín terapéutico.
              </p>
            </div>

            <div className="flex flex-col items-center justify-center py-2">
              <div className="relative flex items-center justify-center">
                <svg className="h-28 w-28 transform -rotate-90">
                  <circle
                    cx="56"
                    cy="56"
                    r="48"
                    className="text-gray-100 dark:text-gray-800"
                    strokeWidth="8"
                    stroke="currentColor"
                    fill="transparent"
                  />
                  <circle
                    cx="56"
                    cy="56"
                    r="48"
                    className="text-violeta dark:text-violeta-claro"
                    strokeWidth="8"
                    strokeDasharray={301.6}
                    strokeDashoffset={301.6 - (301.6 * redirectRate) / 100}
                    strokeLinecap="round"
                    stroke="currentColor"
                    fill="transparent"
                  />
                </svg>
                <div className="absolute flex flex-col items-center justify-center text-center">
                  <span className="text-xl font-black text-gray-800 dark:text-gray-100">{redirectRate.toFixed(1)}%</span>
                  <span className="text-[9px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest">Retorno</span>
                </div>
              </div>
            </div>

            <div className="mt-2 border-t border-gray-50 dark:border-gray-855/30 pt-3">
              <div className="grid grid-cols-2 gap-2 text-center text-xs">
                <div className="border-r border-gray-100 dark:border-gray-855/30">
                  <span className="block text-[9px] font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500">Regresan al jardín</span>
                  <span className="text-sm font-extrabold text-violeta dark:text-violeta-claro">{totalRedirects} clicks</span>
                </div>
                <div>
                  <span className="block text-[9px] font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500">Ignoran o continúan</span>
                  <span className="text-sm font-extrabold text-gray-500 dark:text-gray-400">{totalIgnore} clicks</span>
                </div>
              </div>
            </div>
          </SectionCard>

          <SectionCard className="bg-white dark:bg-[#172033] flex flex-col justify-between hover:shadow-md transition duration-300 p-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-menta/30 dark:bg-emerald-955/20 text-bosque dark:text-menta">
                  <Monitor className="h-5 w-5" strokeWidth={1.8} />
                </div>
                <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Usuarios en la extensión</h2>
              </div>
              <p className="text-xs text-gray-400 dark:text-gray-500 mb-4">
                Estado de adopción de la extensión y consentimientos de compartición de datos de bienestar.
              </p>
            </div>

            <div className="flex flex-col gap-3 py-1">
              <div className="flex items-center gap-3 rounded-xl border border-gray-100 dark:border-gray-800 p-3 bg-gray-50/20 dark:bg-[#09111f]/20">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-green-100 dark:bg-green-950/30 text-green-600 dark:text-green-400 shadow-sm">
                  <Monitor className="h-5 w-5" strokeWidth={1.8} />
                </div>
                <div className="flex-1">
                  <span className="block text-[10px] font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wider">Extensión activa ahora</span>
                  <span className="text-lg font-black text-gray-800 dark:text-gray-100">
                    {dashboard?.activeExtensionUsersCount ?? 0} <span className="text-xs font-semibold text-gray-400 dark:text-gray-500">usuarios</span>
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-3 rounded-xl border border-gray-100 dark:border-gray-800 p-3 bg-gray-50/20 dark:bg-[#09111f]/20">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-950/30 text-blue-600 dark:text-blue-400 shadow-sm">
                  <Database className="h-5 w-5" strokeWidth={1.8} />
                </div>
                <div className="flex-1">
                  <span className="block text-[10px] font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wider">Comparten datos de navegación</span>
                  <span className="text-lg font-black text-gray-800 dark:text-gray-100">
                    {dashboard?.dataSharingConsentUsersCount ?? 0} <span className="text-xs font-semibold text-gray-400 dark:text-gray-500">usuarios</span>
                  </span>
                </div>
              </div>
            </div>

            <div className="mt-3 border-t border-gray-50 dark:border-gray-855/30 pt-3 text-center">
              <span className="text-[11px] text-gray-400 dark:text-gray-500 font-medium">
                Porcentaje de adopción de compartición de datos:{' '}
                <span className="font-bold text-gray-700 dark:text-gray-200">
                  {dashboard?.totalUsersCount && dashboard.totalUsersCount > 0
                    ? ((dashboard.dataSharingConsentUsersCount / dashboard.totalUsersCount) * 100).toFixed(0)
                    : 0}%
                </span>
              </span>
            </div>
          </SectionCard>
        </div>

        <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 w-full items-stretch">
          <SectionCard className="bg-white dark:bg-[#172033] hover:shadow-md transition duration-300 p-4 flex flex-col gap-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
                  <Settings className="h-5 w-5" strokeWidth={1.8} />
                </div>
                <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Configuración de la extensión</h2>
              </div>
              <p className="text-xs text-gray-400 dark:text-gray-500 mb-4">
                Personaliza el comportamiento global de la extensión anti-scroll.
              </p>
            </div>

            {configLoading ? (
              <div className="text-center py-6 text-sm text-gray-400 dark:text-gray-500 animate-pulse">Cargando configuración...</div>
            ) : (
              <form onSubmit={handleSaveConfig} className="flex flex-col gap-4 flex-1">
                {configError && (
                  <div className="text-xs text-red-500 bg-red-50 dark:bg-red-955/20 p-2.5 rounded-lg border border-red-100 dark:border-red-900/30">
                    {configError}
                  </div>
                )}
                
                {saveSuccess && (
                  <div className="text-xs text-green-600 dark:text-green-400 bg-green-50 dark:bg-green-955/20 p-2.5 rounded-lg border border-green-100 dark:border-green-900/30 flex items-center gap-1.5">
                    <CheckCircle className="h-4 w-4 shrink-0" />
                    <span>Configuración guardada exitosamente.</span>
                  </div>
                )}

                <div className="flex flex-col gap-1.5">
                  <label htmlFor="default-time-input" className="text-xs font-bold text-gray-700 dark:text-gray-300 flex items-center gap-1.5">
                    <Clock className="h-3.5 w-3.5 text-gray-400 dark:text-gray-500" />
                    <span>Tiempo de pausa por defecto (minutos)</span>
                  </label>
                  <input
                    id="default-time-input"
                    type="number"
                    min={1}
                    value={defaultTime}
                    onChange={(e) => setDefaultTime(parseInt(e.target.value) || 20)}
                    className="w-full text-sm border border-gray-200 dark:border-gray-800 rounded-xl px-3 py-2 bg-gray-50 dark:bg-[#09111f] text-gray-700 dark:text-gray-200 focus:bg-white dark:focus:bg-[#172033] focus:outline-none focus:ring-2 focus:ring-violeta/30 focus:border-violeta dark:focus:border-violeta-claro transition duration-200"
                    required
                  />
                  <span className="text-[10px] text-gray-400 dark:text-gray-500">
                    Intervalo en minutos en el que se sugerirá una pausa al usuario.
                  </span>
                </div>

                <div className="flex flex-col gap-1.5 flex-1">
                  <label htmlFor="terms-input" className="text-xs font-bold text-gray-700 dark:text-gray-300 flex items-center gap-1.5">
                    <FileText className="h-3.5 w-3.5 text-gray-400 dark:text-gray-500" />
                    <span>Mensaje de consentimiento de datos</span>
                  </label>
                  <textarea
                    id="terms-input"
                    value={terms}
                    onChange={(e) => setTerms(e.target.value)}
                    className="w-full text-sm border border-gray-200 dark:border-gray-800 rounded-xl px-3 py-2 bg-gray-50 dark:bg-[#09111f] text-gray-700 dark:text-gray-200 focus:bg-white dark:focus:bg-[#172033] focus:outline-none focus:ring-2 focus:ring-violeta/30 focus:border-violeta dark:focus:border-violeta-claro transition duration-200 min-h-[120px] flex-1 resize-y font-normal leading-relaxed"
                    required
                  />
                  <span className="text-[10px] text-gray-400 dark:text-gray-500">
                    Mensaje que se mostrará en el modal de consentimiento de recopilación de estadísticas.
                  </span>
                </div>

                <Button
                  type="submit"
                  variant="primary"
                  isLoading={configSaving}
                  loadingLabel="Guardando..."
                  fullWidth
                >
                  Guardar configuración
                </Button>
              </form>
            )}
          </SectionCard>

          <SectionCard className="bg-white dark:bg-[#172033] hover:shadow-md transition duration-300 p-4 flex flex-col h-full gap-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
                  <Link2 className="h-5 w-5" strokeWidth={1.8} />
                </div>
                <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Páginas más consumidas</h2>
              </div>
              <p className="text-xs text-gray-400 dark:text-gray-500 mb-4">
                Dominios en los que los usuarios con seguimiento autorizado pasan la mayor cantidad de tiempo scrolleando.
              </p>
            </div>

            <div className="flex flex-col gap-3 flex-1 overflow-y-auto">
              {dashboard?.topUsedApps && dashboard.topUsedApps.length > 0 ? (
                dashboard.topUsedApps.slice(0, 5).map((app, index) => {
                  const percentage = (app.totalActiveSeconds / maxAppTime) * 100
                  return (
                    <div
                      key={app.domain}
                      className="flex flex-col gap-2 rounded-xl border border-gray-100 dark:border-gray-800 p-3 hover:border-purple-200 dark:hover:border-purple-900/30 hover:bg-purple-50/10 dark:hover:bg-purple-950/10 transition duration-200"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <div className="flex items-center gap-3 min-w-0">
                          <DomainFavicon
                            domain={app.domain}
                            size={32}
                            className="bg-gray-50 dark:bg-[#09111f] p-1 shadow-sm rounded-lg"
                          />
                          <span className="font-mono text-sm font-bold text-gray-700 dark:text-gray-200 truncate">{app.domain}</span>
                        </div>
                        <span className="text-xs font-bold text-violeta dark:text-violeta-claro bg-[#D1CAEF]/20 dark:bg-[#2A233C]/40 px-2.5 py-0.5 rounded-full shrink-0">
                          #{index + 1}
                        </span>
                      </div>

                      <div className="space-y-1">
                        <div className="flex justify-between text-[11px] text-gray-500 dark:text-gray-400 font-semibold">
                          <span>Tiempo acumulado</span>
                          <span className="text-gray-800 dark:text-gray-200 font-bold">{formatConsumptionTime(app.totalActiveSeconds)}</span>
                        </div>
                        <div className="w-full h-1.5 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-gradient-to-r from-violeta to-violeta-claro rounded-full"
                            style={{ width: `${percentage}%` }}
                          />
                        </div>
                      </div>
                    </div>
                  )
                })
              ) : (
                <div className="py-12 text-center text-sm text-gray-400 dark:text-gray-500">No hay datos de consumo registrados</div>
              )}
            </div>
          </SectionCard>
        </div>
      </div>
    </div>
  )
}
