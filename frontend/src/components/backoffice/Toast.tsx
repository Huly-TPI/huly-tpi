import { useEffect } from 'react'
import { createPortal } from 'react-dom'

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
    <div className="fixed bottom-5 right-5 z-[9999] flex w-80 items-start gap-3 rounded-2xl bg-white dark:bg-[#172033] px-4 py-3.5 shadow-xl dark:shadow-none ring-1 ring-[#D1CAEF] dark:ring-violet-900/40">
      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#8869AC]">
        <svg className="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m0 3.75h.007M10.29 3.86L1.82 18a2.25 2.25 0 001.95 3.375h16.46a2.25 2.25 0 001.95-3.375L13.71 3.86a2.25 2.25 0 00-3.9 0z" />
        </svg>
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-bold text-[#8869AC] dark:text-[#A78BFA]">Error</p>
        <p className="mt-0.5 text-xs text-[#4A5568] dark:text-gray-300 break-words">{message}</p>
      </div>
      <button
        onClick={onClose}
        className="shrink-0 mt-0.5 text-[#A0AEC0] dark:text-gray-500 hover:text-[#8869AC] dark:hover:text-[#A78BFA] transition-colors"
        aria-label="Cerrar"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>,
    document.body
  )
}
