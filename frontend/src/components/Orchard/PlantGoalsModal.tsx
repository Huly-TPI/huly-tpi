import { useState, useEffect } from 'react'
import { userPlantsApi } from '../../api/userPlants'
import { type UserGoalResponse, type UserPlantSummaryResponse } from '../../api/userGoals'
import Plant from '../Challenges/Plant'
import PostitModal from '../Challenges/PostitModal'

const SCALE = 0.7
const PLANT_W = 220
const PLANT_H = 260
const VISUAL_W = Math.round(PLANT_W * SCALE)
const VISUAL_H = Math.round(PLANT_H * SCALE)

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

function GoalListItem({ goal, index, onClick }: { goal: UserGoalResponse; index: number; onClick: () => void }) {
  return (
    <li
      className="flex flex-col gap-1 bg-green-50 rounded-xl px-3 py-2 flex-shrink-0 cursor-pointer hover:bg-green-100 active:bg-green-200 transition-colors duration-100"
      onClick={onClick}
      role="button"
      tabIndex={0}
      onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') onClick() }}
      aria-label={`Ver detalle: ${goal.title}`}
    >
      <div className="flex items-start gap-2">
        <span className="text-green-600 font-bold text-sm flex-shrink-0">{index + 1}.</span>
        <span className="text-sm text-gray-700 leading-snug font-medium">{goal.title}</span>
        {goal.imageUrl && (
          <span className="ml-auto text-green-500 text-xs flex-shrink-0" title="Tiene foto">📷</span>
        )}
      </div>
      {goal.description && (
        <p className="text-xs text-gray-500 ml-5 leading-relaxed italic">{goal.description}</p>
      )}
    </li>
  )
}

export default function PlantGoalsModal({ plant, onClose }: PlantGoalsModalProps) {
  const [goals, setGoals] = useState<UserGoalResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedGoal, setSelectedGoal] = useState<UserGoalResponse | null>(null)
  const [isMobile, setIsMobile] = useState(() => window.innerWidth < 768)

  useEffect(() => {
    const handler = () => setIsMobile(window.innerWidth < 768)
    window.addEventListener('resize', handler)
    return () => window.removeEventListener('resize', handler)
  }, [])

  useEffect(() => {
    userPlantsApi.getGoals(plant.id)
      .then(res => setGoals(res.goals))
      .catch(() => setGoals([]))
      .finally(() => setLoading(false))
  }, [plant.id])

  const goalList = (
    <>
      {loading ? (
        <p className="text-sm text-gray-500 italic text-center py-4">Cargando retos…</p>
      ) : goals.length === 0 ? (
        <p className="text-sm text-gray-500 italic text-center py-4">Sin retos registrados para esta planta.</p>
      ) : (
        <ul className="list-none p-0 m-0 flex flex-col gap-2 overflow-y-auto flex-1 pr-1">
          {goals.map((goal, i) => (
            <GoalListItem key={goal.id} goal={goal} index={i} onClick={() => setSelectedGoal(goal)} />
          ))}
        </ul>
      )}
    </>
  )

  return (
    <>
      <div
        className="fixed inset-0 bg-black/[0.42] flex items-center justify-center z-[1000] p-4 backdrop-blur-[3px]"
        role="dialog"
        aria-modal="true"
        aria-label={`Retos de la Planta #${plant.plantNumber}`}
        onClick={e => { if (e.target === e.currentTarget) onClose() }}
      >
        <div className="bg-white rounded-3xl w-full max-w-[680px] shadow-[0_24px_64px_rgba(0,0,0,0.28)] flex flex-col max-h-[82vh] overflow-hidden">

          <div className="flex items-center justify-between gap-2 px-6 pt-5 pb-3 border-b border-gray-100 flex-shrink-0">
            <h2 className="text-[1.15rem] font-extrabold text-[#2D3748] leading-snug">
              Planta #{plant.plantNumber}
            </h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 bg-transparent border-0 cursor-pointer text-xl p-0 leading-none flex-shrink-0"
              aria-label="Cerrar"
            >
              ✕
            </button>
          </div>

          {isMobile ? (
            <div className="flex flex-col flex-1 min-h-0">
              <div className="flex items-center gap-4 px-5 py-4 border-b border-gray-100 flex-shrink-0">
                <div style={{ width: VISUAL_W * 0.7, height: VISUAL_H * 0.7, overflow: 'hidden', position: 'relative', flexShrink: 0 }}>
                  <div style={{ width: PLANT_W, height: PLANT_H, transform: `scale(${SCALE * 0.7})`, transformOrigin: 'top left', position: 'absolute', top: 0, left: 0 }}>
                    <Plant stage={5} isWatering={false} plantType={plant.plantNumber} />
                  </div>
                </div>
                <div className="flex flex-col gap-1">
                  {plant.completedAt && (
                    <p className="text-xs text-gray-400">
                      Cosechada el{' '}
                      <span className="font-semibold text-gray-500">{formatDate(plant.completedAt)}</span>
                    </p>
                  )}
                  {goals.length > 0 && (
                    <p className="text-xs text-gray-400 italic">
                      {goals.length} reto{goals.length !== 1 ? 's' : ''} completado{goals.length !== 1 ? 's' : ''}
                    </p>
                  )}
                </div>
              </div>
              <div className="flex flex-col flex-1 min-h-0 px-5 py-4">
                <p className="text-[0.7rem] uppercase tracking-wider text-gray-400 font-semibold mb-3 flex-shrink-0">
                  Retos completados · tocá uno para ver detalle
                </p>
                {goalList}
              </div>
            </div>
          ) : (
            <div className="flex flex-1 min-h-0">
              <div className="flex flex-col items-center gap-3 px-6 py-5 border-r border-gray-100 w-[240px] flex-shrink-0">
                {plant.completedAt && (
                  <p className="text-xs text-gray-400 text-center">
                    Cosechada el<br />
                    <span className="font-semibold text-gray-500">{formatDate(plant.completedAt)}</span>
                  </p>
                )}
                <div className="flex-1 flex items-end justify-center">
                  <div style={{ width: VISUAL_W, height: VISUAL_H, overflow: 'hidden', position: 'relative' }}>
                    <div style={{ width: PLANT_W, height: PLANT_H, transform: `scale(${SCALE})`, transformOrigin: 'top left', position: 'absolute', top: 0, left: 0 }}>
                      <Plant stage={5} isWatering={false} plantType={plant.plantNumber} />
                    </div>
                  </div>
                </div>
                <p className="text-xs text-gray-400 italic text-center">
                  {goals.length > 0 ? `${goals.length} reto${goals.length !== 1 ? 's' : ''} completado${goals.length !== 1 ? 's' : ''}` : ''}
                </p>
              </div>

              <div className="flex flex-col flex-1 min-w-0 py-5 px-5">
                <p className="text-[0.7rem] uppercase tracking-wider text-gray-400 font-semibold mb-3">
                  Retos completados · hacé click en uno para ver detalle
                </p>
                {goalList}
              </div>
            </div>
          )}
        </div>
      </div>

      {selectedGoal && (
        <PostitModal
          initialMode="detail"
          goal={selectedGoal}
          onClose={() => setSelectedGoal(null)}
        />
      )}
    </>
  )
}
