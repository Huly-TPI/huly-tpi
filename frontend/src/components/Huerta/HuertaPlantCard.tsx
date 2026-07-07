import Plant from '../Challenges/Plant'
import { type UserPlantSummaryResponse } from '../../api/userGoals'

function getPlantStage(progress: number, required: number): 0 | 1 | 2 | 3 | 4 | 5 {
  if (progress === 0) return 0
  return Math.min(Math.ceil((progress / required) * 5), 5) as 0 | 1 | 2 | 3 | 4 | 5
}

interface OrchardPlantCardProps {
  plant: UserPlantSummaryResponse
  onClick: () => void
}

export default function OrchardPlantCard({ plant, onClick }: OrchardPlantCardProps) {
  const isGrowing = plant.status === 'GROWING'
  const stage = isGrowing
    ? getPlantStage(plant.completedGoalsCount, plant.requiredGoals)
    : 5

  return (
    <button
      onClick={onClick}
      className="Orchard-plant-card flex flex-col items-center gap-2 bg-white/80 backdrop-blur-sm rounded-2xl p-4 shadow-md hover:shadow-lg hover:-translate-y-1 transition-all duration-200 cursor-pointer border-2 border-transparent hover:border-bosque/30 w-full"
      aria-label={isGrowing ? `Planta ${plant.plantNumber} creciendo, ir a retos` : `Planta ${plant.plantNumber} cosechada, ver retos completados`}
    >
      <div className="scale-[0.55] origin-top -mb-10">
        <Plant stage={stage} isWatering={false} plantType={plant.plantNumber} />
      </div>

      <div className="flex flex-col items-center gap-1 w-full">
        <span className="text-sm font-bold text-bosque">
          Planta #{plant.plantNumber}
        </span>

        {isGrowing ? (
          <>
            <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-medium">
              Creciendo
            </span>
            <span className="text-[0.7rem] text-gray-500 mt-0.5">
              {plant.completedGoalsCount} / {plant.requiredGoals} retos
            </span>
          </>
        ) : (
          <span className="text-xs bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full font-medium">
            Cosechada
          </span>
        )}
      </div>
    </button>
  )
}
