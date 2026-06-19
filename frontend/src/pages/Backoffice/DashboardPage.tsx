import { useEffect, useState } from 'react'
import { getAntiScrollDashboard, AntiScrollDashboardResponse } from '../../api/admin'
import { WellbeingSection } from '../../components/backoffice/WellbeingSection'
import { ActivitiesSection } from '../../components/backoffice/ActivitiesSection'
import { SectionCard } from '../../components/backoffice/SectionCard'

export default function DashboardPage() {
  const [stats, setStats] = useState<AntiScrollDashboardResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getAntiScrollDashboard()
      .then((data) => setStats(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="flex flex-col gap-6">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-[#8869AC] to-[#D1CAEF] p-6 text-white shadow-sm transition duration-300 hover:shadow-md">
        <div className="relative z-10">
          <h2 className="text-2xl font-extrabold md:text-3xl">¡Bienvenido al Panel de Control!</h2>
          <p className="mt-1 text-sm opacity-90">
            Aquí tienes un resumen general del estado del sistema, bienestar de los usuarios y configuraciones clave.
          </p>
        </div>
        <div className="absolute right-0 bottom-0 top-0 w-1/3 opacity-15 pointer-events-none">
          <div className="absolute -bottom-8 -right-8 h-32 w-32 rounded-full bg-white blur-xl" />
          <div className="absolute -top-8 -right-4 h-24 w-24 rounded-full bg-white blur-lg" />
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <SectionCard className="bg-white dark:bg-[#172033] border-l-4 border-l-violeta hover:translate-y-[-2px] transition duration-200">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-violeta-claro/30 text-violeta">
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            </div>
            <div>
              <p className="text-xs font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wider">Usuarios Registrados</p>
              <h3 className="text-2xl font-extrabold text-gray-800 dark:text-gray-100 mt-0.5">
                {loading ? '...' : stats?.totalUsersCount ?? 0}
              </h3>
            </div>
          </div>
        </SectionCard>

        <SectionCard className="bg-white dark:bg-[#172033] border-l-4 border-l-bosque hover:translate-y-[-2px] transition duration-200">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-menta/30 text-bosque">
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <rect x="5" y="2" width="14" height="20" rx="3" />
              </svg>
            </div>
            <div>
              <p className="text-xs font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wider">Extensión Activa</p>
              <h3 className="text-2xl font-extrabold text-gray-800 dark:text-gray-100 mt-0.5">
                {loading ? '...' : stats?.activeExtensionUsersCount ?? 0}
              </h3>
            </div>
          </div>
        </SectionCard>

        <SectionCard className="bg-white dark:bg-[#172033] border-l-4 border-l-anaranjado hover:translate-y-[-2px] transition duration-200">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-anaranjado/20 text-anaranjado">
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4" />
              </svg>
            </div>
            <div>
              <p className="text-xs font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wider">Comparten Datos</p>
              <h3 className="text-2xl font-extrabold text-gray-800 dark:text-gray-100 mt-0.5">
                {loading ? '...' : stats?.dataSharingConsentUsersCount ?? 0}
              </h3>
            </div>
          </div>
        </SectionCard>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <WellbeingSection />
        <ActivitiesSection />
      </div>
    </div>
  )
}
