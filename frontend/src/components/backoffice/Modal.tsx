import React, { useEffect } from 'react'
import { X } from 'lucide-react'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title: string
  subtitle?: string
  children: React.ReactNode
  maxWidthClass?: string // p.ej. "max-w-2xl", "max-w-lg"
}

export default function Modal({
  isOpen,
  onClose,
  title,
  subtitle,
  children,
  maxWidthClass = 'max-w-md',
}: ModalProps) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden'
      
      const handleKeyDown = (e: KeyboardEvent) => {
        if (e.key === 'Escape') {
          onClose()
        }
      }
      
      window.addEventListener('keydown', handleKeyDown)
      return () => {
        document.body.style.overflow = ''
        window.removeEventListener('keydown', handleKeyDown)
      }
    } else {
      document.body.style.overflow = ''
    }
  }, [isOpen, onClose])

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
      {/* Overlay - Cierre al presionar fuera */}
      <div 
        className="absolute inset-0 cursor-default" 
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Caja del Modal */}
      <div 
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        aria-describedby={subtitle ? "modal-subtitle" : undefined}
        className={`relative w-full ${maxWidthClass} rounded-3xl bg-white p-6 shadow-2xl dark:bg-[#172033] border border-gray-100 dark:border-gray-800/60 flex flex-col gap-5 max-h-[90vh] overflow-y-auto z-10 transition-all transform duration-300`}
      >
        {/* Encabezado */}
        <div className="flex items-center justify-between">
          <div>
            <h3 id="modal-title" className="text-xl font-extrabold text-[#8869AC]">
              {title}
            </h3>
            {subtitle && (
              <p id="modal-subtitle" className="text-xs text-gray-400 uppercase font-bold tracking-wider mt-0.5">{subtitle}</p>
            )}
          </div>
          <button
            onClick={onClose}
            aria-label="Cerrar modal"
            className="rounded-xl p-2 text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <hr className="border-gray-100 dark:border-gray-800/60" />

        {/* Contenido */}
        <div className="flex-1">
          {children}
        </div>
      </div>
    </div>
  )
}
