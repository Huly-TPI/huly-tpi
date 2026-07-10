import type { RefObject } from 'react'
import type { PendingTaskResponse } from '../../api/pending'
import Postit from './Postit'
import boardBg from '../../assets/pending/board.png'

export interface BoardProps {
  boardRef: RefObject<HTMLDivElement>
  tasks: PendingTaskResponse[]
  followingTaskId: number | null
  recommendedTaskIds: Set<number>
  onPickUp: (taskId: number, origin?: { x: number; y: number }) => void
  onOpen: (task: PendingTaskResponse) => void
}

export default function Board({ boardRef, tasks, followingTaskId, recommendedTaskIds, onPickUp, onOpen }: BoardProps) {
  const visibleTasks = tasks.filter(task => task.id !== followingTaskId)

  return (
    <div
      ref={boardRef}
      aria-label="Tablero de pendientes"
      className="board-pending relative flex-1 min-h-[620px] touch-none md:min-h-[420px]"
    >
      <img
        src={boardBg}
        alt=""
        aria-hidden="true"
        className="pointer-events-none absolute inset-x-0 bottom-0 h-full w-full object-cover"
      />
      {visibleTasks.map((task, index) => (
        <div
          key={task.id}
          className="absolute"
          style={{
            left: `${task.positionX}%`,
            top: `${task.positionY}%`,
            transform: 'translate(-50%, -50%)',
            zIndex: index + 1,
          }}
        >
          <Postit
            task={task}
            isRecommended={recommendedTaskIds.has(task.id)}
            onPickUp={origin => onPickUp(task.id, origin)}
            onOpen={() => onOpen(task)}
          />
        </div>
      ))}
    </div>
  )
}
