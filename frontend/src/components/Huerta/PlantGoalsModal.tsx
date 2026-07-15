import { useState, useEffect } from 'react'
import { userPlantsApi } from '../../api/userPlants'
import { type UserGoalResponse, type UserPlantSummaryResponse } from '../../api/userGoals'

interface PlantGoalsModalProps {
  plant: UserPlantSummaryResponse
  onClose: () => void
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
      className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={`Retos de la Planta #${plant.plantNumber}`}
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="bg-white rounded-3xl p-6 w-full max-w-[360px] shadow-[0_24px_64px_rgba(0,0,0,0.28)] flex flex-col gap-4 max-h-[80vh]">
        <div className="flex items-center justify-between">
          <h2 className="text-[1.1rem] font-extrabold text-[#2D3748] m-0 leading-snug">
            Planta #{plant.plantNumber} — Retos cosechados
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 bg-transparent border-0 cursor-pointer text-xl p-0 leading-none"
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
              <li
                key={goal.id}
                className="flex items-start gap-2 bg-green-50 rounded-xl px-3 py-2"
              >
                <span className="text-green-600 font-bold text-sm flex-shrink-0">{i + 1}.</span>
                <span className="text-sm text-gray-700 leading-snug">{goal.title}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
