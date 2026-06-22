import { useEffect, useState, useRef, useCallback } from 'react'
import { createPortal } from 'react-dom'
import { AlertTriangle, CheckCircle2, Info, X } from 'lucide-react'

export type ToastType = 'error' | 'success' | 'warning' | 'info'

interface ToastProps {
  message: string
  type?: ToastType
  onClose: () => void
  duration?: number
  isPortal?: boolean
}

export function Toast({
  message,
  type = 'error',
  onClose,
  duration = 6000,
  isPortal = true,
}: ToastProps) {
  const [isExiting, setIsExiting] = useState(false)

  const onCloseRef = useRef(onClose)
  useEffect(() => {
    onCloseRef.current = onClose
  }, [onClose])

  const handleClose = useCallback(() => {
    setIsExiting(true)
    setTimeout(() => {
      onCloseRef.current()
    }, 300)
  }, [])

  useEffect(() => {
    if (isExiting) return
    const t = setTimeout(handleClose, duration)
    return () => clearTimeout(t)
  }, [handleClose, duration, isExiting])

  const typeConfig = {
    error: {
      title: 'Error',
      icon: AlertTriangle,
      colorClass: 'text-red-700 dark:text-red-400',
      bgClass: 'bg-red-50 dark:bg-red-955/20',
      ringClass: 'ring-red-200 dark:ring-red-900/30',
      iconBg: 'bg-red-500',
      iconColor: 'text-white',
    },
    success: {
      title: 'Éxito',
      icon: CheckCircle2,
      colorClass: 'text-bosque dark:text-menta',
      bgClass: 'bg-[#EBF7EE] dark:bg-[#12231b]',
      ringClass: 'ring-bosque/30 dark:ring-menta/20',
      iconBg: 'bg-bosque',
      iconColor: 'text-white',
    },
    warning: {
      title: 'Advertencia',
      icon: AlertTriangle,
      colorClass: 'text-anaranjado dark:text-[#f3a462]',
      bgClass: 'bg-[#FFF7ED] dark:bg-[#25180f]',
      ringClass: 'ring-anaranjado/30 dark:ring-[#9c5312]/20',
      iconBg: 'bg-anaranjado',
      iconColor: 'text-white',
    },
    info: {
      title: 'Información',
      icon: Info,
      colorClass: 'text-violeta dark:text-violeta-claro',
      bgClass: 'bg-[#F4EFFB] dark:bg-[#1a1324]',
      ringClass: 'ring-violeta/30 dark:ring-violeta-claro/20',
      iconBg: 'bg-violeta',
      iconColor: 'text-white',
    },
  }

  const config = typeConfig[type]
  const IconComponent = config.icon

  const animationClass = isExiting ? 'animate-toast-exit' : 'animate-toast-enter'

  const content = (
    <div
      className={`flex w-[350px] sm:w-[450px] max-w-[calc(100vw-2.5rem)] items-start gap-3.5 rounded-2xl p-4 sm:p-5 shadow-2xl ring-1 ${config.bgClass} ${config.ringClass} ${animationClass} ${
        isPortal ? 'fixed bottom-5 right-5 z-[9999]' : 'relative'
      }`}
    >
      <div className={`flex h-9 w-9 sm:h-10 sm:w-10 shrink-0 items-center justify-center rounded-full ${config.iconBg}`}>
        <IconComponent className={`h-5 w-5 ${config.iconColor}`} strokeWidth={2} />
      </div>
      <div className="min-w-0 flex-1">
        <p className={`text-sm sm:text-base font-bold ${config.colorClass}`}>{config.title}</p>
        <p className="mt-1 break-words text-xs sm:text-sm text-[#4A5568] dark:text-gray-300 leading-relaxed">{message}</p>
      </div>
      <button
        onClick={handleClose}
        className="mt-0.5 shrink-0 text-[#A0AEC0] transition-colors hover:text-gray-600 dark:text-gray-500 dark:hover:text-gray-300"
        aria-label="Cerrar"
      >
        <X className="h-4 w-4 sm:h-5 sm:w-5" strokeWidth={2} />
      </button>
    </div>
  )

  if (isPortal) {
    return createPortal(content, document.body)
  }

  return content
}
