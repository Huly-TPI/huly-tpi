import { useState, useCallback } from 'react'
import { useUserGoals } from '../../hooks/useUserGoals'
import { type UserGoalResponse } from '../../api/userGoals'
import Plant from '../../components/Challenges/Plant'
import BoardItem from '../../components/Challenges/BoardItem'
import PostitModal from '../../components/Challenges/PostitModal'
import HarvestModal from '../../components/Challenges/HarvestModal'
import Button from '../../components/Buttons/Button/Button'
import BackButton from '../../components/Buttons/BackButton/BackButton'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import dayBackground from '../../assets/shared/day-background.webp'
import nightBackground from '../../assets/shared/dark-background.webp'
import boardBg from '../../assets/challenges/board-challenges.png'
import stumpImg from '../../assets/challenges/stump.png'
import './Challenges.css'
import { useTheme } from '../../context/theme'
import { useAuthGate } from '../../context/authGate'
import { ActivityType } from '../../api/activities'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'

const CYCLE_SIZE = 16

function getCycleData(completedCount: number) {
  const completedPlants = Math.floor(completedCount / CYCLE_SIZE)
  const cycleProgress = completedCount % CYCLE_SIZE
  return { completedPlants, cycleProgress }
}

function getPlantStage(cycleProgress: number): 0 | 1 | 2 | 3 | 4 | 5 {
  if (cycleProgress === 0) return 0
  if (cycleProgress <= 2) return 1
  if (cycleProgress <= 5) return 2
  if (cycleProgress <= 9) return 3
  if (cycleProgress <= 13) return 4
  return 5
}

const PLANT_HINTS: Record<0 | 1 | 2 | 3 | 4 | 5, string> = {
  0: '¡Completá retos para regar tu planta!',
  1: 'Tu planta está brotando. ¡Seguí así!',
  2: '¡Ya tiene sus primeras hojas!',
  3: '¡Tu planta está creciendo fuerte!',
  4: '¡Ya casi florece!',
  5: '¡Lista para cosechar!',
}

type ModalState =
  | null
  | { mode: 'create' }
  | { mode: 'detail'; goal: UserGoalResponse }

export default function Challenges() {
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const { requireAuth } = useAuthGate()
  const [modal, setModal] = useState<ModalState>(null)
  const [isWatering, setIsWatering] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [harvestPlant, setHarvestPlant] = useState<number | null>(null)

  const { pendientes, completados, loading, error, createGoal, updateGoal, deleteGoal, completeGoal } =
    useUserGoals()

  const { markConditionMet, saveSession } = useActivitySessionTracker(ActivityType.RETO, {
    autoStart: true,
  })

  const completedCount = completados?.totalElements ?? 0
  const { completedPlants, cycleProgress } = getCycleData(completedCount)
  const plantStage = getPlantStage(cycleProgress)
  const cyclePct = Math.round((cycleProgress / CYCLE_SIZE) * 100)

  const triggerWatering = useCallback(() => {
    setIsWatering(true)
    const timer = setTimeout(() => setIsWatering(false), 3000)
    return () => clearTimeout(timer)
  }, [])

  const handleCreate = useCallback(async (data: { title: string; description: string }) => {
    await createGoal({ title: data.title, description: data.description || undefined })
    markConditionMet()
  }, [createGoal, markConditionMet])

  const handleUpdate = useCallback(async (id: number, data: { title: string; description: string }) => {
    await updateGoal(id, { title: data.title, description: data.description || undefined })
  }, [updateGoal])

  const handleDelete = useCallback(async (id: number) => {
    setActionError(null)
    try {
      await deleteGoal(id)
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Error al eliminar el reto')
    }
  }, [deleteGoal])

  const handleComplete = useCallback(async (id: number) => {
    setActionError(null)
    const isHarvest = cycleProgress === CYCLE_SIZE - 1
    try {
      await completeGoal(id)
      markConditionMet()
      await saveSession()
      if (isHarvest) {
        setHarvestPlant(completedPlants + 1)
      } else {
        triggerWatering()
      }
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Error al completar el reto')
    }
  }, [cycleProgress, completedPlants, completeGoal, triggerWatering, markConditionMet, saveSession])

  const hasPending  = (pendientes?.totalElements ?? 0) > 0
  const hasCompleted = (completados?.totalElements ?? 0) > 0

  return (
    <div
      className="challenges-page relative h-full flex flex-row items-stretch overflow-hidden"
    >
      <ThemeBackground
        lightSrc={dayBackground}
        darkSrc={nightBackground}
        lightAlt="Fondo de retos"
        darkAlt="Fondo nocturno de retos"
      />
      <BackButton to="/" />

      <aside className="plant-zone relative z-10 flex-shrink-0 w-[44%] flex flex-col items-center gap-[0.6rem] pt-6 px-2 justify-start overflow-hidden">
        <div className="plant-zone__info flex flex-col items-center gap-[0.6rem] w-full">
          <h1 className="challenges-title text-2xl font-extrabold text-bosque m-0 text-center tracking-[-0.01em]">
            Mis Retos
          </h1>

          <div className="w-full max-w-[200px] text-center">
            <p className="text-[0.78rem] text-bosque m-0 mb-[0.35rem]">
              <strong>{cycleProgress}</strong> / {CYCLE_SIZE} en este ciclo
            </p>
            <div
              className="h-[9px] bg-white/30 rounded-full overflow-hidden w-full"
              role="progressbar"
              aria-valuenow={cyclePct}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label={`Progreso: ${cyclePct}%`}
            >
              <div
                className="h-full bg-gradient-to-r from-bosque to-[#a8d8a8] rounded-full transition-[width] duration-1000"
                style={{ width: `${cyclePct}%` }}
              />
            </div>
            <p className="text-[0.78rem] font-bold text-bosque mt-[0.2rem] m-0">{cyclePct}%</p>
          </div>

          <p className="challenges-plant-hint text-[0.72rem] text-bosque text-center max-w-[190px] m-0 italic leading-[1.4] pt-4">
            {PLANT_HINTS[plantStage]}
          </p>

          {completedPlants > 0 && (
            <p className="text-[0.72rem] text-bosque text-center m-0">
              Plantas cosechadas: <strong>{completedPlants}</strong>
            </p>
          )}
        </div>

        <div className="plant-on-stump mt-auto flex flex-col items-center relative mr-3 translate-y-[140px]">
          <Plant stage={plantStage} isWatering={isWatering} />
          <img
            src={stumpImg}
            className="w-[250px] -mt-[105px] relative z-0 object-contain drop-shadow-[0_4px_10px_rgba(0,0,0,0.22)]"
            alt=""
            aria-hidden="true"
          />
        </div>
      </aside>

      <div className="challenges-right relative z-10 flex-1 flex items-center justify-end py-8 pr-4">
        <section
          className="board-zone relative w-[850px] h-[calc(100dvh-8rem)] bg-no-repeat [background-size:100%_100%] bg-center flex items-stretch rounded-[6px] drop-shadow-[0_8px_24px_rgba(0,0,0,0.3)] overflow-hidden"
          style={{ backgroundImage: `url(${boardBg})` }}
          aria-label="Listado de retos"
        >
          {isDark && (
            <div
              className="absolute inset-0 bg-[#2f2418]/[0.12] pointer-events-none"
              aria-hidden="true"
            />
          )}
          <div className="board-inner relative z-10 flex-1 flex flex-col gap-[0.7rem] pt-[4rem] pl-[6rem] pr-[5rem] pb-[6rem] overflow-hidden min-h-0">
            {actionError && (
              <div className="flex items-start justify-between gap-[0.6rem] bg-[rgba(255,245,245,0.92)] border border-[#FEB2B2] rounded-[7px] py-[0.55rem] px-[0.75rem] text-[0.8rem] text-[#C53030] leading-[1.4]" role="alert">
                <span>{actionError}</span>
                <button
                  className="bg-transparent border-0 cursor-pointer text-[#C53030] text-[0.85rem] p-0 flex-shrink-0 opacity-70 hover:opacity-100"
                  onClick={() => setActionError(null)}
                  aria-label="Cerrar"
                >
                  ✕
                </button>
              </div>
            )}

            <div className="flex justify-end mb-[0.1rem]">
              <Button variant="primary" size="sm" onClick={() => requireAuth(() => setModal({ mode: 'create' }))}>
                + Nuevo reto
              </Button>
            </div>

            {loading && <p className="text-[0.82rem] text-anaranjado m-0 italic [text-shadow:0_1px_0_rgba(255,255,255,0.4)]">Cargando retos…</p>}
            {error && !loading && <p className="text-[0.82rem] text-[#9b2c2c] m-0 italic [text-shadow:0_1px_0_rgba(255,255,255,0.4)]">{error}</p>}

            {!loading && !error && (
              <ul className="board-list list-none p-0 m-0 flex flex-col gap-[0.35rem] flex-1 min-h-0 overflow-x-hidden overflow-y-auto">
                {hasPending ? (
                  pendientes!.content.map(goal => (
                    <li key={goal.id}>
                      <BoardItem
                        goal={goal}
                        darkMode={isDark}
                        onSelect={g => setModal({ mode: 'detail', goal: g })}
                        onComplete={handleComplete}
                      />
                    </li>
                  ))
                ) : (
                  <li className="text-[0.82rem] italic list-none py-[0.2rem] text-[#7a5c38] [text-shadow:0_1px_0_rgba(255,255,255,0.35)]">
                    Sembrá tus metas. ¡Creá un nuevo reto!
                  </li>
                )}

                {hasCompleted && (
                  <li
                    className="h-[3px] bg-gradient-to-r from-transparent via-[rgba(120,120,120,0.55)] to-transparent my-2 border-none list-none"
                    role="separator"
                  />
                )}

                {hasCompleted && completados!.content.map(goal => (
                  <li key={goal.id}>
                    <BoardItem
                      goal={goal}
                      darkMode={isDark}
                      onSelect={g => setModal({ mode: 'detail', goal: g })}
                    />
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      </div>

      {modal && (
        <PostitModal
          initialMode={modal.mode}
          goal={'goal' in modal ? modal.goal : undefined}
          onClose={() => setModal(null)}
          onCreate={handleCreate}
          onUpdate={handleUpdate}
          onDelete={handleDelete}
          onComplete={handleComplete}
        />
      )}

      {harvestPlant !== null && (
        <HarvestModal
          plantNumber={harvestPlant}
          onCreateNew={() => { setHarvestPlant(null); setModal({ mode: 'create' }) }}
        />
      )}
    </div>
  )
}
