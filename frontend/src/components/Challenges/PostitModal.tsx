import { useState } from 'react'
import type { FormEvent } from 'react'
import type { UserGoalResponse } from '../../api/userGoals'
import Button from '../Buttons/Button/Button'
import GoalDetailPanel from './GoalDetailPanel'
import challengeDetailBg from '../../assets/challenges/challenge-detail-bg.png'

export type PostitMode = 'create' | 'edit' | 'detail'

export interface PostitModalProps {
  initialMode: PostitMode
  goal?: UserGoalResponse
  onClose: () => void
  onCreate?: (data: { title: string; description: string }) => Promise<void>
  onUpdate?: (id: number, data: { title: string; description: string }) => Promise<void>
  onDelete?: (id: number) => Promise<void>
  onComplete?: (id: number, image?: File) => Promise<void>
}

const inputBase =
  'w-full box-border border-0 border-b border-b-[rgba(92,61,30,0.25)] bg-transparent px-0 py-1 font-sans text-[#3b2510] outline-none leading-[1.45] mb-[0.85rem] transition-[border-color] duration-150 [color-scheme:light]'

export default function PostitModal({
  initialMode,
  goal,
  onClose,
  onCreate,
  onUpdate,
  onDelete,
  onComplete,
}: PostitModalProps) {
  const [mode, setMode] = useState<PostitMode>(initialMode)
  const [title, setTitle] = useState(goal?.title ?? '')
  const [description, setDescription] = useState(goal?.description ?? '')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      if (mode === 'create' && onCreate) {
        await onCreate({ title: title.trim(), description: description.trim() })
      } else if (mode === 'edit' && onUpdate && goal) {
        await onUpdate(goal.id, { title: title.trim(), description: description.trim() })
      }
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error inesperado')
    } finally {
      setLoading(false)
    }
  }

  const enterEditMode = () => {
    setTitle(goal?.title ?? '')
    setDescription(goal?.description ?? '')
    setError(null)
    setMode('edit')
  }

  const handleCancel = () => {
    if (initialMode === 'detail') {
      setMode('detail')
    } else {
      onClose()
    }
  }

  return (
    <div className="fixed inset-0 bg-black/[0.42] flex items-center justify-center z-[1000] p-4 backdrop-blur-[3px]" role="dialog" aria-modal="true">
      <div
        className="postit-modal relative w-[300px] min-h-[200px] [background-size:100%_100%] bg-no-repeat pt-8 px-8 pb-[5rem] drop-shadow-[0_8px_24px_rgba(0,0,0,0.28)]"
        style={{ backgroundImage: `url(${challengeDetailBg})` }}
      >
        <button
          className="absolute top-[0.65rem] right-[0.75rem] bg-transparent border-0 cursor-pointer text-[0.85rem] text-[#7a5c38] opacity-60 p-[0.2rem] leading-none transition-opacity duration-150 hover:opacity-100"
          onClick={onClose}
          aria-label="Cerrar"
        >
          ✕
        </button>

        {mode === 'detail' && goal && (
          <GoalDetailPanel
            goal={goal}
            onClose={onClose}
            onComplete={onComplete}
            onEdit={!goal || goal.status === 'COMPLETED' ? undefined : enterEditMode}
            onDelete={onDelete}
          />
        )}

        {(mode === 'create' || mode === 'edit') && (
          <form onSubmit={handleSubmit} className="flex flex-col">
            <p className="text-[0.7rem] font-bold text-[rgba(92,61,30,0.5)] uppercase tracking-[0.07em] m-0 mb-4">
              {mode === 'create' ? 'Nuevo reto' : 'Editar reto'}
            </p>
            <label className="text-[0.65rem] font-bold text-[rgba(92,61,30,0.5)] uppercase tracking-[0.06em] block mb-[0.2rem]" htmlFor="postit-title">
              Título
            </label>
            <input
              id="postit-title"
              className={`postit-modal__input ${inputBase} text-base font-bold`}
              type="text"
              value={title}
              onChange={e => setTitle(e.target.value)}
              maxLength={255}
              required
              autoFocus
              placeholder="¿Qué querés lograr?"
            />
            <label className="text-[0.65rem] font-bold text-[rgba(92,61,30,0.5)] uppercase tracking-[0.06em] block mb-[0.2rem]" htmlFor="postit-desc">
              Descripción
            </label>
            <textarea
              id="postit-desc"
              className={`postit-modal__textarea ${inputBase} text-[0.85rem] resize-none`}
              value={description}
              onChange={e => setDescription(e.target.value)}
              maxLength={250}
              rows={3}
              placeholder="Describe tu reto (opcional)"
            />
            {error && (
              <p className="text-[0.78rem] text-[#9b2c2c] -mt-[0.3rem] mb-[0.5rem] bg-[rgba(229,62,62,0.08)] rounded p-[0.3rem_0.5rem]">
                {error}
              </p>
            )}
            <div className="flex flex-col gap-[0.45rem] mt-[0.8rem]">
              <Button
                type="submit"
                variant="primary"
                size="sm"
                fullWidth
                isLoading={loading}
                loadingLabel="Guardando..."
                disabled={!title.trim()}
              >
                Guardar
              </Button>
              <Button variant="secondary" size="sm" fullWidth onClick={handleCancel}>
                Cancelar
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
