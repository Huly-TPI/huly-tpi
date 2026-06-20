import { useEffect, useRef, useState } from 'react'
import { EllipsisVerticalIcon } from '@heroicons/react/24/outline'
import { TrashIcon } from '@heroicons/react/24/solid'
import Button from '../Buttons/Button/Button'
import hulyChatbotImage from '../../assets/chatbot/huly-chatbot.webp'

interface ChatbotHeaderProps {
  onClose: () => void
  onReset: () => void
}

export default function ChatbotHeader({ onClose, onReset }: ChatbotHeaderProps) {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!isMenuOpen) return

    const handlePointerDown = (event: MouseEvent) => {
      if (!menuRef.current?.contains(event.target as Node)) {
        setIsMenuOpen(false)
      }
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsMenuOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('keydown', handleEscape)

    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [isMenuOpen])

  return (
    <header className="flex items-center justify-between border-b border-[var(--border-soft)] px-6 py-4">
      <div className="flex items-center gap-2">
        <img src={hulyChatbotImage} alt="" aria-hidden className="h-9 w-9 rounded-full object-cover" />
        <h2 className="text-3xl font-extrabold text-violeta">Huly</h2>
      </div>
      <div className="flex items-center gap-2">
        <div className="relative" ref={menuRef}>
          <button
            type="button"
            aria-label="Abrir opciones del chat"
            aria-haspopup="menu"
            aria-expanded={isMenuOpen}
            onClick={() => setIsMenuOpen(open => !open)}
            className="flex h-10 w-10 items-center justify-center rounded-full text-violeta transition-[background-color,color,opacity,box-shadow] duration-300 ease-in-out hover:bg-violeta/10 focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-3 focus-visible:outline-[#8869ac59]"
          >
            <EllipsisVerticalIcon className="h-5 w-5" />
          </button>

          {isMenuOpen && (
            <div
              role="menu"
              aria-label="Opciones del chat"
              className="absolute right-0 top-full z-10 mt-2 min-w-[10rem] overflow-hidden rounded-xl border border-[var(--border-soft)] bg-[var(--surface-primary)] py-1 shadow-lg"
            >
              <button
                type="button"
                role="menuitem"
                onClick={() => {
                  onReset()
                  setIsMenuOpen(false)
                }}
                className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-[var(--text-primary)] transition hover:bg-violeta/10"
              >
                <TrashIcon className="h-4 w-4 shrink-0 text-violeta" />
                Limpiar chat
              </button>
            </div>
          )}
        </div>
        <Button
          type="button"
          onClick={onClose}
          variant="secondary"
          size="sm"
          className="!w-auto !min-w-0"
        >
          Cerrar
        </Button>
      </div>
    </header>
  )
}
