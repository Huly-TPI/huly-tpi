import { useRef, useState } from 'react'
import { getBackendOrigin } from '../../api/client'
import type { UserGoalResponse } from '../../api/userGoals'
import Button from '../Buttons/Button/Button'
import seedIcon from '../../assets/rewards/seed.webp'

interface GoalDetailPanelProps {
  goal: UserGoalResponse
  onClose: () => void
  onComplete?: (id: number, image?: File) => Promise<void>
  onEdit?: () => void
  onDelete?: (id: number) => Promise<void>
}

export default function GoalDetailPanel({ goal, onClose, onComplete, onEdit, onDelete }: GoalDetailPanelProps) {
  const isCompleted = goal.status === 'COMPLETED'
  const [completeError, setCompleteError] = useState<string | null>(null)
  const [selectedImage, setSelectedImage] = useState<File | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const imageDisplayUrl = goal.imageUrl
    ? (goal.imageUrl.startsWith('http') ? goal.imageUrl : `${getBackendOrigin()}${goal.imageUrl}`)
    : null

  return (
    <>
      {isCompleted && (
        <span className="inline-block text-[0.7rem] font-bold text-bosque bg-bosque/[0.14] rounded-full px-[0.55rem] py-[0.12rem] mb-[0.45rem] tracking-[0.04em]">
          ✓ Completado
        </span>
      )}
      <h3 className={`text-[1.05rem] font-extrabold m-0 mb-[0.6rem] leading-[1.35] break-words ${isCompleted ? 'text-[#7a9c6e] line-through decoration-bosque/45' : 'text-[#3b2510]'}`}>
        {goal.title}
      </h3>
      {goal.description && (
        <p className="text-[0.82rem] text-[#5c4028] m-0 mb-[0.5rem] leading-[1.5] break-words">{goal.description}</p>
      )}
      {imageDisplayUrl && (
        <div className="mt-1 mb-3 rounded-lg overflow-hidden">
          <img
            src={imageDisplayUrl}
            alt="Foto del logro"
            className="w-full object-cover max-h-36 rounded-lg"
          />
        </div>
      )}
      {!isCompleted && onComplete && (
        <div className="mt-[0.8rem] mb-[0.3rem]">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={e => {
              const file = e.target.files?.[0] ?? null
              if (file && file.size > 5 * 1024 * 1024) {
                setCompleteError('La imagen no puede superar los 5 MB.')
                setSelectedImage(null)
                e.target.value = ''
                return
              }
              setCompleteError(null)
              setSelectedImage(file)
            }}
          />
          <p className="text-[0.65rem] font-bold text-[rgba(92,61,30,0.5)] uppercase tracking-[0.06em] m-0 mb-[0.35rem]">
            Foto del logro (opcional)
          </p>
          <div className="flex items-center gap-[0.5rem] mb-[0.4rem]">
            <span className="flex items-center gap-[0.2rem] text-[0.7rem] text-[#7a5c38]">
              Sin foto: <strong>{goal.coinsReward}</strong>
              <img src={seedIcon} alt="semillas" className="inline-block w-[0.85rem] h-[0.85rem] object-contain flex-shrink-0" />
            </span>
            <span className="text-[#c5a87a] text-[0.65rem]">·</span>
            <span className="flex items-center gap-[0.2rem] text-[0.7rem] font-bold text-[#8a6c2a]">
              Con foto: <strong>{goal.coinsRewardWithImage}</strong>
              <img src={seedIcon} alt="semillas" className="inline-block w-[0.85rem] h-[0.85rem] object-contain flex-shrink-0" />
            </span>
          </div>
          <button
            type="button"
            className="w-full text-left text-[0.75rem] text-[#5c4028] border border-dashed border-[rgba(92,61,30,0.35)] rounded-[6px] px-[0.6rem] py-[0.4rem] bg-transparent cursor-pointer hover:border-[rgba(92,61,30,0.6)] transition-colors duration-150 truncate"
            onClick={() => fileInputRef.current?.click()}
          >
            {selectedImage ? `📎 ${selectedImage.name}` : '+ Adjuntar imagen'}
          </button>
        </div>
      )}
      {completeError && (
        <p className="text-[0.73rem] text-[#9b2c2c] mt-[0.4rem] mb-[0.1rem] bg-[rgba(229,62,62,0.08)] rounded p-[0.3rem_0.5rem] leading-[1.4]">
          {completeError}
        </p>
      )}
      <div className="flex flex-col gap-[0.45rem] mt-[0.5rem]">
        {!isCompleted && onComplete && (
          <Button
            variant="primary"
            size="sm"
            fullWidth
            onClick={async () => {
              setCompleteError(null)
              await onComplete(goal.id, selectedImage ?? undefined)
              onClose()
            }}
            onAsyncError={err => setCompleteError(err instanceof Error ? err.message : 'Error inesperado')}
          >
            ✓ Completar
          </Button>
        )}
        {!isCompleted && onEdit && (
          <Button variant="secondary" size="sm" fullWidth onClick={onEdit}>
            ✎ Editar
          </Button>
        )}
        {onDelete && !isCompleted && (
          <Button
            variant="alert"
            size="sm"
            fullWidth
            onClick={async () => { await onDelete(goal.id); onClose() }}
            onAsyncError={() => {}}
          >
            Eliminar
          </Button>
        )}
      </div>
    </>
  )
}
