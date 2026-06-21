import { useEffect, useRef } from 'react'
import { CircleDollarSign, X } from 'lucide-react'

interface RewardToastProps {
  /** Monedas acreditadas a mostrar. */
  coins: number
  /** Se llama al cerrar (manual o tras el auto-cierre). */
  onClose: () => void
  /** Mensaje principal del toast. */
  message?: string
}

/** Toast dorado de "recompensa reclamada" (usado por daily reward y comeback). Se auto-cierra a los 3.5s. */
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
      className="fixed bottom-24 right-5 z-50 flex items-center gap-3 rounded-2xl bg-gradient-to-r from-[#b8860b] to-[#e6b800] px-5 py-4 shadow-xl text-white"
    >
      <CircleDollarSign className="w-7 h-7 text-white" strokeWidth={2} aria-hidden="true" />
      <div>
        <p className="text-sm opacity-90 m-0">{message}</p>
        <p className="font-bold text-lg m-0">+{coins} monedas</p>
      </div>
      <button
        aria-label="Cerrar"
        onClick={onClose}
        className="ml-1 rounded-full p-1.5 hover:bg-white/20 transition bg-transparent border-0 cursor-pointer text-white"
      >
        <X className="w-4 h-4" strokeWidth={2} />
      </button>
    </div>
  )
}
