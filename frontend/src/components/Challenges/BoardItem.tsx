import { useState } from 'react'
import type { MouseEvent } from 'react'
import type { UserGoalResponse } from '../../api/userGoals'
import wateringCanImg from '../../assets/challenges/watering-can.png'

export interface BoardItemProps {
  goal: UserGoalResponse
  onSelect: (goal: UserGoalResponse) => void
  onComplete?: (id: number) => Promise<void>
  darkMode?: boolean
}

export default function BoardItem({ goal, onSelect, onComplete }: BoardItemProps) {
  const [completing, setCompleting] = useState(false)
  const isCompleted = goal.status === 'COMPLETED'

  const handleComplete = async (e: MouseEvent) => {
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
      className={[
        'flex items-center justify-between gap-2 py-2 pl-3 pr-2 rounded-[5px] w-full box-border cursor-pointer',
        'border transition-[background,box-shadow,transform] duration-150 relative',
        'hover:shadow-[0_2px_8px_rgba(92,61,30,0.15)] hover:translate-x-[2px]',
        'focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-violeta',
        isCompleted
          ? 'bg-[rgba(238,248,238,0.6)] border-[rgba(100,153,89,0.22)] hover:bg-[rgba(238,248,238,0.85)]'
          : 'bg-[rgba(255,248,230,0.68)] border-[rgba(140,95,40,0.18)] hover:bg-[rgba(255,248,230,0.9)]',
        completing ? 'board-item--completing' : '',
      ].join(' ')}
      onClick={() => onSelect(goal)}
      role="button"
      tabIndex={0}
      onKeyDown={e => e.key === 'Enter' && onSelect(goal)}
      aria-label={`Ver detalles: ${goal.title}`}
    >
      <span className={`board-item__title flex-1 text-[0.85rem] lg:text-[0.92rem] font-semibold leading-[1.35] break-words relative ${
        isCompleted ? 'text-[#7a9c6e]' : 'text-[#3b2510]'
      }`}>
        {goal.title}
      </span>

      {!isCompleted && onComplete && (
        <button
          className={[
            'flex-shrink-0 bg-transparent border-0 p-[0.15rem] cursor-pointer flex items-center justify-center rounded-[5px]',
            'transition-transform duration-150',
            !completing ? 'hover:-rotate-[14deg] hover:scale-110 active:-rotate-[24deg] active:scale-95' : '',
            'disabled:opacity-45 disabled:cursor-not-allowed',
          ].join(' ')}
          onClick={handleComplete}
          disabled={completing}
          aria-label="Completar reto"
          title="Completar reto"
        >
          <img
            src={wateringCanImg}
            alt=""
            aria-hidden="true"
            className={`w-10 h-10 object-contain drop-shadow-[0_1px_3px_rgba(0,0,0,0.14)] transition-transform duration-200${completing ? ' board-item__can--pouring' : ''}`}
          />
        </button>
      )}

      {isCompleted && (
        <span className="flex-shrink-0 text-[0.9rem] font-bold w-10 text-center text-bosque" aria-label="Completado">
          ✓
        </span>
      )}
    </div>
  )
}
