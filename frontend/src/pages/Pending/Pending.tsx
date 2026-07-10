import { useCallback, useRef, useState } from 'react'
import BackButton from '../../components/Buttons/BackButton/BackButton'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import Board from '../../components/Pending/Board'
import Tray from '../../components/Pending/Tray'
import PostitFollowLayer from '../../components/Pending/PostitFollowLayer'
import PendingFormModal, { type PendingTaskFormData } from '../../components/Pending/PendingFormModal'
import RecommendationBanner from '../../components/Pending/RecommendationBanner'
import { usePendingTasks } from '../../hooks/usePendingTasks'
import { usePendingRecommendation } from '../../hooks/usePendingRecommendation'
import { usePostitDrag } from '../../hooks/usePostitDrag'
import { useToast } from '../../context/toast'
import dayBackground from '../../assets/shared/day-background.webp'
import nightBackground from '../../assets/shared/dark-background.webp'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'
import { ActivityType } from '../../api/activities'
import './Pending.css'

type ModalState = { mode: 'create' } | { mode: 'edit'; taskId: number } | null

export default function Pending() {
  const {
    tasks,
    unplacedTasks,
    placedTasks,
    createTask,
    updateTask,
    deleteTask,
    completeTask,
    addSubtask,
    toggleSubtask,
    deleteSubtask,
    placeTask,
  } = usePendingTasks()
  const {
    recommendation,
    hasUnrespondedRecommendation,
    recommendedTaskIds,
    fetchRecommendation,
    accept,
    reject,
    requestOnDemand,
  } = usePendingRecommendation()
  const { showToast } = useToast()
  const [modal, setModal] = useState<ModalState>(null)
  const boardRef = useRef<HTMLDivElement>(null)
  const followLayerRef = useRef<HTMLDivElement>(null)

  const handlePlace = useCallback(
    (taskId: number, x: number, y: number) => {
      void placeTask(taskId, x, y).then(() => fetchRecommendation())
    },
    [placeTask, fetchRecommendation],
  )

  const { dragState, pickUp } = usePostitDrag(boardRef, handlePlace, followLayerRef)
  const { markConditionMet, saveSession } = useActivitySessionTracker(ActivityType.PENDING, {
    autoStart: true,
  })

  const handleSubmit = async (data: PendingTaskFormData) => {
    if (modal?.mode === 'create') {
      const created = await createTask(data)
      setModal(null)
      pickUp(created.id)
    } else if (modal?.mode === 'edit') {
      await updateTask(modal.taskId, data)
      setModal(null)
      void fetchRecommendation()
    }
  }

  const handleDelete = async () => {
    if (modal?.mode !== 'edit') return
    try {
      await deleteTask(modal.taskId)
      setModal(null)
      void fetchRecommendation()
    } catch {
      showToast('No se pudo eliminar la tarea', 'error')
    }
  }

  const handleComplete = async () => {
    if (modal?.mode !== 'edit') return
    try {
      await completeTask(modal.taskId)
      markConditionMet()
      await saveSession()
      setModal(null)
      void fetchRecommendation()
    } catch {
      showToast('No se pudo completar la tarea', 'error')
    }
  }

  const handleGenerateRecommendation = async () => {
    try {
      const produced = await requestOnDemand()
      if (!produced) {
        showToast('No hay suficientes tareas pendientes para recomendar una combinación', 'info')
      }
    } catch {
      showToast('No se pudo generar la recomendación', 'error')
    }
  }

  const editingTask = modal?.mode === 'edit' ? tasks.find(task => task.id === modal.taskId) : undefined
  const followingTask = dragState.mode === 'following' ? tasks.find(task => task.id === dragState.taskId) : undefined
  const recommendedTaskTitles = recommendation
    ? tasks.filter(task => recommendation.recommendedTaskIds.includes(task.id)).map(task => task.title)
    : []

  return (
    <div className="pending-page relative flex min-h-full flex-col px-2 pt-16 pb-0 sm:px-4">
      <ThemeBackground
        lightSrc={dayBackground}
        darkSrc={nightBackground}
        lightAlt="Fondo de pendientes"
        darkAlt="Fondo nocturno de pendientes"
      />
      <BackButton to="/" />

      {hasUnrespondedRecommendation && (
        <RecommendationBanner taskTitles={recommendedTaskTitles} onAccept={accept} onReject={reject} />
      )}

      <div className="relative z-10 flex flex-1 flex-col gap-3 md:flex-row">
        <Tray
          tasks={unplacedTasks}
          onPickUp={pickUp}
          onCreateNew={() => setModal({ mode: 'create' })}
          onGenerateRecommendation={handleGenerateRecommendation}
        />
        <Board
          boardRef={boardRef}
          tasks={placedTasks}
          followingTaskId={dragState.mode === 'following' ? dragState.taskId : null}
          recommendedTaskIds={recommendedTaskIds}
          onPickUp={pickUp}
          onOpen={task => setModal({ mode: 'edit', taskId: task.id })}
        />
      </div>

      {dragState.mode === 'following' && followingTask && (
        <PostitFollowLayer ref={followLayerRef} task={followingTask} x={dragState.x} y={dragState.y} />
      )}

      <p className="pending-page__hint">Mantené presionado un pendiente para moverlo por el tablero</p>

      {modal && (
        <PendingFormModal
          mode={modal.mode}
          task={editingTask}
          onClose={() => setModal(null)}
          onSubmit={handleSubmit}
          onDelete={modal.mode === 'edit' ? handleDelete : undefined}
          onComplete={modal.mode === 'edit' ? handleComplete : undefined}
          onAddSubtask={
            modal.mode === 'edit'
              ? async text => {
                  await addSubtask(modal.taskId, text)
                  void fetchRecommendation()
                }
              : undefined
          }
          onToggleSubtask={
            modal.mode === 'edit'
              ? async subtaskId => {
                  await toggleSubtask(modal.taskId, subtaskId)
                  void fetchRecommendation()
                }
              : undefined
          }
          onDeleteSubtask={
            modal.mode === 'edit'
              ? async subtaskId => {
                  await deleteSubtask(modal.taskId, subtaskId)
                  void fetchRecommendation()
                }
              : undefined
          }
        />
      )}
    </div>
  )
}
