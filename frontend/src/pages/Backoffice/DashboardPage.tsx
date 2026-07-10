import { useEffect, useState } from 'react'
import { getAdminDashboard, AdminDashboardResponse } from '../../api/admin'
import { WellbeingSection } from '../../components/backoffice/WellbeingSection'
import { ActivitiesSection } from '../../components/backoffice/ActivitiesSection'
import KPICard from '../../components/backoffice/KPICard'

export default function DashboardPage() {
  const [stats, setStats] = useState<AdminDashboardResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getAdminDashboard()
      .then((data) => setStats(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="flex flex-col gap-6">
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-[#8869AC] to-[#D1CAEF] p-6 text-white shadow-sm transition duration-300 hover:shadow-md">
        <div className="relative z-10">
          <h2 className="text-2xl font-extrabold md:text-3xl">¡Bienvenido al Panel de Control!</h2>
          <p className="mt-1 text-sm opacity-90">
            Aquí tienes un resumen general del estado del sistema, bienestar de los usuarios and configuraciones clave.
          </p>
        </div>
        <div className="pointer-events-none absolute bottom-0 right-0 top-0 w-1/3 opacity-15">
          <div className="absolute -bottom-8 -right-8 h-32 w-32 rounded-full bg-white blur-xl" />
          <div className="absolute -top-8 -right-4 h-24 w-24 rounded-full bg-white blur-lg" />
        </div>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
        <KPICard
          title="usuarios registrados esta semana"
          value={loading ? '...' : (stats?.usersRegisteredThisWeek ?? 0)}
          type="violeta"
        />

        <KPICard
          title="Sesiones de actividad esta semana"
          value={loading ? '...' : (stats?.activitiesThisWeek ?? 0)}
          type="emerald"
        />

        <KPICard
          title="usuarios con extensión activa"
          value={loading ? '...' : (stats?.activeExtensionUsersCount ?? 0)}
          type="blue"
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <WellbeingSection />
        <ActivitiesSection />
      </div>
    </div>
  )
}
