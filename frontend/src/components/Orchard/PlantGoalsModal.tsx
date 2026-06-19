import { useState, useEffect } from 'react'
import { userPlantsApi } from '../../api/userPlants'
import { type UserGoalResponse, type UserPlantSummaryResponse } from '../../api/userGoals'

interface PlantGoalsModalProps {
  plant: UserPlantSummaryResponse
  onClose: () => void
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('es-AR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

export default function PlantGoalsModal({ plant, onClose }: PlantGoalsModalProps) {
  const [goals, setGoals] = useState<UserGoalResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    userPlantsApi.getGoals(plant.id)
      .then(res => setGoals(res.goals))
      .catch(() => setGoals([]))
      .finally(() => setLoading(false))
  }, [plant.id])

  return (
    <div
      className="fixed inset-0 bg-black/[0.42] flex items-center justify-center z-[1000] p-4 backdrop-blur-[3px]"
      role="dialog"
      aria-modal="true"
      aria-label={`Retos de la Planta #${plant.plantNumber}`}
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="bg-white rounded-3xl p-6 w-full max-w-[360px] shadow-[0_24px_64px_rgba(0,0,0,0.28)] flex flex-col gap-4 max-h-[80vh]">
        <div className="flex items-start justify-between gap-2">
          <div>
            <h2 className="text-[1.1rem] font-extrabold text-[#2D3748] leading-snug">
              Planta #{plant.plantNumber}
            </h2>
            {plant.completedAt && (
              <p className="text-xs text-gray-400 mt-0.5">
                Cosechada el {formatDate(plant.completedAt)}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 bg-transparent border-0 cursor-pointer text-xl p-0 leading-none flex-shrink-0 mt-0.5"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>

        {loading ? (
          <p className="text-sm text-gray-500 italic text-center py-4">Cargando retos…</p>
        ) : goals.length === 0 ? (
          <p className="text-sm text-gray-500 italic text-center py-4">Sin retos registrados para esta planta.</p>
        ) : (
          <ul className="list-none p-0 m-0 flex flex-col gap-2 overflow-y-auto flex-1">
            {goals.map((goal, i) => (
              <li key={goal.id} className="flex flex-col gap-1 bg-green-50 rounded-xl px-3 py-2">
                <div className="flex items-start gap-2">
                  <span className="text-green-600 font-bold text-sm flex-shrink-0">{i + 1}.</span>
                  <span className="text-sm text-gray-700 leading-snug font-medium">{goal.title}</span>
                </div>
                {goal.description && (
                  <p className="text-xs text-gray-500 ml-5 leading-relaxed italic">{goal.description}</p>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
