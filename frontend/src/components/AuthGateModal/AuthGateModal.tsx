import { useEffect } from 'react'

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
      className="fixed inset-0 z-[100] flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="auth-gate-title"
    >
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />

      <div
        className="relative w-full max-w-[calc(100%-2rem)] sm:max-w-sm px-8 py-12 md:px-[4.5rem] md:py-14 font-nunito"
        style={{
          backgroundImage: `url(${cardFrame})`,
          backgroundSize: '100% 100%',
          backgroundRepeat: 'no-repeat',
        }}
      >
        <h2
          id="auth-gate-title"
          className="mb-2 text-center text-2xl md:text-3xl font-bold text-[#3D3D3D]"
        >
          ¡Necesitás una cuenta!
        </h2>

        <p className="mb-8 text-center text-sm text-[#6B6B6B]">
          Para esta acción iniciá sesión o registrate
        </p>

        <div className="flex flex-col gap-4">
          <button
            type="button"
            onClick={onLogin}
            className="
              w-full
              rounded-full
              bg-[#8E68B0]
              py-3
              text-sm
              font-semibold
              text-white
              shadow-md
              transition-all
              hover:brightness-105
              active:scale-[0.98]
            "
          >
            Iniciar sesión
          </button>

          <button
            type="button"
            onClick={onRegister}
            className="
              w-full
              rounded-full
              border-2
              border-[#8E68B0]
              bg-transparent
              py-3
              text-sm
              font-semibold
              text-[#8E68B0]
              transition-all
              hover:bg-[#8E68B0]/5
            "
          >
            Registrarse
          </button>

          <button
            type="button"
            onClick={onClose}
            className="
              mt-1
              text-sm
              text-[#4C7C64]
              underline-offset-4
              transition-all
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