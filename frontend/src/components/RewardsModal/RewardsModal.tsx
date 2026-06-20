import { useState, useEffect } from 'react'
import { XMarkIcon, LockClosedIcon, CheckIcon } from '@heroicons/react/24/solid'
import modalSemillasBg from '../../assets/rewards/modalSemillas.png'
import { useDailyRewards } from '../../hooks/shop/useDailyRewards'

interface RewardsModalProps {
  isOpen: boolean
  onClose: () => void
  onClaimed?: () => void
}

export default function RewardsModal({ isOpen, onClose, onClaimed }: RewardsModalProps) {
  const { status, loading, claiming, error, claim } = useDailyRewards()
  const [toastCoins, setToastCoins] = useState<number | null>(null)

  useEffect(() => {
    if (toastCoins === null) return
    const id = setTimeout(() => setToastCoins(null), 3500)
    return () => clearTimeout(id)
  }, [toastCoins])

  if (!isOpen) return null

  const handleClaim = async () => {
    const result = await claim()
    if (result) {
      setToastCoins(result.coins)
      onClaimed?.()
    }
  }

  const completed = status?.completedDays ?? 0
  const claimableDay = status?.canClaimToday ? status.nextDay : null
  const claimableCoins = status?.days.find(d => d.dayNumber === claimableDay)?.coins ?? 0

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Recompensas diarias"
        className="relative z-10 w-full max-w-[520px]"
        onClick={e => e.stopPropagation()}
      >
        {/* Fondo — modalSemillas.png con los 7 slots, el badge arriba y la barra verde abajo */}
        <img
          src={modalSemillasBg}
          alt=""
          aria-hidden="true"
          className="w-full h-auto select-none pointer-events-none block"
          draggable={false}
        />

        <div className="absolute inset-0">

          {/* Botón cerrar */}
          <button
            onClick={onClose}
            aria-label="Cerrar recompensas"
            className="absolute top-[3%] right-[2%] w-6 h-6 flex items-center justify-center rounded-full bg-[#8B7355]/80 hover:bg-[#6d5840] text-white transition z-10"
          >
            <XMarkIcon className="w-3.5 h-3.5" />
          </button>

          {/* Título y subtítulo centrados en el badge */}
          <div className="absolute left-0 right-0 text-center z-10" style={{ top: '27%' }}>
            <p className="text-[20px] font-black text-[#8a4f16] leading-tight">
              ¡Cosecha diaria!
            </p>
            <p className="text-[12px] font-bold text-[#8B7355] leading-tight mt-0.5">
              Iniciá sesión todos los días y obtené más semillas
            </p>
          </div>

          {/* 7 slots */}
          <div
            className="absolute grid grid-cols-7"
            style={{
              top: '38%',
              left: '10%',
              right: '10%',
              height: '35%',
            }}

          >
            {loading ? (
              <div className="col-span-7 flex items-center justify-center">
                <div className="w-5 h-5 border-4 border-[#8B6914] border-t-transparent rounded-full animate-spin" />
              </div>
            ) : status && status.days.length > 0 ? (
              status.days.map(day => {
                const isClaimed = day.dayNumber <= completed

                return (
                  <div key={day.dayNumber} className="relative h-full">

                    {/* Día arriba de la semilla */}
                    <span
                      className="absolute left-0 right-0 text-center text-[10px] font-black text-[#5a3e1b] leading-none"
                      style={{ top: '8%' }}
                    >
                      Día {day.dayNumber}
                    </span>

                    {/* Cantidad debajo de la semilla */}
                    <span
                      className="absolute left-0 right-0 text-center text-[10px] font-black text-[#5a3e1b] leading-none"
                      style={{ top: '52%' }}
                    >
                      {day.coins}
                    </span>

                    {/* Candado/check debajo del número, dentro de la card */}
                    <div

                      className="absolute left-0 right-0 flex justify-center"
                      style={{ top: '67%' }}
                    >

                      {isClaimed ? (
                        <CheckIcon className="w-3 h-3 text-[#4a7a30]" />
                      ) : (
                        <LockClosedIcon className="w-3 h-3 text-[#b8944f]" />
                      )}
                    </div>
                  </div>
                )
              })
            ) : null}
          </div>

          {/* Botón — superpuesto sobre la barra verde dibujada en la imagen */}
          {!loading && status && (
            <div
              className="absolute flex items-center justify-center"
              style={{ top: '69%', left: '10%', right: '10%', bottom: '8%' }}
            >
              {status.canClaimToday ? (
                <button
                  onClick={handleClaim}
                  disabled={claiming}
                  className="w-full h-full flex items-center justify-center text-[#3d4a2e] font-bold text-[11px] active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {claiming ? 'Reclamando...' : `Recoger +${claimableCoins} semillas`}
                </button>
              ) : (
                <span className="text-[#3d4a2e] font-semibold text-[11px] text-center leading-tight">
                  Volvé mañana para tu siguiente recompensa
                </span>
              )}
            </div>
          )}

          {error && (
            <p className="absolute bottom-[3%] left-0 right-0 text-red-600 text-[9px] text-center px-2">
              {error}
            </p>
          )}
        </div>
      </div>

      {/* Toast */}
      {toastCoins !== null && (
        <div
          role="status"
          aria-live="polite"
          className="fixed bottom-24 right-5 z-[60] flex items-center gap-3 rounded-2xl bg-gradient-to-r from-[#4C7C64] to-[#6aaa8a] px-5 py-4 shadow-xl text-white"
        >
          <span className="text-2xl">🌱</span>
          <div>
            <p className="text-sm opacity-90 m-0">¡Recompensa reclamada!</p>
            <p className="font-bold text-lg m-0">+{toastCoins} semillas</p>
          </div>
          <button
            aria-label="Cerrar notificación"
            onClick={() => setToastCoins(null)}
            className="ml-1 rounded-full p-1.5 hover:bg-white/20 transition"
          >
            <XMarkIcon className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  )
}
