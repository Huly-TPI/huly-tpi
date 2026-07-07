import { useState } from 'react'
import { X } from 'lucide-react'

export interface SubtaskItem {
  id: string | number
  text: string
  done: boolean
}

export interface SubtasksFieldProps {
  items: SubtaskItem[]
  onAdd: (text: string) => void
  onToggle?: (id: string | number) => void
  onDelete: (id: string | number) => void
  readOnly?: boolean
}

export default function SubtasksField({ items, onAdd, onToggle, onDelete, readOnly = false }: SubtasksFieldProps) {
  const [draft, setDraft] = useState('')

  const handleAdd = () => {
    const text = draft.trim()
    if (!text) return
    onAdd(text)
    setDraft('')
  }

  return (
    <div className="mt-2">
      {!readOnly && (
        <>
          <label className="text-[0.65rem] font-bold uppercase tracking-[0.06em] text-[rgba(92,61,30,0.5)]">
            Subtareas
          </label>
          <div className="mt-1 flex gap-1">
            <input
              value={draft}
              onChange={e => setDraft(e.target.value)}
              onKeyDown={e => {
                if (e.key === 'Enter') {
                  e.preventDefault()
                  handleAdd()
                }
              }}
              placeholder="Agregar subtarea"
              aria-label="Nueva subtarea"
              className="flex-1 rounded border border-[rgba(92,61,30,0.25)] bg-transparent px-2 py-1 text-xs text-[#3b2510]"
            />
            <button
              type="button"
              onClick={handleAdd}
              aria-label="Agregar subtarea"
              className="rounded bg-violeta px-2 text-xs text-white"
            >
              +
            </button>
          </div>
        </>
      )}
      <ul className="mt-2 flex flex-col gap-1">
        {items.map(item => (
          <li key={item.id} className="flex items-center gap-2 text-xs text-[#3b2510]">
            <input
              type="checkbox"
              checked={item.done}
              disabled={!onToggle}
              onChange={() => onToggle?.(item.id)}
              aria-label={`Marcar ${item.text}`}
              className="accent-bosque"
            />
            <span className={`flex-1 ${item.done ? 'line-through text-[#7a5c38]' : ''}`}>{item.text}</span>
            {!readOnly && (
              <button type="button" onClick={() => onDelete(item.id)} aria-label={`Eliminar ${item.text}`}>
                <X className="h-3 w-3" />
              </button>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
