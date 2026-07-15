import { useEffect } from 'react'
import { X } from 'lucide-react'

import cardFrame from '../../assets/register/cardFrame.webp'

interface AuthGateModalProps {
  open: boolean
  onClose: () => void
  onLogin: () => void
  onRegister: () => void
}

export default function AuthGateModal({
  open,
  onClose,
  onLogin,
  onRegister,
}: AuthGateModalProps) {
  useEffect(() => {
    if (!open) return

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }

    document.addEventListener('keydown', handleEscape)

    return () => {
      document.removeEventListener('keydown', handleEscape)
    }
  }, [open, onClose])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] px-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="auth-gate-title"
      onClick={onClose}
    >
      <div
        className="
          relative
          w-full
          max-w-[460px]
          px-12
          pt-16
          pb-12
          font-nunito
        "
        onClick={e => e.stopPropagation()}
        style={{
          backgroundImage: `url(${cardFrame})`,
          backgroundSize: '100% 100%',
          backgroundRepeat: 'no-repeat',
        }}
      >
        <button
          type="button"
          onClick={onClose}
          aria-label="Cerrar"
          className="
          absolute
          -right-4
          -top-4
          z-10
          max-sm:hidden
          flex
          h-9
          w-9
          items-center
          justify-center
          rounded-full
          bg-[#a06f9e]
          text-white
          shadow-md
          transition
          hover:bg-[#875a86]
        "
        >
          <X className="h-5 w-5" />
        </button>

        <h2
          id="auth-gate-title"
          className="
            mb-5
            text-center
            text-3xl
            font-extrabold
            tracking-tight
            text-[#3E3A35]
          "
        >
          ¡Necesitás una cuenta!
        </h2>

        <p
          className="
            mx-auto
            mb-12
            max-w-[320px]
            text-center
            text-base
            leading-relaxed
            text-[#6F675F]
          "
        >
          Iniciá sesión o registrate para disfrutar de todas las
          funcionalidades de Huly.
        </p>

        <div className="mx-auto flex max-w-[340px] flex-col gap-6">
          <button
            type="button"
            onClick={onLogin}
            className="
              w-full
              rounded-full
              bg-[#9B6ACF]
              py-3.5
              text-base
              font-bold
              text-white
              shadow-lg
              transition-all
              duration-200
              hover:-translate-y-0.5
              hover:bg-[#8A58C2]
              active:scale-[0.98]
            "
          >
            Iniciar sesión
          </button>

          <div className="flex items-center gap-4">
            <div className="h-px flex-1 bg-[#C8B59C]" />
            <span
              className="
                text-sm
                font-semibold
                text-[#8C8075]
              "
            >
              o
            </span>
            <div className="h-px flex-1 bg-[#C8B59C]" />
          </div>

          <button
            type="button"
            onClick={onRegister}
            className="
              w-full
              rounded-full
              border-2
              border-[#B892E3]
              py-3.5
              text-base
              font-bold
              text-[#8D63C7]
              transition-all
              duration-200
              hover:bg-[#9B6ACF]/10
              hover:-translate-y-0.5
            "
          >
            Registrarse
          </button>

          <button
            type="button"
            onClick={onClose}
            className="
              mt-1
              text-center
              text-sm
              font-bold
              text-[#5F5953]
              underline-offset-4
              transition-all
              hover:text-[#3E3A35]
              hover:underline
            "
          >
            Ahora no
          </button>
        </div>
      </div>
    </div>
  )
}