import { useEffect, useState } from 'react'
import { CircleDollarSign, Gift, X } from 'lucide-react'
import { useComebackReward } from '../../hooks/shop/useComebackReward'
import type { ClaimComebackRewardResult } from '../../api/comebackRewards'

interface ComebackRewardModalProps {
  /** Se llama tras reclamar con éxito, por si el contenedor necesita refrescar el saldo. */
  onClaimed?: () => void
}

/**
 * Modal de "bienvenida de vuelta": aparece en el home cuando el usuario estuvo inactivo el
 * tiempo suficiente y tiene una recompensa de monedas para reclamar.
 */
export default function ComebackRewardModal({ onClaimed }: ComebackRewardModalProps) {
  const { status, claiming, error, claim } = useComebackReward()
  const [dismissed, setDismissed] = useState(false)
  const [result, setResult] = useState<ClaimComebackRewardResult | null>(null)

  const showSuccess = !dismissed && !!result?.granted
  const showOffer = !dismissed && !result && !!status?.available
  const isOpen = showOffer || showSuccess

  useEffect(() => {
    if (!isOpen) return

    const originalOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setDismissed(true)
    }
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = originalOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen])

  if (!isOpen) return null

  const coins = result?.coins ?? status?.coins ?? 0
  const days = result?.daysInactive ?? status?.daysInactive ?? 0

  const close = () => setDismissed(true)

  const handleClaim = async () => {
    const res = await claim()
    if (res?.granted) {
      setResult(res)
      onClaimed?.()
    } else if (res) {
      // No calificaba (caso borde por carrera): cerramos sin error.
      setDismissed(true)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[var(--overlay-strong)] p-4">
      <button
        type="button"
        aria-label="Cerrar"
        className="absolute inset-0 cursor-default"
        onClick={close}
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-label="Recompensa por volver"
        className="relative z-10 w-full max-w-sm overflow-hidden rounded-2xl bg-[var(--surface-primary)] text-[var(--text-primary)] shadow-2xl"
      >
        <button
          type="button"
          aria-label="Cerrar"
          onClick={close}
          className="absolute right-3 top-3 rounded-full border-0 bg-transparent p-1.5 text-[var(--text-secondary)] transition hover:bg-black/5"
        >
          <X className="h-5 w-5" strokeWidth={2} />
        </button>

        <div className="flex flex-col items-center gap-3 px-6 pb-7 pt-9 text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-yellow-100">
            {showSuccess ? (
              <CircleDollarSign className="h-9 w-9 text-yellow-500" strokeWidth={2} aria-hidden="true" />
            ) : (
              <Gift className="h-9 w-9 text-yellow-500" strokeWidth={2} aria-hidden="true" />
            )}
          </div>

          {showSuccess ? (
            <>
              <h2 className="text-xl font-bold">¡Monedas acreditadas!</h2>
              <p className="text-sm text-[var(--text-secondary)]">
                Sumaste <span className="font-bold text-yellow-600">+{coins} monedas</span> por volver. 🪙
              </p>
              <button
                type="button"
                onClick={close}
                className="mt-2 w-full rounded-xl bg-yellow-500 py-3 text-sm font-semibold text-white transition-all hover:bg-yellow-600 active:scale-95"
              >
                ¡Genial!
              </button>
            </>
          ) : (
            <>
              <h2 className="text-xl font-bold">¡Qué bueno verte de nuevo!</h2>
              <p className="text-sm text-[var(--text-secondary)]">
                Pasaron <span className="font-semibold">{days} días</span> desde tu última visita. Te dejamos una
                recompensa por volver al jardín.
              </p>

              {error && (
                <div className="w-full rounded-lg border border-red-200 bg-red-50 px-4 py-2.5 text-center text-sm text-red-700">
                  {error}
                </div>
              )}

              <button
                type="button"
                onClick={handleClaim}
                disabled={claiming}
                className="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-yellow-500 py-3 text-sm font-semibold text-white transition-all hover:bg-yellow-600 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {claiming ? (
                  <>
                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                    Reclamando…
                  </>
                ) : (
                  <>Reclamar +{coins} monedas 🪙</>
                )}
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
