import { useEffect, useRef } from 'react'
import { X } from 'lucide-react'
import seedIcon from '../../assets/rewards/seed.webp'

interface RewardToastProps {
  coins: number
  onClose: () => void
  message?: string
}

export function RewardToast({ coins, onClose, message = '¡Recompensa reclamada!' }: RewardToastProps) {
  const onCloseRef = useRef(onClose)
  onCloseRef.current = onClose

  useEffect(() => {
    const id = setTimeout(() => onCloseRef.current(), 3500)
    return () => clearTimeout(id)
  }, [coins])

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed bottom-24 right-5 max-[640px]:right-4 max-[640px]:left-4 z-[60] flex items-center justify-between gap-3 rounded-2xl bg-[#f3d8aa]/95 border border-[#b98a54] px-4 py-3 shadow-[0_6px_12px_rgba(91,60,24,0.25),inset_0_1px_0_rgba(255,255,255,0.45)] text-[#6b4a2a]"
    >
      <div className="flex items-center gap-2">
        <img
          src={seedIcon}
          alt=""
          aria-hidden="true"
          className="w-10 h-10 object-contain shrink-0"
        />
        <div>
          <p className="text-[12px] font-black m-0 leading-none">{message}</p>
          <p className="text-[15px] font-black m-0 mt-1 leading-none">
            +<span className="text-[#4d8a2d]">{coins} semillas</span>
          </p>
        </div>
      </div>
      <button
        aria-label="Cerrar notificación"
        onClick={onClose}
        className="rounded-full p-1.5 bg-[#d9b77e]/60 hover:bg-[#c99d61]/70 text-[#7b5c3c] transition"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  )
}
