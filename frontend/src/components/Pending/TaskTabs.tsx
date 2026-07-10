import { Calendar, Clock, ListChecks, Tag, type LucideIcon } from 'lucide-react'
import Tooltip from './Tooltip'

export type TaskTabKey = 'date' | 'duration' | 'category' | 'subtasks'

export interface TaskTabsProps {
  activeTab: TaskTabKey | null
  onTabChange: (tab: TaskTabKey | null) => void
  hasValue: Record<TaskTabKey, boolean>
}

const TABS: { key: TaskTabKey; icon: LucideIcon; label: string }[] = [
  { key: 'date', icon: Calendar, label: 'Fecha límite' },
  { key: 'duration', icon: Clock, label: 'Duración estimada' },
  { key: 'category', icon: Tag, label: 'Categoría' },
  { key: 'subtasks', icon: ListChecks, label: 'Subtareas' },
]

export default function TaskTabs({ activeTab, onTabChange, hasValue }: TaskTabsProps) {
  return (
    <div className="flex items-center gap-2 border-t border-[rgba(92,61,30,0.15)] dark:border-slate-800 pt-2 mt-2">
      {TABS.map(({ key, icon: Icon, label }) => {
        const isActive = activeTab === key
        return (
          <Tooltip key={key} label={label}>
            <button
              type="button"
              aria-pressed={isActive}
              aria-label={label}
              onClick={() => onTabChange(isActive ? null : key)}
              className={`relative flex h-9 w-9 items-center justify-center rounded-full transition-colors ${
                isActive ? 'bg-violeta text-white' : 'bg-[rgba(92,61,30,0.08)] dark:bg-slate-800 text-[#5c3d1e] dark:text-slate-300'
              }`}
            >
              <Icon className="h-4 w-4" />
              {hasValue[key] && (
                <span aria-hidden="true" className="absolute -top-0.5 -right-0.5 h-2 w-2 rounded-full bg-bosque" />
              )}
            </button>
          </Tooltip>
        )
      })}
    </div>
  )
}
