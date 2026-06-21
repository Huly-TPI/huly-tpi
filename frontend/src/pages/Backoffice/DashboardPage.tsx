import { useEffect, useState } from 'react'
import { Check, Smartphone, Users } from 'lucide-react'
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
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-[#8869AC] to-[#D1CAEF] p-6 text-white shadow-sm transition duration-300 hover:shadow-md">
        <div className="relative z-10">
          <h2 className="text-2xl font-extrabold md:text-3xl">¡Bienvenido al Panel de Control!</h2>
          <p className="mt-1 text-sm opacity-90">
            Aquí tienes un resumen general del estado del sistema, bienestar de los usuarios y configuraciones clave.
          </p>
        </div>
        <div className="pointer-events-none absolute bottom-0 right-0 top-0 w-1/3 opacity-15">
          <div className="absolute -bottom-8 -right-8 h-32 w-32 rounded-full bg-white blur-xl" />
          <div className="absolute -top-8 -right-4 h-24 w-24 rounded-full bg-white blur-lg" />
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <SectionCard className="border-l-4 border-l-violeta bg-white transition duration-200 hover:translate-y-[-2px] dark:bg-[#172033]">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-violeta-claro/30 text-violeta">
              <Users className="h-6 w-6" strokeWidth={2} />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">Usuarios Registrados</p>
              <h3 className="mt-0.5 text-2xl font-extrabold text-gray-800 dark:text-gray-100">
                {loading ? '...' : stats?.totalUsersCount ?? 0}
              </h3>
            </div>
          </div>
        </SectionCard>

        <SectionCard className="border-l-4 border-l-bosque bg-white transition duration-200 hover:translate-y-[-2px] dark:bg-[#172033]">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-menta/30 text-bosque">
              <Smartphone className="h-6 w-6" strokeWidth={2} />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">Extensión Activa</p>
              <h3 className="mt-0.5 text-2xl font-extrabold text-gray-800 dark:text-gray-100">
                {loading ? '...' : stats?.activeExtensionUsersCount ?? 0}
              </h3>
            </div>
          </div>
        </SectionCard>

        <SectionCard className="border-l-4 border-l-anaranjado bg-white transition duration-200 hover:translate-y-[-2px] dark:bg-[#172033]">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-anaranjado/20 text-anaranjado">
              <Check className="h-6 w-6" strokeWidth={2} />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">Comparten Datos</p>
              <h3 className="mt-0.5 text-2xl font-extrabold text-gray-800 dark:text-gray-100">
                {loading ? '...' : stats?.dataSharingConsentUsersCount ?? 0}
              </h3>
            </div>
          </div>
        </SectionCard>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <WellbeingSection />
        <ActivitiesSection />
      </div>
    </div>
  )
}
