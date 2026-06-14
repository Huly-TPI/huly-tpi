import { useUsers } from '../../hooks/backoffice/useUsers'
import { UserResponse } from '../../api/admin'
import { SectionCard } from '../../components/backoffice/SectionCard'
import { Table, Column } from '../../components/backoffice/Table'
import DomainFavicon from '../../components/backoffice/DomainFavicon'
import { formatConsumptionTime } from '../../utils/date'
import { ChevronLeft, Search, Eye, HelpCircle, ShieldAlert } from 'lucide-react'

export default function UsersPage() {
  const {
    search,
    setSearch,
    loading,
    error,
    selectedWeek,
    setSelectedWeek,
    selectedDay,
    setSelectedDay,
    filteredUsers,
    selectedUser,
    DAYS,
    getDailyTime,
    filteredTotalTime,
    domainList,
    maxDomainTime,
    hasUsageData,
    handleBack,
    navigate,
  } = useUsers()

  if (selectedUser) {
    return (
      <div className="flex flex-col gap-4 animate-fadeIn">
        <div className="flex flex-col gap-0.5">
          <div className="flex items-center gap-2">
            <button
              onClick={handleBack}
              className="text-gray-400 dark:text-gray-500 hover:text-violeta dark:hover:text-[#A78BFA] transition duration-150 flex items-center justify-center p-0.5"
              aria-label="volver"
            >
              <ChevronLeft className="h-5 w-5" strokeWidth={2.5} />
            </button>
            <h1 className="text-[30px] font-extrabold leading-tight text-violeta dark:text-[#A78BFA]">Detalle de usuario</h1>
          </div>
          <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">Ver información general y estadísticas de navegación</p>
        </div>

        <div className="grid grid-cols-1 gap-4 lg:grid-cols-[240px_1fr]">
          <SectionCard className="flex flex-col gap-6 bg-white dark:bg-[#172033] h-fit">
            <div className="flex flex-col items-center text-center">
              <div className="flex h-20 w-20 items-center justify-center rounded-full bg-violeta-claro/30 dark:bg-[#2A233C] text-[32px] font-black text-violeta dark:text-[#A78BFA] mb-3 shadow-inner">
                {selectedUser.name ? selectedUser.name.charAt(0).toUpperCase() : 'u'}
              </div>
              <h2 className="text-xl font-bold text-gray-800 dark:text-gray-100 truncate w-full px-2">{selectedUser.name || 'sin nombre'}</h2>
              <p className="text-sm text-gray-400 dark:text-gray-500 truncate w-full px-2">{selectedUser.email}</p>
            </div>

            <div className="space-y-4 border-t border-gray-50 dark:border-gray-800 pt-4">
              <div className="flex justify-between text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-500">Rol</span>
                <span className="font-bold text-gray-700 dark:text-gray-300">{selectedUser.role}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-500">Estado</span>
                <span className="font-bold text-green-600 dark:text-green-400">{selectedUser.status}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-500">Antiscroll</span>
                <span className={`font-bold ${selectedUser.antiScrollEnabled ? 'text-green-600 dark:text-green-400' : 'text-gray-400 dark:text-gray-500'}`}>
                  {selectedUser.antiScrollEnabled ? 'activo' : 'inactivo'}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="font-semibold text-gray-400 dark:text-gray-500">Compartir datos</span>
                <span className={`font-bold ${selectedUser.dataSharingConsent ? 'text-blue-600 dark:text-blue-400' : 'text-red-500 dark:text-red-400'}`}>
                  {selectedUser.dataSharingConsent ? 'autorizado' : 'denegado'}
                </span>
              </div>
            </div>
          </SectionCard>

          {selectedUser.antiScrollEnabled && (
            <div className="flex flex-col gap-4">
              {selectedUser.dataSharingConsent ? (
                hasUsageData ? (
                  <div className="grid grid-cols-1 gap-4 md:grid-cols-2 max-w-3xl w-full">
                    <SectionCard className="bg-white dark:bg-[#172033] hover:shadow-md transition duration-300">
                      <h3 className="text-base font-bold text-gray-700 dark:text-gray-200 mb-1">Tiempo scrolleando por día</h3>
                      <p className="text-xs text-gray-400 dark:text-gray-500 mb-5">Haz clic en un día para filtrar el tiempo por dominio</p>

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
                                  : 'text-gray-600 dark:text-gray-300 bg-gray-50/50 dark:bg-[#09111f]/50 hover:bg-gray-55 dark:hover:bg-[#09111f] border-gray-100 dark:border-gray-800 hover:border-gray-200 dark:hover:border-gray-700'
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
                    </SectionCard>

                    <SectionCard className="bg-white dark:bg-[#172033] hover:shadow-md transition duration-300 flex flex-col justify-between">
                      <div>
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-4 border-b border-gray-50 dark:border-gray-800 pb-3">
                          <h3 className="text-base font-bold text-gray-700 dark:text-gray-200">Tiempo en cada dominio</h3>
                          
                          <div className="flex gap-2">
                            <select
                              value={selectedWeek}
                              onChange={(e) => setSelectedWeek(e.target.value)}
                              className="rounded-lg border border-gray-200 dark:border-gray-800 bg-gray-50 dark:bg-[#09111f] py-1 px-2.5 text-xs font-bold text-gray-600 dark:text-gray-300 outline-none cursor-pointer focus:border-violeta dark:focus:border-[#A78BFA]"
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
                                        className="shrink-0 shadow-sm bg-gray-50 dark:bg-[#09111f]"
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
                            <div className="py-8 text-center text-sm text-gray-400 dark:text-gray-500">No hay datos de consumo para este día</div>
                          )}
                        </div>
                      </div>

                      <div className="mt-4 border-t border-gray-50 dark:border-gray-800 pt-3 flex justify-between items-center text-xs font-bold text-gray-400 dark:text-gray-500">
                        <span>Total filtrado</span>
                        <span className="text-gray-700 dark:text-gray-200 text-sm font-extrabold">{formatConsumptionTime(filteredTotalTime)}</span>
                      </div>
                    </SectionCard>
                  </div>
                ) : (
                  <SectionCard className="bg-white dark:bg-[#172033] flex flex-col items-center justify-center text-center py-12 px-4 border border-dashed border-gray-200 dark:border-gray-800 max-w-3xl w-full">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-50 dark:bg-blue-955/20 text-blue-500 mb-3">
                      <HelpCircle className="h-6 w-6" strokeWidth={1.8} />
                    </div>
                    <h4 className="text-sm font-bold text-gray-700 dark:text-gray-200 mb-1">Sin estadísticas de uso</h4>
                    <p className="text-xs text-gray-400 dark:text-gray-500 max-w-sm">
                      El usuario ha aprobado compartir sus datos de navegación, pero aún no se registran eventos activos desde la extensión.
                    </p>
                  </SectionCard>
                )
              ) : (
                <SectionCard className="bg-white dark:bg-[#172033] flex flex-col items-center justify-center text-center py-12 px-4 border border-dashed border-gray-200 dark:border-gray-800 max-w-3xl w-full">
                  <div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-50 dark:bg-red-955/20 text-red-500 mb-3">
                    <ShieldAlert className="h-6 w-6" strokeWidth={1.8} />
                  </div>
                  <h4 className="text-sm font-bold text-gray-700 dark:text-gray-200 mb-1">Estadísticas privadas</h4>
                  <p className="text-xs text-gray-400 dark:text-gray-500 max-w-sm">
                    Este usuario no ha autorizado compartir sus estadísticas de bienestar o no tiene iniciada su sesión en la extensión de Chrome.
                  </p>
                </SectionCard>
              )}
            </div>
          )}
        </div>
      </div>
    )
  }

  const userColumns: Column<UserResponse>[] = [
    {
      header: 'Usuario',
      render: (u) => (
        <div>
          <div className="font-semibold text-gray-800 dark:text-gray-200 text-sm">{u.name || 'sin nombre'}</div>
          <div className="text-xs text-gray-400 dark:text-gray-500">{u.email}</div>
        </div>
      ),
    },
    {
      header: 'Antiscroll',
      className: 'text-center',
      render: (u) => (
        <span
          className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold leading-5 ${
            u.antiScrollEnabled
              ? 'bg-green-100 dark:bg-green-950/30 text-green-800 dark:text-green-400'
              : 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300'
          }`}
        >
          {u.antiScrollEnabled ? 'activo' : 'inactivo'}
        </span>
      ),
    },
    {
      header: 'Estadísticas',
      className: 'text-center',
      render: (u) => (
        <span
          className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold leading-5 ${
            u.dataSharingConsent
              ? 'bg-blue-100 dark:bg-blue-955/35 text-blue-800 dark:text-blue-400'
              : 'bg-red-100 dark:bg-red-955/35 text-red-800 dark:text-red-400'
          }`}
        >
          {u.dataSharingConsent ? 'aceptado' : 'denegado'}
        </span>
      ),
    },
    {
      header: 'Acciones',
      className: 'text-center',
      render: (u) => (
        <button
          onClick={() => {
            navigate(`/backoffice/usuarios/${u.id}`)
          }}
          className="inline-flex h-8 w-8 items-center justify-center rounded-lg hover:bg-[#D1CAEF]/30 dark:hover:bg-[#D1CAEF]/10 text-gray-400 dark:text-gray-550 hover:text-[#8869AC] dark:hover:text-[#A78BFA] transition duration-150"
          aria-label={`Ver detalles de ${u.name}`}
        >
          <Eye className="h-5 w-5" strokeWidth={1.8} />
        </button>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4 animate-fadeIn">
      <div className="flex flex-col gap-0.5">
        <h1 className="text-[30px] font-extrabold leading-tight text-[#8869AC] dark:text-[#A78BFA]">Usuarios</h1>
        <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">
          Administra los usuarios registrados en el sistema y visualiza sus consentimientos y configuraciones de extensión.
        </p>
      </div>

      <SectionCard className="bg-white dark:bg-[#172033]">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <h2 className="text-lg font-bold text-gray-700 dark:text-gray-200">Listado de usuarios</h2>
            <div className="relative w-full sm:w-64">
              <input
                type="text"
                placeholder="Buscar por nombre o email..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-55 dark:bg-[#09111f] py-2 pl-4 pr-10 text-sm text-gray-700 dark:text-gray-200 outline-none transition duration-150 focus:border-[#8869AC] dark:focus:border-[#A78BFA] focus:bg-white dark:focus:bg-[#172033]"
              />
              <div className="absolute right-3 top-2.5 text-gray-400 dark:text-gray-500">
                <Search className="h-4 w-4" strokeWidth={1.8} />
              </div>
            </div>
          </div>

          {loading ? (
            <div className="py-8 text-center text-sm text-gray-400 dark:text-gray-500">Cargando usuarios...</div>
          ) : error ? (
            <div className="py-8 text-center text-sm text-red-500">{error}</div>
          ) : filteredUsers.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-400 dark:text-gray-500">No se encontraron usuarios</div>
          ) : (
            <Table data={filteredUsers} columns={userColumns} keyExtractor={(u) => u.id} />
          )}
        </div>
      </SectionCard>
    </div>
  )
}
