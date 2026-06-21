import { useState, useEffect } from 'react'
import { XMarkIcon, LockClosedIcon, CheckIcon } from '@heroicons/react/24/solid'
import modalBg from '../../assets/rewards/modal_papel_transparente.png'
import cardBg from '../../assets/rewards/cardSeed.png'
import giftIcon from '../../assets/rewards/regalo.png'
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

  const rewards =
    status?.days && status.days.length > 0
      ? status.days
      : [
        { dayNumber: 1, coins: 10 },
        { dayNumber: 2, coins: 15 },
        { dayNumber: 3, coins: 20 },
        { dayNumber: 4, coins: 25 },
        { dayNumber: 5, coins: 30 },
        { dayNumber: 6, coins: 40 },
        { dayNumber: 7, coins: 100 },
      ]

  const visualCurrentDay = status?.canClaimToday
    ? status.nextDay
    : Math.min(completed + 1, 7)

  const claimableCoins =
    status?.days.find(day => day.dayNumber === status.nextDay)?.coins ?? 0

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Recompensas diarias"
        className="relative z-10 w-full max-w-[700px]"
        onClick={e => e.stopPropagation()}
      >
        <img
          src={modalBg}
          alt=""
          aria-hidden="true"
          className="w-full h-auto select-none pointer-events-none block"
          draggable={false}
        />
        

        <div className="absolute inset-0 px-[8%] pt-[20%] pb-[7%]">
          <button
            onClick={onClose}
            aria-label="Cerrar recompensas"
            className="absolute top-[4%] right-[4%] w-8 h-8 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20"
          >
            <XMarkIcon className="w-5 h-5" />
          </button>

          <div className="text-center">
           <img
    src={giftIcon}
    alt=""
    aria-hidden="true"
    className="
      absolute
      top-[7.5%]
      left-1/2
      -translate-x-1/2
      w-18
      h-18
      sm:w-20
      sm:h-20
      object-contain
      pointer-events-none
      select-none
      z-10
    "
  />
            <h2 className="text-[22px] sm:text-[27px] font-black text-[#9b5718] leading-none">
              ¡Cosecha diaria!
            </h2>

            <p className="mt-2 text-[11px] sm:text-[13px] font-bold text-[#7b5c3c]">
              Iniciá sesión todos los días y obtené más semillas
            </p>

            <div className="mx-auto mt-3 w-fit rounded-full bg-[#f3d7a6]/90 border border-[#b98a54] px-4 shadow-[inset_0_1px_0_rgba(255,255,255,0.45),0_2px_4px_rgba(91,60,24,0.12)]">
              <span className="text-[11px] sm:text-[12px] font-black text-[#68451f]">
                🔥 Racha actual:{' '}
                <span className="text-[#4d8a2d]">{completed} días</span>
              </span>
            </div>
          </div>

          <div className="grid grid-cols-7 mt-4 sm:mt-4">
            {loading ? (
              <div className="col-span-7 flex justify-center py-12">
                <div className="w-7 h-7 border-4 border-[#8B6914] border-t-transparent rounded-full animate-spin" />
              </div>
            ) : (
              rewards.map(day => {
                const isClaimed = day.dayNumber <= completed
                const isToday = day.dayNumber === visualCurrentDay
                const isClaimable =
                  day.dayNumber === status?.nextDay && Boolean(status?.canClaimToday)
                const isLocked = !isClaimed && !isClaimable

                return (
                  <div
                    key={day.dayNumber}
                    className={`
                      relative aspect-[3/5]
                      transition-all duration-200 origin-center
                      ${isToday ? 'scale-[1.06] z-10' : ''}
                      ${isLocked ? 'opacity-95' : ''}
                    `}
                  >
                    {isToday && (
                      <div className="absolute inset-x-1 top-8 bottom-8 rounded-full bg-[#f6d56b]/45 blur-md" />
                    )}

                    <img
                      src={cardBg}
                      alt=""
                      aria-hidden="true"
                      className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_5px_6px_rgba(91,60,24,0.18)]"
                      draggable={false}
                    />

                    <div className="absolute inset-0">
                      <span className="absolute top-[9%] left-0 right-0 text-center text-[9px] sm:text-[12px] font-black whitespace-nowrap leading-none text-[#fff]">
                        Día {day.dayNumber}
                      </span>


                      <span className="absolute bottom-[29%] left-0 right-0 text-center text-[15px] sm:text-[18px] font-black text-[#5b3b1b] leading-none">
                        {day.coins}
                      </span>

                      {(isClaimed || isLocked) && (
                        <div className="absolute bottom-[15%] left-0 right-0 flex items-center justify-center">
                          {isClaimed ? (
                            <CheckIcon className="w-4 h-4 text-[#4d8a2d]" />
                          ) : (
                            <LockClosedIcon className="w-3.5 h-3.5 text-[#9a7541]" />
                          )}
                        </div>
                      )}

                      {isClaimable && (
                        <button
                          onClick={handleClaim}
                          disabled={claiming}
                          className="absolute bottom-[14%] left-1/2 -translate-x-1/2 rounded-full bg-[#6f9f38] px-2.5 py-0.5 text-[8px] sm:text-[9px] font-black text-white shadow-md active:scale-95 disabled:opacity-60 whitespace-nowrap"
                        >
                          {claiming ? '...' : 'Recolectar'}
                        </button>
                      )}
                    </div>
                  </div>
                )
              })
            )}
          </div>

          {!loading && status && (
            <div className="mt-4 sm:mt-5 mx-auto w-fit rounded-2xl bg-[#f3d8aa]/80 border border-[#c49a60] px-5 py-2 text-center shadow-sm">
              <p className="text-[11px] sm:text-[12px] text-[#6b4a2a] font-bold">
                {status.canClaimToday
                  ? `Recolectá ${claimableCoins} semillas hoy`
                  : 'Volvé mañana para seguir cultivando tu jardín'}
              </p>
            </div>
          )}

          {error && (
            <p className="absolute bottom-[3%] left-0 right-0 text-red-600 text-[10px] text-center px-4 font-bold">
              {error}
            </p>
          )}
        </div>
      </div>

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