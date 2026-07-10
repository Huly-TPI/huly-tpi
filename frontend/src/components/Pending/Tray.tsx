import type { PendingTaskResponse } from '../../api/pending'
import RecommendationTriggerButton from './RecommendationTriggerButton'
import { useAuthGate } from '../../context/authGate'

export interface TrayProps {
  tasks: PendingTaskResponse[]
  onPickUp: (taskId: number) => void
  onCreateNew: () => void
  onGenerateRecommendation: () => Promise<void>
}

export default function Tray({ tasks, onPickUp, onCreateNew, onGenerateRecommendation }: TrayProps) {
  const { requireAuth } = useAuthGate()

  return (
    <aside className="tray-pending flex md:w-56 w-full flex-row md:flex-col gap-2 overflow-x-auto md:overflow-y-auto md:overflow-x-hidden p-2">
      <div className="flex flex-shrink-0 justify-center md:justify-start">
        <RecommendationTriggerButton onClick={async () => requireAuth(() => void onGenerateRecommendation())} />
      </div>

      <button
        type="button"
        onClick={() => requireAuth(onCreateNew)}
        className="flex-shrink-0 rounded-full bg-violeta px-4 py-2 text-sm font-semibold text-white shadow-md transition-transform hover:scale-105"
      >
        + Nueva tarea
      </button>

      {tasks.length === 0 && (
        <p className="text-sm font-semibold italic text-[#7a5c38] px-2">
          No tenés tareas sin ubicar — ¡creá una nueva!
        </p>
      )}

      {tasks.map(task => (
        <button
          key={task.id}
          type="button"
          onClick={() => requireAuth(() => onPickUp(task.id))}
          className="flex-shrink-0 w-32 md:w-full rounded-md bg-[#fff3c4] p-2 text-left shadow-sm hover:shadow-md transition-shadow"
        >
          <p className="text-xs font-bold text-[#3b2510] truncate">{task.title}</p>
          {task.description && (
            <p className="text-[11px] font-medium text-[#5c3d1e] line-clamp-2">{task.description}</p>
          )}
        </button>
      ))}
    </aside>
  )
}
