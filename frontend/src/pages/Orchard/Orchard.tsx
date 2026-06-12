import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useUserPlants } from '../../hooks/useUserPlants'
import { type UserPlantSummaryResponse } from '../../api/userGoals'
import OrchardPlantCard from '../../components/Orchard/OrchardPlantCard'
import PlantGoalsModal from '../../components/Orchard/PlantGoalsModal'
import BackButton from '../../components/Buttons/BackButton/BackButton'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import dayBackground from '../../assets/shared/day-background.webp'
import nightBackground from '../../assets/shared/dark-background.webp'
import { useTheme } from '../../context/theme'

export default function Orchard() {
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const navigate = useNavigate()
  const { plants, loading, error } = useUserPlants()
  const [selectedPlant, setSelectedPlant] = useState<UserPlantSummaryResponse | null>(null)

  const growingPlant = plants.find(p => p.status === 'GROWING') ?? null
  const completedPlants = plants.filter(p => p.status === 'COMPLETED')

  const handleCardClick = (plant: UserPlantSummaryResponse) => {
    if (plant.status === 'GROWING') {
      navigate('/challenges')
    } else {
      setSelectedPlant(plant)
    }
  }

  return (
    <div className="relative min-h-screen flex flex-col">
      <ThemeBackground
        lightSrc={dayBackground}
        darkSrc={nightBackground}
        lightAlt="Fondo huerta"
        darkAlt="Fondo nocturno huerta"
      />
      <BackButton to="/challenges" />

      <main className="relative z-10 flex flex-col items-center pt-24 pb-12 px-4 min-h-screen">
        <h1 className={`text-3xl font-extrabold mb-2 tracking-tight ${isDark ? 'text-white' : 'text-bosque'}`}>
          Mi Huerta
        </h1>
        <p className={`text-sm mb-8 ${isDark ? 'text-white/70' : 'text-bosque/70'}`}>
          Tus plantas cultivadas a través de retos completados
        </p>

        {loading && (
          <p className={`text-sm italic ${isDark ? 'text-white/60' : 'text-bosque/60'}`}>
            Cargando tu huerta…
          </p>
        )}

        {error && !loading && (
          <p className="text-sm text-red-600 bg-red-50 rounded-xl px-4 py-3">
            {error}
          </p>
        )}

        {!loading && !error && plants.length === 0 && (
          <div className="flex flex-col items-center gap-3 mt-8">
            <p className={`text-sm italic ${isDark ? 'text-white/70' : 'text-bosque/70'}`}>
              Tu huerta está vacía todavía.
            </p>
            <button
              onClick={() => navigate('/challenges')}
              className="text-sm text-bosque underline underline-offset-2 bg-transparent border-0 cursor-pointer hover:opacity-70 transition-opacity"
            >
              Ir a Retos para empezar
            </button>
          </div>
        )}

        {!loading && !error && plants.length > 0 && (
          <div className="w-full max-w-4xl">
            {growingPlant && (
              <section className="mb-8">
                <h2 className={`text-base font-bold mb-3 ${isDark ? 'text-white/80' : 'text-bosque/80'}`}>
                  Planta en curso
                </h2>
                <div className="max-w-[160px]">
                  <OrchardPlantCard
                    plant={growingPlant}
                    onClick={() => handleCardClick(growingPlant)}
                  />
                </div>
              </section>
            )}

            {completedPlants.length > 0 && (
              <section>
                <h2 className={`text-base font-bold mb-3 ${isDark ? 'text-white/80' : 'text-bosque/80'}`}>
                  Plantas cosechadas
                </h2>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                  {completedPlants.map(plant => (
                    <OrchardPlantCard
                      key={plant.id}
                      plant={plant}
                      onClick={() => handleCardClick(plant)}
                    />
                  ))}
                </div>
              </section>
            )}
          </div>
        )}
      </main>

      {selectedPlant && (
        <PlantGoalsModal
          plant={selectedPlant}
          onClose={() => setSelectedPlant(null)}
        />
      )}
    </div>
  )
}
