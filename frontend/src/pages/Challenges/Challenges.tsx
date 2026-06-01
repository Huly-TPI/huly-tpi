import { useState, useCallback } from 'react'
import { useUserGoals } from '../../hooks/useUserGoals'
import { type UserGoalResponse } from '../../api/userGoals'
import BackButton from '../../components/Buttons/BackButton/BackButton'
import dayBackground from '../../assets/garden/light-theme/background/day-background.webp'
import wateringCanImg from '../../assets/challenges/watering-can.png'
import flowerpotBaseImg from '../../assets/challenges/plant-stages/flowerpot-base.png'
import flowerpotTopImg from '../../assets/challenges/plant-stages/flowerpot-top.png'
import stumpImg from '../../assets/challenges/stump.png'
import challengeDetailBg from '../../assets/challenges/challenge-detail-bg.png'
import plant0 from '../../assets/challenges/plant-stages/plant-0.png'
import plant1 from '../../assets/challenges/plant-stages/plant-1.png'
import plant2 from '../../assets/challenges/plant-stages/plant-2.png'
import plant3 from '../../assets/challenges/plant-stages/plant-3.png'
import plant4 from '../../assets/challenges/plant-stages/plant-4.png'
import plant5 from '../../assets/challenges/plant-stages/plant-5.png'
import './Challenges.css'

const PLANT_IMAGES = [plant0, plant1, plant2, plant3, plant4, plant5] as const
const CYCLE_SIZE = 16

function getUserIdFromToken(): number | null {
  try {
    const urlParam = new URLSearchParams(window.location.search).get('userId')
    if (urlParam) return parseInt(urlParam, 10)
    const token = localStorage.getItem('token')
    if (!token) return null
    const payload = JSON.parse(atob(token.split('.')[1]))
    return typeof payload.userId === 'number' ? payload.userId : null
  } catch {
    return null
  }
}

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

interface PlantProps {
  stage: 0 | 1 | 2 | 3 | 4 | 5
  isWatering: boolean
}

function Plant({ stage, isWatering }: PlantProps) {
  return (
    <div className={`plant${isWatering ? ' plant--watering' : ''}`}>
      {isWatering && (
        <img src={wateringCanImg} alt="" aria-hidden="true" className="plant__watering-can" />
      )}
      <img src={flowerpotBaseImg} alt="" aria-hidden="true" className="plant__flowerpot-base" />
      <img key={stage} src={PLANT_IMAGES[stage]} alt={`Planta etapa ${stage + 1}`} className="plant__image" />
      <div className="plant__drops" aria-hidden="true">
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
      </div>
      <img src={flowerpotTopImg} alt="" aria-hidden="true" className="plant__flowerpot-top" />
    </div>
  )
}

interface BoardItemProps {
  goal: UserGoalResponse
  onSelect: (goal: UserGoalResponse) => void
  onComplete?: (id: number) => Promise<void>
}

function BoardItem({ goal, onSelect, onComplete }: BoardItemProps) {
  const [completing, setCompleting] = useState(false)
  const isCompleted = goal.status === 'COMPLETED'

  const handleComplete = async (e: React.MouseEvent) => {
    e.stopPropagation()
    if (!onComplete) return
    setCompleting(true)
    try {
      await onComplete(goal.id)
    } catch {
      setCompleting(false)
    }
  }

  return (
    <div
      className={`board-item${isCompleted ? ' board-item--completed' : ''}${completing ? ' board-item--completing' : ''}`}
      onClick={() => onSelect(goal)}
      role="button"
      tabIndex={0}
      onKeyDown={e => e.key === 'Enter' && onSelect(goal)}
      aria-label={`Ver detalles: ${goal.title}`}
    >
      <span className="board-item__title">{goal.title}</span>

      {!isCompleted && onComplete && (
        <button
          className="board-item__water-btn"
          onClick={handleComplete}
          disabled={completing}
          aria-label="Completar reto"
          title="Completar reto"
        >
          <img
            src={wateringCanImg}
            alt=""
            aria-hidden="true"
            className={`board-item__can${completing ? ' board-item__can--pouring' : ''}`}
          />
        </button>
      )}

      {isCompleted && <span className="board-item__done" aria-label="Completado">✓</span>}
    </div>
  )
}

type PostitMode = 'create' | 'edit' | 'detail'

interface PostitModalProps {
  initialMode: PostitMode
  goal?: UserGoalResponse
  onClose: () => void
  onCreate?: (data: { title: string; description: string }) => Promise<void>
  onUpdate?: (id: number, data: { title: string; description: string }) => Promise<void>
  onDelete?: (id: number) => Promise<void>
  onComplete?: (id: number) => Promise<void>
}

function PostitModal({ initialMode, goal, onClose, onCreate, onUpdate, onDelete, onComplete }: PostitModalProps) {
  const [mode, setMode] = useState<PostitMode>(initialMode)
  const [title, setTitle] = useState(goal?.title ?? '')
  const [description, setDescription] = useState(goal?.description ?? '')
  const [loading, setLoading] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [completing, setCompleting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isCompleted = goal?.status === 'COMPLETED'

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      if (mode === 'create' && onCreate) {
        await onCreate({ title: title.trim(), description: description.trim() })
      } else if (mode === 'edit' && onUpdate && goal) {
        await onUpdate(goal.id, { title: title.trim(), description: description.trim() })
      }
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error inesperado')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async () => {
    if (!onDelete || !goal) return
    setDeleting(true)
    try {
      await onDelete(goal.id)
      onClose()
    } catch {
      setDeleting(false)
    }
  }

  const handleComplete = async () => {
    if (!onComplete || !goal) return
    setCompleting(true)
    try {
      await onComplete(goal.id)
      onClose()
    } catch {
      setCompleting(false)
    }
  }

  const enterEditMode = () => {
    setTitle(goal?.title ?? '')
    setDescription(goal?.description ?? '')
    setError(null)
    setMode('edit')
  }

  const handleCancel = () => {
    if (initialMode === 'detail') {
      setMode('detail')
    } else {
      onClose()
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div
        className="postit-modal"
        style={{ backgroundImage: `url(${challengeDetailBg})` }}
      >
        <button className="postit-modal__close" onClick={onClose} aria-label="Cerrar">✕</button>

        {mode === 'detail' && goal && (
          <>
            {isCompleted && <span className="postit-modal__badge">✓ Completado</span>}
            <h3 className={`postit-modal__title${isCompleted ? ' postit-modal__title--done' : ''}`}>
              {goal.title}
            </h3>
            {goal.description && (
              <p className="postit-modal__desc">{goal.description}</p>
            )}
            <div className="postit-modal__actions">
              {!isCompleted && onComplete && (
                <button
                  className="postit-btn postit-btn--complete"
                  onClick={handleComplete}
                  disabled={completing}
                >
                  {completing ? '…' : '✓ Completar'}
                </button>
              )}
              {!isCompleted && (
                <button className="postit-btn postit-btn--edit" onClick={enterEditMode}>
                  ✎ Editar
                </button>
              )}
              <button
                className="postit-btn postit-btn--delete"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? '…' : '✕ Eliminar'}
              </button>
            </div>
          </>
        )}

        {(mode === 'create' || mode === 'edit') && (
          <form onSubmit={handleSubmit} className="postit-modal__form">
            <p className="postit-modal__form-heading">
              {mode === 'create' ? 'Nuevo reto' : 'Editar reto'}
            </p>
            <label className="postit-modal__label" htmlFor="postit-title">Título</label>
            <input
              id="postit-title"
              className="postit-modal__input"
              type="text"
              value={title}
              onChange={e => setTitle(e.target.value)}
              maxLength={255}
              required
              autoFocus
              placeholder="¿Qué querés lograr?"
            />
            <label className="postit-modal__label" htmlFor="postit-desc">Descripción</label>
            <textarea
              id="postit-desc"
              className="postit-modal__textarea"
              value={description}
              onChange={e => setDescription(e.target.value)}
              maxLength={250}
              rows={3}
              placeholder="Describe tu reto (opcional)"
            />
            {error && <p className="postit-modal__error">{error}</p>}
            <div className="postit-modal__actions">
              <button
                type="button"
                className="postit-btn postit-btn--cancel"
                onClick={handleCancel}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="postit-btn postit-btn--save"
                disabled={loading || !title.trim()}
              >
                {loading ? '…' : 'Guardar'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

interface HarvestModalProps {
  plantNumber: number
  onViewGarden: () => void
  onCreateNew: () => void
}

function HarvestModal({ plantNumber, onCreateNew }: HarvestModalProps) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="harvest-modal">
        <img src={plant5} alt="Planta cosechada" className="harvest-modal__plant" />
        <h2 className="harvest-modal__title">
          ¡Cosechaste la Planta #{plantNumber}!
        </h2>
        <p className="harvest-modal__subtitle">
          Tu jardín sigue creciendo. ¡Seguí así!
        </p>
        <div className="harvest-modal__actions">
          {/* <button className="harvest-btn harvest-btn--garden" onClick={onViewGarden}>
            Ver mi huerta
          </button> */}
          <button className="harvest-btn harvest-btn--new" onClick={onCreateNew}>
            + Crear nuevo reto
          </button>
        </div>
      </div>
    </div>
  )
}

type ModalState =
  | null
  | { mode: 'create' }
  | { mode: 'detail'; goal: UserGoalResponse }

export default function Challenges() {
  const [userId] = useState<number | null>(() => getUserIdFromToken())
  const [modal, setModal] = useState<ModalState>(null)
  const [isWatering, setIsWatering] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [harvestPlant, setHarvestPlant] = useState<number | null>(null)

  const { pendientes, completados, loading, error, createGoal, updateGoal, deleteGoal, completeGoal } =
    useUserGoals(userId)

  const completedCount = completados?.totalElements ?? 0
  const { completedPlants, cycleProgress } = getCycleData(completedCount)
  const plantStage = getPlantStage(cycleProgress)
  const cyclePct = Math.round((cycleProgress / CYCLE_SIZE) * 100)

  const triggerWatering = useCallback(() => {
    setIsWatering(true)
    const timer = setTimeout(() => setIsWatering(false), 3000)
    return () => clearTimeout(timer)
  }, [])

  const handleCreate = async (data: { title: string; description: string }) => {
    await createGoal({ title: data.title, description: data.description || undefined })
  }

  const handleUpdate = async (id: number, data: { title: string; description: string }) => {
    await updateGoal(id, { title: data.title, description: data.description || undefined })
  }

  const handleDelete = async (id: number) => {
    setActionError(null)
    try {
      await deleteGoal(id)
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Error al eliminar el reto')
    }
  }

  const handleComplete = async (id: number) => {
    setActionError(null)
    const isHarvest = cycleProgress === CYCLE_SIZE - 1
    try {
      await completeGoal(id)
      if (isHarvest) {
        setHarvestPlant(completedPlants + 1)
      } else {
        triggerWatering()
      }
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Error al completar el reto')
    }
  }

  const hasPending  = (pendientes?.totalElements ?? 0) > 0
  const hasCompleted = (completados?.totalElements ?? 0) > 0

  return (
    <div
      className="challenges-page"
      style={{ backgroundImage: `url(${dayBackground})`, backgroundSize: 'cover', backgroundPosition: 'center' }}
    >
      <BackButton />

      <aside className="plant-zone">
        <div className="plant-zone__info">
          <h1 className="challenges-title">Mis Retos</h1>

          <div className="challenges-progress">
            <p className="challenges-progress__text">
              <strong>{cycleProgress}</strong> / {CYCLE_SIZE} en este ciclo
            </p>
            <div
              className="challenges-progress__bar"
              role="progressbar"
              aria-valuenow={cyclePct}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label={`Progreso: ${cyclePct}%`}
            >
              <div className="challenges-progress__fill" style={{ width: `${cyclePct}%` }} />
            </div>
            <p className="challenges-progress__pct">{cyclePct}%</p>
          </div>

          <p className="challenges-plant-hint">{PLANT_HINTS[plantStage]}</p>

          {completedPlants > 0 && (
            <p className="challenges-total-completed">
              Plantas cosechadas: <strong>{completedPlants}</strong>
            </p>
          )}
        </div>

        <div className="plant-on-stump">
          <Plant stage={plantStage} isWatering={isWatering} />
          <img src={stumpImg} className="stump-img" alt="" aria-hidden="true" />
        </div>
      </aside>

      <div className="challenges-right">
        <section className="board-zone" aria-label="Listado de retos">
          <div className="board-inner">
            {actionError && (
              <div className="challenges-error" role="alert">
                <span>{actionError}</span>
                <button onClick={() => setActionError(null)} aria-label="Cerrar">✕</button>
              </div>
            )}

            <div className="board-header">
              <button className="challenges-add-btn" onClick={() => setModal({ mode: 'create' })}>
                + Nuevo reto
              </button>
            </div>

            {loading && <p className="board-status">Cargando retos…</p>}
            {error && !loading && <p className="board-status board-status--error">{error}</p>}
            {!userId && !loading && <p className="board-status">Inicia sesión para ver tus retos.</p>}

            {!loading && !error && userId && (
              <ul className="board-list">
                {hasPending ? (
                  pendientes!.content.map(goal => (
                    <li key={goal.id}>
                      <BoardItem
                        goal={goal}
                        onSelect={g => setModal({ mode: 'detail', goal: g })}
                        onComplete={handleComplete}
                      />
                    </li>
                  ))
                ) : (
                  <li className="board-empty">Sembrá tus metas. ¡Creá un nuevo reto!</li>
                )}

                {hasCompleted && (
                  <li className="board-divider" role="separator" />
                )}

                {hasCompleted && completados!.content.map(goal => (
                  <li key={goal.id}>
                    <BoardItem
                      goal={goal}
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
          onViewGarden={() => setHarvestPlant(null)}
          onCreateNew={() => { setHarvestPlant(null); setModal({ mode: 'create' }) }}
        />
      )}
    </div>
  )
}
