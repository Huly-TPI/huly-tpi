import { useState, type FormEvent } from 'react'
import type { PendingCategory, EstimatedDuration, PendingTaskResponse } from '../../api/pending'
import Button from '../Buttons/Button/Button'
import TaskTabs, { type TaskTabKey } from './TaskTabs'
import DueDateField from './DueDateField'
import DurationField from './DurationField'
import CategoryField from './CategoryField'
import SubtasksField, { type SubtaskItem } from './SubtasksField'
import TaskDetailView from './TaskDetailView'

export interface PendingTaskFormData {
  title: string
  description?: string
  dueDate?: string
  estimatedDuration?: EstimatedDuration
  category?: PendingCategory
  subtasks?: string[]
}

export interface PendingFormModalProps {
  mode: 'create' | 'edit'
  task?: PendingTaskResponse
  onClose: () => void
  onSubmit: (data: PendingTaskFormData) => Promise<void>
  onDelete?: () => Promise<void>
  onComplete?: () => Promise<void>
  onAddSubtask?: (text: string) => Promise<void>
  onToggleSubtask?: (subtaskId: number) => Promise<void>
  onDeleteSubtask?: (subtaskId: number) => Promise<void>
}

function fieldsFromTask(task?: PendingTaskResponse) {
  return {
    title: task?.title ?? '',
    description: task?.description ?? '',
    dueDate: task?.dueDate ?? '',
    estimatedDuration: task?.estimatedDuration ?? undefined,
    category: task?.category ?? undefined,
  }
}

export default function PendingFormModal({
  mode,
  task,
  onClose,
  onSubmit,
  onDelete,
  onComplete,
  onAddSubtask,
  onToggleSubtask,
  onDeleteSubtask,
}: PendingFormModalProps) {
  const [isEditing, setIsEditing] = useState(mode === 'create')
  const [title, setTitle] = useState(fieldsFromTask(task).title)
  const [description, setDescription] = useState(fieldsFromTask(task).description)
  const [dueDate, setDueDate] = useState(fieldsFromTask(task).dueDate)
  const [estimatedDuration, setEstimatedDuration] = useState<EstimatedDuration | undefined>(fieldsFromTask(task).estimatedDuration)
  const [category, setCategory] = useState<PendingCategory | undefined>(fieldsFromTask(task).category)
  const [draftSubtasks, setDraftSubtasks] = useState<string[]>([])
  const [activeTab, setActiveTab] = useState<TaskTabKey | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleCancelEdit = () => {
    const fields = fieldsFromTask(task)
    setTitle(fields.title)
    setDescription(fields.description)
    setDueDate(fields.dueDate)
    setEstimatedDuration(fields.estimatedDuration)
    setCategory(fields.category)
    setActiveTab(null)
    setError(null)
    if (mode === 'edit') {
      setIsEditing(false)
    } else {
      onClose()
    }
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await onSubmit({
        title: title.trim(),
        description: description.trim() || undefined,
        dueDate: dueDate || undefined,
        estimatedDuration,
        category,
        subtasks: mode === 'create' ? draftSubtasks : undefined,
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error inesperado')
    } finally {
      setLoading(false)
    }
  }

  const subtaskItems: SubtaskItem[] =
    mode === 'edit' && task
      ? task.subtasks.map(subtask => ({ id: subtask.id, text: subtask.text, done: subtask.done }))
      : draftSubtasks.map((text, index) => ({ id: index, text, done: false }))

  return (
    <div className="fixed inset-0 bg-black/[0.42] flex items-center justify-center z-[1000] p-4 backdrop-blur-[3px]" role="dialog" aria-modal="true">
      <div className="relative w-[320px] rounded-lg bg-[#fff8e7] p-6 shadow-[0_8px_24px_rgba(0,0,0,0.28)]">
        <button
          className="absolute top-2 right-3 bg-transparent border-0 cursor-pointer text-[0.85rem] text-[#7a5c38] opacity-60 hover:opacity-100"
          onClick={onClose}
          aria-label="Cerrar"
        >
          ✕
        </button>

        {mode === 'edit' && !isEditing && task ? (
          <TaskDetailView
            task={task}
            onEdit={() => setIsEditing(true)}
            onClose={onClose}
            onDelete={async () => {
              await onDelete?.()
            }}
            onComplete={onComplete ? async () => onComplete() : undefined}
            onToggleSubtask={onToggleSubtask}
          />
        ) : (
          <form onSubmit={handleSubmit} className="flex flex-col">
            <p className="text-[0.8rem] font-bold text-[rgba(92,61,30,0.8)] uppercase tracking-[0.07em] mb-3">
              {mode === 'create' ? 'Nueva tarea' : 'Editar tarea'}
            </p>

            <label className="text-[0.75rem] font-bold uppercase text-[rgba(92,61,30,0.8)]" htmlFor="pending-title">
              Título
            </label>
            <input
              id="pending-title"
              className="mb-3 border-0 border-b border-b-[rgba(92,61,30,0.25)] bg-transparent px-0 py-1 text-base font-bold text-[#3b2510] outline-none"
              value={title}
              onChange={e => setTitle(e.target.value)}
              maxLength={120}
              required
              autoFocus
              placeholder="¿Qué necesitás hacer?"
            />

            <label className="text-[0.75rem] font-bold uppercase text-[rgba(92,61,30,0.8)]" htmlFor="pending-description">
              Descripción
            </label>
            <textarea
              id="pending-description"
              className="mb-2 border-0 border-b border-b-[rgba(92,61,30,0.25)] bg-transparent px-0 py-1 text-[0.85rem] text-[#3b2510] outline-none resize-none"
              value={description}
              onChange={e => setDescription(e.target.value)}
              maxLength={1000}
              rows={3}
              placeholder="Describí tu tarea (opcional)"
            />

            <TaskTabs
              activeTab={activeTab}
              onTabChange={setActiveTab}
              hasValue={{
                date: Boolean(dueDate),
                duration: Boolean(estimatedDuration),
                category: Boolean(category),
                subtasks: subtaskItems.length > 0,
              }}
            />

            {activeTab === 'date' && <DueDateField value={dueDate} onChange={setDueDate} />}
            {activeTab === 'duration' && <DurationField value={estimatedDuration} onChange={setEstimatedDuration} />}
            {activeTab === 'category' && <CategoryField value={category} onChange={setCategory} />}
            {activeTab === 'subtasks' && (
              <SubtasksField
                items={subtaskItems}
                onAdd={text => {
                  if (mode === 'edit') {
                    void onAddSubtask?.(text)
                  } else {
                    setDraftSubtasks(prev => [...prev, text])
                  }
                }}
                onToggle={mode === 'edit' ? id => void onToggleSubtask?.(Number(id)) : undefined}
                onDelete={id => {
                  if (mode === 'edit') {
                    void onDeleteSubtask?.(Number(id))
                  } else {
                    setDraftSubtasks(prev => prev.filter((_, index) => index !== id))
                  }
                }}
              />
            )}

            {error && (
              <p className="mt-2 rounded bg-[rgba(229,62,62,0.08)] p-2 text-[0.78rem] text-[#9b2c2c]">{error}</p>
            )}

            <div className="mt-4 flex flex-col gap-2">
              <Button type="submit" variant="primary" size="sm" fullWidth isLoading={loading} loadingLabel="Guardando..." disabled={!title.trim()}>
                Listo
              </Button>
              <Button variant="secondary" size="sm" fullWidth onClick={handleCancelEdit}>
                Cancelar
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
