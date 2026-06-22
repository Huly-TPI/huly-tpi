import { useEffect, useState } from 'react'
import { useComebackReward } from '../../hooks/shop/useComebackReward'
import { RewardToast } from './RewardToast'
import modalCard from '../../assets/return-modal/modal_transparente_estilo_cozy.webp'
import claimButton from '../../assets/return-modal/boton_reclamar_cozy.webp'

interface ComebackRewardModalProps {
  /** Se llama tras reclamar con éxito, por si el contenedor necesita refrescar el saldo. */
  onClaimed?: () => void
}

/**
 * Modal de "bienvenida de vuelta": aparece en el home cuando el usuario estuvo inactivo el
 * tiempo suficiente y tiene una recompensa para reclamar. Usa el arte de fieltro
 * (card + botón) de assets/return-modal. Al canjear, cierra la oferta y muestra el toast dorado.
 */
export default function ComebackRewardModal({ onClaimed }: ComebackRewardModalProps) {
  const { status, claiming, error, claim } = useComebackReward()
  const [closed, setClosed] = useState(false)
  const [toastCoins, setToastCoins] = useState<number | null>(null)

  const showDialog = !closed && !!status?.available

  useEffect(() => {
    if (!showDialog) return

    const originalOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setClosed(true)
    }
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = originalOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [showDialog])

  const close = () => setClosed(true)

  const handleClaim = async () => {
    if (claiming) return
    const res = await claim()
    if (res?.granted) {
      setToastCoins(res.coins)
      onClaimed?.()
      setClosed(true)
    } else if (res) {
      // No calificaba (caso borde por carrera): cerramos sin error.
      setClosed(true)
    }
  }

  return (
    <>
      {showDialog && (
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
            className="relative z-10 w-full max-w-4xl"
          >
            <img
              src={modalCard}
              alt="¡Recompensa por volver a la página! Gracias por regresar."
              className="pointer-events-none w-full select-none"
              draggable={false}
            />

            {/* Hotspot invisible sobre la X horneada (arriba-derecha del card). */}
            <button
              type="button"
              aria-label="Cerrar"
              onClick={close}
              className="absolute right-[3%] top-[4%] h-[14%] w-[14%] cursor-pointer rounded-full"
            />

            {/* Botón "Reclamar" sobre el hueco inferior del card. */}
            <button
              type="button"
              aria-label="Reclamar recompensa"
              onClick={handleClaim}
              disabled={claiming}
              className="absolute bottom-[7%] left-1/2 w-[46%] -translate-x-1/2 transition-transform hover:scale-[1.03] active:scale-95 disabled:cursor-not-allowed"
            >
              <img
                src={claimButton}
                alt="Reclamar"
                className={`w-full select-none ${claiming ? 'opacity-60' : ''}`}
                draggable={false}
              />
              {claiming && (
                <span className="absolute inset-0 flex items-center justify-center">
                  <span className="h-5 w-5 animate-spin rounded-full border-2 border-white border-t-transparent" />
                </span>
              )}
            </button>

            {error && (
              <p className="absolute bottom-[2%] left-1/2 w-[80%] -translate-x-1/2 text-center text-xs font-semibold text-red-600">
                {error}
              </p>
            )}
          </div>
        </div>
      )}

      {toastCoins !== null && (
        <RewardToast coins={toastCoins} onClose={() => setToastCoins(null)} />
      )}
    </>
  )
}
