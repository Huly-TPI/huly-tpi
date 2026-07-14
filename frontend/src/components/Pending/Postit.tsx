import { useRef } from 'react'
import { Calendar, Clock, ListChecks, Tag } from 'lucide-react'
import type { PendingTaskResponse } from '../../api/pending'
import RecommendedBadge from './RecommendedBadge'
import pinIcon from '../../assets/pending/pin.png'

const LONG_PRESS_MS = 400
const MOVE_CANCEL_THRESHOLD_PX = 10

export interface PostitProps {
  task: PendingTaskResponse
  isRecommended: boolean
  onPickUp: (origin: { x: number; y: number }) => void
  onOpen: () => void
  style?: React.CSSProperties
  className?: string
}

export default function Postit({ task, isRecommended, onPickUp, onOpen, style, className }: PostitProps) {
  const longPressTimer = useRef<ReturnType<typeof setTimeout> | null>(null)
  const pressOrigin = useRef<{ x: number; y: number } | null>(null)
  const longPressFired = useRef(false)

  const clearTimer = () => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current)
      longPressTimer.current = null
    }
  }

  const handlePointerDown = (event: React.PointerEvent) => {
    pressOrigin.current = { x: event.clientX, y: event.clientY }
    longPressFired.current = false
    longPressTimer.current = setTimeout(() => {
      longPressFired.current = true
      if (pressOrigin.current) {
        onPickUp(pressOrigin.current)
      }
    }, LONG_PRESS_MS)
  }

  const handlePointerMove = (event: React.PointerEvent) => {
    if (!pressOrigin.current) return
    const dx = event.clientX - pressOrigin.current.x
    const dy = event.clientY - pressOrigin.current.y
    if (Math.hypot(dx, dy) > MOVE_CANCEL_THRESHOLD_PX) {
      clearTimer()
    }
  }

  const handlePointerUp = () => {
    clearTimer()
  }

  const handleClick = () => {
    if (longPressFired.current) {
      longPressFired.current = false
      return
    }
    onOpen()
  }

  const rotation = task.rotationDeg ?? 0
  const dueLabel = task.dueDate ? `Vence: ${task.dueDate}` : null
  const doneSubtasks = task.subtasks.filter(subtask => subtask.done).length

  return (
    <div
      style={{ ...style, transform: `${style?.transform ?? ''} rotate(${rotation}deg)` }}
      className={`postit relative w-28 min-h-[92px] p-2 md:w-36 md:min-h-[110px] md:p-3 rounded-md bg-[#fff3c4] shadow-[0_6px_14px_rgba(0,0,0,0.22)] cursor-pointer select-none ${task.status === 'COMPLETED' ? 'opacity-60' : ''} ${className ?? ''}`}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onPointerCancel={handlePointerUp}
      onClick={handleClick}
      role="button"
      tabIndex={0}
      aria-label={isRecommended ? `${task.title}, recomendado hoy` : task.title}
    >
      {task.pinnedAt && (
        <img
          src={pinIcon}
          alt=""
          aria-hidden="true"
          data-testid="postit-pin"
          className="absolute -top-4 left-1/2 h-6 w-6 -translate-x-1/2 drop-shadow"
        />
      )}
      {isRecommended && <RecommendedBadge />}
      <p className={`text-sm font-bold text-[#3b2510] ${task.status === 'COMPLETED' ? 'line-through' : ''}`}>
        {task.title}
      </p>
      {task.description && (
        <p className="mt-1 text-xs text-[#5c3d1e] line-clamp-2">{task.description}</p>
      )}
      <div className="mt-2 flex items-center gap-2 text-[#7a5c38]">
        {task.dueDate && <Calendar aria-label={dueLabel ?? undefined} className="h-3.5 w-3.5" />}
        {task.estimatedDuration && <Clock className="h-3.5 w-3.5" />}
        {task.category && <Tag className="h-3.5 w-3.5" />}
        {task.subtasks.length > 0 && (
          <span className="flex items-center gap-0.5 text-[11px]">
            <ListChecks className="h-3.5 w-3.5" />
            {doneSubtasks}/{task.subtasks.length}
          </span>
        )}
      </div>
    </div>
  )
}
