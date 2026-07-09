import { useState, useCallback, useEffect } from 'react'
import { RewardToast } from '../../components/Shop/RewardToast'
import { useNavigate } from 'react-router-dom'
import { useUserGoals } from '../../hooks/useUserGoals'
import { InlineError } from '../../components/feedback/InlineError'
import { useToast } from '../../context/toast'
import { type UserGoalResponse, type UserPlantSummaryResponse } from '../../api/userGoals'
import { userPlantsApi } from '../../api/userPlants'
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
import nurseryImg from '../../assets/challenges/nursery.png'
import './Challenges.css'
import { useTheme } from '../../context/theme'
import { useAuthGate } from '../../context/authGate'
import { ActivityType } from '../../api/activities'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'

function getPlantStage(progress: number, required: number): 0 | 1 | 2 | 3 | 4 | 5 {
  if (progress === 0) return 0
  return Math.min(Math.ceil((progress / required) * 5), 5) as 0 | 1 | 2 | 3 | 4 | 5
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
  const navigate = useNavigate()
  const { showToast, setToastsRaised } = useToast()
  const [modal, setModal] = useState<ModalState>(null)
  const [isWatering, setIsWatering] = useState(false)
  const [harvestPlant, setHarvestPlant] = useState<number | null>(null)
  const [currentPlant, setCurrentPlant] = useState<UserPlantSummaryResponse | null>(null)
  const [coinToast, setCoinToast] = useState<number | null>(null)

  const { pendientes, completados, loading, error, createGoal, updateGoal, deleteGoal, completeGoal } =
    useUserGoals()

  const { markConditionMet, saveSession } = useActivitySessionTracker(ActivityType.CHALLENGE, {
    autoStart: true,
  })

  useEffect(() => {
    userPlantsApi.getCurrent()
      .then(setCurrentPlant)
      .catch(() => {/* no plant yet, will be created on first complete */ })
  }, [])

  const cycleProgress = currentPlant?.completedGoalsCount ?? 0
  const requiredGoals = currentPlant?.requiredGoals ?? 5
  const plantStage = getPlantStage(cycleProgress, requiredGoals)
  const cyclePct = Math.round((cycleProgress / requiredGoals) * 100)

  const triggerWatering = useCallback(() => {
    setIsWatering(true)
    const timer = setTimeout(() => setIsWatering(false), 3000)
    return () => clearTimeout(timer)
  }, [])

  const handleCreate = useCallback(async (data: { title: string; description: string }) => {
    await createGoal({ title: data.title, description: data.description || undefined })
  }, [createGoal])

  const handleUpdate = useCallback(async (id: number, data: { title: string; description: string }) => {
    await updateGoal(id, { title: data.title, description: data.description || undefined })
  }, [updateGoal])

  const handleDelete = useCallback(async (id: number) => {
    try {
      await deleteGoal(id)
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Error al eliminar el reto', 'error')
    }
  }, [deleteGoal, showToast])

  const handleComplete = useCallback(async (id: number, image?: File) => {
    try {
      const result = await completeGoal(id, image)
      markConditionMet()
      await saveSession()
      setCurrentPlant(result.currentPlant)
      const coinsEarned = image
        ? (result.goal.coinsRewardWithImage ?? 25)
        : (result.goal.coinsReward ?? 10)
      if (!image) {
        showToast('📸 Tip: subí una foto como evidiencia la próxima vez y ganás más semillas', 'info')
      }
      setToastsRaised(true)
      setCoinToast(coinsEarned)
      if (result.harvestTriggered) {
        setHarvestPlant(result.harvestedPlantNumber)
      } else {
        triggerWatering()
      }
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Error al completar el reto', 'error')
    }
  }, [completeGoal, triggerWatering, markConditionMet, saveSession, showToast, setToastsRaised])

  const hasPending = (pendientes?.totalElements ?? 0) > 0
  const hasCompleted = (completados?.totalElements ?? 0) > 0
  const hasAnyGoal = hasPending || hasCompleted

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
          <h1 className="challenges-title text-2xl font-extrabold text-bosque m-0 text-center tracking-[-0.01em] lg:text-[2rem]">
            Mis Retos
          </h1>

          <div className="w-full max-w-[220px] text-center">
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
            <p className="text-[0.78rem] font-bold text-bosque mt-[0.2rem] m-0 lg:text-[0.95rem]">{cyclePct}%</p>
          </div>

          <p className="challenges-plant-hint text-[0.72rem] text-bosque text-center max-w-[210px] m-0 italic leading-[1.4] pt-4 lg:text-[0.94rem]">
            {PLANT_HINTS[plantStage]}
          </p>

          <button
            className="mt-1 flex items-center gap-2 px-5 py-2.5 rounded-full bg-white/70 border border-bosque/20 shadow-sm cursor-pointer hover:bg-white/90 hover:shadow-md transition-all duration-150"
            onClick={() => navigate('/orchard')}
          >
            <img src={nurseryImg} alt="" aria-hidden="true" className="w-12 h-12 object-contain" />
            <span className="text-sm font-semibold text-bosque lg:text-[1rem]">Ver Vivero</span>
          </button>
        </div>

        <div className="plant-on-stump mt-auto flex flex-col items-center relative mr-3 translate-y-[140px]">
          <Plant stage={plantStage} isWatering={isWatering} plantType={currentPlant?.plantNumber ?? 1} />
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

            {loading && <p className="text-[0.82rem] text-anaranjado m-0 italic [text-shadow:0_1px_0_rgba(255,255,255,0.4)]">Cargando retos…</p>}
            {error && !loading && <InlineError message={error} className="mt-2" />}

            {!loading && !error && (
              <ul className="board-list list-none p-0 m-0 flex flex-col gap-[0.35rem] flex-1 min-h-0 overflow-x-hidden overflow-y-auto">

                {hasAnyGoal && (
                  <li className="text-[0.82rem] italic list-none py-[0.2rem] text-[#7a5c38] [text-shadow:0_1px_0_rgba(255,255,255,0.35)] lg:text-[0.9rem]">
                    Huly creó estos retos para vos. ¡Completalos y hacé florecer tu planta!
                  </li>
                )}

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
                ) : !hasCompleted ? (
                  <li className="list-none py-3 text-[#7a5c38] [text-shadow:0_1px_0_rgba(255,255,255,0.35)]">
                    <p className="m-0 mt-1 text-[0.82rem] italic leading-[1.4] lg:text-[0.9rem]">
                      Aún no tenés retos. Hablá con Huly y va a crear unos personalizados para vos
                    </p>
                  </li>
                ) : null}

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
          onGoToOrchard={() => navigate('/orchard')}
        />
      )}

      {coinToast !== null && (
        <RewardToast
          coins={coinToast}
          onClose={() => { setCoinToast(null); setToastsRaised(false) }}
          message="¡Reto completado!"
        />
      )}
    </div>
  )
}
