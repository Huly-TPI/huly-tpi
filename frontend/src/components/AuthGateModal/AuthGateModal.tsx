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
    return () => document.removeEventListener('keydown', handleEscape)
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
          className="mb-2 text-center text-2xl md:text-3xl font-bold text-[#4C7C64]"
        >
          ¡Necesitás una cuenta!
        </h2>
        <p className="mb-6 text-center text-sm italic text-[#8c7b66]">
          Para esta acción iniciá sesión o registrate
        </p>

        <div className="flex flex-col gap-3">
          <button
            type="button"
            onClick={onLogin}
            className="rounded-xl border-b-4 border-[#3d6b44] bg-[#5a8a50] py-3 text-sm font-bold text-white transition-all hover:brightness-105 active:translate-y-[2px] active:border-b-2"
          >
            Iniciar sesión
          </button>
          <button
            type="button"
            onClick={onRegister}
            className="rounded-xl border-2 border-[#5a8a50] bg-transparent py-3 text-sm font-bold text-[#4C7C64] transition-colors hover:bg-[#5a8a50]/10"
          >
            Registrarse
          </button>
          <button
            type="button"
            onClick={onClose}
            className="mt-1 py-1 text-xs text-[#8c7b66] transition-colors hover:text-[#4C7C64]"
          >
            Ahora no
          </button>
        </div>
      </div>
    </div>
  )
}