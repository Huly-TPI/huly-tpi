import { useState, useCallback } from 'react'
import { useUserGoals } from '../../hooks/useUserGoals'
import { type UserGoalResponse } from '../../api/userGoals'
import dayBackground from '../../assets/garden/light-theme/background/day-background.webp'
import wateringCanImg from '../../assets/garden/light-theme/watering-can.png'
import flowerpotImg from '../../assets/garden/plant-stages/flowerpot.png'
import stumpImg from '../../assets/garden/light-theme/stump.png'
import postitImg from '../../assets/garden/light-theme/postit.png'
import boardBg from '../../assets/garden/light-theme/board-challenges.png'
import plant0 from '../../assets/garden/plant-stages/plant-0.png'
import plant1 from '../../assets/garden/plant-stages/plant-1.png'
import plant2 from '../../assets/garden/plant-stages/plant-2.png'
import plant3 from '../../assets/garden/plant-stages/plant-3.png'
import plant4 from '../../assets/garden/plant-stages/plant-4.png'
import plant5 from '../../assets/garden/plant-stages/plant-5.png'
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
  0: '¡Completá retos para empezar a regar tu planta!',
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
      <div className="plant__drops" aria-hidden="true">
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
      </div>
      <img key={stage} src={PLANT_IMAGES[stage]} alt={`Planta etapa ${stage + 1}`} className="plant__image" />
      <img src={flowerpotImg} alt="" aria-hidden="true" className="plant__flowerpot" />
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

interface GoalDetailModalProps {
  goal: UserGoalResponse
  onClose: () => void
  onEdit: (goal: UserGoalResponse) => void
  onDelete: (id: number) => Promise<void>
  onComplete?: (id: number) => Promise<void>
}

function GoalDetailModal({ goal, onClose, onEdit, onDelete, onComplete }: GoalDetailModalProps) {
  const [deleting, setDeleting] = useState(false)
  const [completing, setCompleting] = useState(false)
  const isCompleted = goal.status === 'COMPLETED'

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await onDelete(goal.id)
      onClose()
    } catch {
      setDeleting(false)
    }
  }

  const handleComplete = async () => {
    if (!onComplete) return
    setCompleting(true)
    try {
      await onComplete(goal.id)
      onClose()
    } catch {
      setCompleting(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
      <div
        className="postit-modal"
        onClick={e => e.stopPropagation()}
        style={{ backgroundImage: `url(${postitImg})` }}
      >
        <button className="postit-modal__close" onClick={onClose} aria-label="Cerrar">✕</button>
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
            <button className="postit-btn postit-btn--edit" onClick={() => onEdit(goal)}>
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
      </div>
    </div>
  )
}

interface HarvestModalProps {
  plantNumber: number
  onViewGarden: () => void
  onCreateNew: () => void
}

function HarvestModal({ plantNumber, onViewGarden, onCreateNew }: HarvestModalProps) {
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
          <button className="harvest-btn harvest-btn--garden" onClick={onViewGarden}>
            Ver mi huerta
          </button>
          <button className="harvest-btn harvest-btn--new" onClick={onCreateNew}>
            + Crear nuevo reto
          </button>
        </div>
      </div>
    </div>
  )
}

interface GoalModalProps {
  initialData?: { title: string; description: string }
  onClose: () => void
  onSubmit: (data: { title: string; description: string }) => Promise<void>
}

function GoalModal({ initialData, onClose, onSubmit }: GoalModalProps) {
  const [title, setTitle] = useState(initialData?.title ?? '')
  const [description, setDescription] = useState(initialData?.description ?? '')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await onSubmit({ title: title.trim(), description: description.trim() })
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error inesperado')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal" onClick={e => e.stopPropagation()}>
        <button className="modal__close" onClick={onClose} aria-label="Cerrar modal">✕</button>
        <h3 className="modal__title">{initialData ? 'Editar reto' : 'Nuevo reto'}</h3>
        <form onSubmit={handleSubmit} className="modal__form">
          <label className="modal__label" htmlFor="goal-title">Título *</label>
          <input
            id="goal-title"
            className="modal__input"
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value)}
            maxLength={255}
            required
            autoFocus
            placeholder="¿Qué quieres lograr?"
          />
          <label className="modal__label" htmlFor="goal-desc">Descripción</label>
          <textarea
            id="goal-desc"
            className="modal__textarea"
            value={description}
            onChange={e => setDescription(e.target.value)}
            maxLength={1000}
            rows={3}
            placeholder="Describe tu reto (opcional)"
          />
          {error && <p className="modal__error">{error}</p>}
          <div className="modal__actions">
            <button type="button" className="modal__btn modal__btn--cancel" onClick={onClose}>
              Cancelar
            </button>
            <button
              type="submit"
              className="modal__btn modal__btn--submit"
              disabled={loading || !title.trim()}
            >
              {loading ? 'Guardando…' : initialData ? 'Guardar' : 'Crear reto'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function Challenges() {
  const [userId] = useState<number | null>(() => getUserIdFromToken())
  const [showModal, setShowModal] = useState(false)
  const [editingGoal, setEditingGoal] = useState<UserGoalResponse | null>(null)
  const [selectedGoal, setSelectedGoal] = useState<UserGoalResponse | null>(null)
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

  const handleEdit = async (data: { title: string; description: string }) => {
    if (!editingGoal) return
    await updateGoal(editingGoal.id, { title: data.title, description: data.description || undefined })
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
      <aside className="plant-zone">
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

        <div className="plant-on-stump">
          <Plant stage={plantStage} isWatering={isWatering} />
          <img src={stumpImg} className="stump-img" alt="" aria-hidden="true" />
        </div>

        {completedPlants > 0 && (
          <p className="challenges-total-completed">
            Plantas cosechadas: <strong>{completedPlants}</strong>
          </p>
        )}
      </aside>

      <div className="challenges-right">
      <section
        className="board-zone"
        style={{ backgroundImage: `url(${boardBg})` }}
        aria-label="Listado de retos"
      >
        <div className="board-inner">
          {actionError && (
            <div className="challenges-error" role="alert">
              <span>{actionError}</span>
              <button onClick={() => setActionError(null)} aria-label="Cerrar">✕</button>
            </div>
          )}

          <div className="board-header">
            <button className="challenges-add-btn" onClick={() => setShowModal(true)}>
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
                    <BoardItem goal={goal} onSelect={setSelectedGoal} onComplete={handleComplete} />
                  </li>
                ))
              ) : (
                <li className="board-empty">Sembrá tu primer reto. ¡Crea uno!</li>
              )}

              {hasPending && hasCompleted && (
                <li className="board-divider" role="separator" />
              )}

              {hasCompleted && completados!.content.map(goal => (
                <li key={goal.id}>
                  <BoardItem goal={goal} onSelect={setSelectedGoal} />
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>
      </div>

      {selectedGoal && (
        <GoalDetailModal
          goal={selectedGoal}
          onClose={() => setSelectedGoal(null)}
          onEdit={goal => { setSelectedGoal(null); setEditingGoal(goal) }}
          onDelete={handleDelete}
          onComplete={handleComplete}
        />
      )}

      {showModal && (
        <GoalModal onClose={() => setShowModal(false)} onSubmit={handleCreate} />
      )}

      {editingGoal && (
        <GoalModal
          initialData={{ title: editingGoal.title, description: editingGoal.description ?? '' }}
          onClose={() => setEditingGoal(null)}
          onSubmit={handleEdit}
        />
      )}

      {harvestPlant !== null && (
        <HarvestModal
          plantNumber={harvestPlant}
          onViewGarden={() => setHarvestPlant(null)}
          onCreateNew={() => { setHarvestPlant(null); setShowModal(true) }}
        />
      )}
    </div>
  )
}
