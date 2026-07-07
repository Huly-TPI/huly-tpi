import { useState } from 'react'
import { Calendar, Clock, Tag } from 'lucide-react'
import type { PendingTaskResponse } from '../../api/pending'
import Button from '../Buttons/Button/Button'
import SubtasksField, { type SubtaskItem } from './SubtasksField'
import { CATEGORY_OPTIONS } from './CategoryField'
import { DURATION_OPTIONS } from './DurationField'

export interface TaskDetailViewProps {
  task: PendingTaskResponse
  onEdit: () => void
  onClose: () => void
  onDelete: () => Promise<void>
  onComplete?: () => Promise<void>
  onToggleSubtask?: (subtaskId: number) => Promise<void>
}

function formatDueDate(dueDate: string): string {
  const [year, month, day] = dueDate.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString('es-AR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

export default function TaskDetailView({ task, onEdit, onClose, onDelete, onComplete, onToggleSubtask }: TaskDetailViewProps) {
  const [confirmingDelete, setConfirmingDelete] = useState(false)

  const categoryLabel = CATEGORY_OPTIONS.find(option => option.value === task.category)?.label
  const durationLabel = DURATION_OPTIONS.find(option => option.value === task.estimatedDuration)?.label
  const subtaskItems: SubtaskItem[] = task.subtasks.map(subtask => ({ id: subtask.id, text: subtask.text, done: subtask.done }))

  return (
    <div className="flex flex-col">
      <p className="text-[0.7rem] font-bold text-[rgba(92,61,30,0.5)] uppercase tracking-[0.07em] mb-3">Tarea</p>

      <p className="mb-1 text-base font-bold text-[#3b2510]">{task.title}</p>

      {task.description && <p className="mb-2 text-[0.85rem] text-[#5c3d1e]">{task.description}</p>}

      {task.dueDate && (
        <div className="mt-2 flex items-center gap-2 text-sm text-[#3b2510]">
          <Calendar className="h-4 w-4 text-[#7a5c38]" aria-hidden="true" />
          <span>{formatDueDate(task.dueDate)}</span>
        </div>
      )}

      {durationLabel && (
        <div className="mt-2 flex items-center gap-2 text-sm text-[#3b2510]">
          <Clock className="h-4 w-4 text-[#7a5c38]" aria-hidden="true" />
          <span>{durationLabel}</span>
        </div>
      )}

      {categoryLabel && (
        <div className="mt-2 flex items-center gap-2 text-sm text-[#3b2510]">
          <Tag className="h-4 w-4 text-[#7a5c38]" aria-hidden="true" />
          <span>{categoryLabel}</span>
        </div>
      )}

      {subtaskItems.length > 0 && (
        <SubtasksField
          items={subtaskItems}
          readOnly
          onAdd={() => {}}
          onDelete={() => {}}
          onToggle={onToggleSubtask ? id => void onToggleSubtask(Number(id)) : undefined}
        />
      )}

      {confirmingDelete ? (
        <div className="mt-4 flex flex-col gap-2">
          <p className="text-center text-[0.8rem] text-[#3b2510]">¿Eliminar esta tarea?</p>
          <Button variant="alert" size="sm" fullWidth onClick={onDelete}>
            Sí, eliminar
          </Button>
          <Button variant="secondary" size="sm" fullWidth onClick={() => setConfirmingDelete(false)}>
            Cancelar
          </Button>
        </div>
      ) : (
        <div className="mt-4 flex flex-col gap-2">
          {task.status !== 'COMPLETED' && (
            <Button variant="secondary" size="sm" fullWidth onClick={onEdit}>
              Editar
            </Button>
          )}
          {onComplete && task.status !== 'COMPLETED' && (
            <Button variant="success" size="sm" fullWidth onClick={onComplete}>
              Completar
            </Button>
          )}
          <Button variant="alert" size="sm" fullWidth onClick={() => setConfirmingDelete(true)}>
            Eliminar
          </Button>
          <Button variant="tertiary" size="sm" fullWidth onClick={onClose}>
            Cerrar
          </Button>
        </div>
      )}
    </div>
  )
}
