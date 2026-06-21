import { useEffect } from 'react'
import { createPortal } from 'react-dom'
import { AlertTriangle, X } from 'lucide-react'

interface ToastProps {
  message: string
  onClose: () => void
  duration?: number
}

export function Toast({ message, onClose, duration = 6000 }: ToastProps) {
  useEffect(() => {
    const t = setTimeout(onClose, duration)
    return () => clearTimeout(t)
  }, [onClose, duration])

  return createPortal(
    <div className="fixed bottom-5 right-5 z-[9999] flex w-80 items-start gap-3 rounded-2xl bg-white px-4 py-3.5 shadow-xl ring-1 ring-[#D1CAEF] dark:bg-[#172033] dark:shadow-none dark:ring-violet-900/40">
      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-violeta">
        <AlertTriangle className="h-4 w-4 text-white" strokeWidth={2} />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-sm font-bold text-violeta dark:text-violeta-claro">Error</p>
        <p className="mt-0.5 break-words text-xs text-[#4A5568] dark:text-gray-300">{message}</p>
      </div>
      <button
        onClick={onClose}
        className="mt-0.5 shrink-0 text-[#A0AEC0] transition-colors hover:text-violeta dark:text-gray-500 dark:hover:text-violeta-claro"
        aria-label="Cerrar"
      >
        <X className="h-4 w-4" strokeWidth={2} />
      </button>
    </div>,
    document.body
  )
}
