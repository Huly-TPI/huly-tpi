import { useState, useEffect } from 'react'
import { XMarkIcon, LockClosedIcon, CheckIcon } from '@heroicons/react/24/solid'
import modalBg from '../../assets/rewards/modal_papel_transparente.png'
import cardBg from '../../assets/rewards/cardSeed.png'
import giftIcon from '../../assets/rewards/regalo.png'
import brote from '../../assets/rewards/brote.png'
import seedIcon from '../../assets/rewards/semilla.png'
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
    rewards.find(day => day.dayNumber === status?.nextDay)?.coins ?? 0

  const nextRewardDay = status?.canClaimToday
    ? status.nextDay
    : Math.min(completed + 1, 7)

  const nextRewardCoins =
    rewards.find(day => day.dayNumber === nextRewardDay)?.coins ?? 0

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Recompensas diarias"
        className="relative z-10 w-full max-w-[700px] max-[640px]:max-w-[370px]"
        onClick={e => e.stopPropagation()}
      >
        <img
          src={modalBg}
          alt=""
          aria-hidden="true"
          className="w-full h-auto max-[640px]:h-[610px] max-[640px]:object-fill select-none pointer-events-none block"
          draggable={false}
        />

        <div className="absolute inset-0 px-[8%] pt-[20%] pb-[7%] max-[640px]:px-[10%] max-[640px]:pt-[28%] max-[640px]:pb-[8%]">
          <button
            onClick={onClose}
            aria-label="Cerrar recompensas"
            className="absolute top-[4%] right-[4%] w-8 h-8 max-[640px]:w-7 max-[640px]:h-7 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20"
          >
            <XMarkIcon className="w-5 h-5 max-[640px]:w-4 max-[640px]:h-4" />
          </button>

          <div className="text-center">
            <img
              src={giftIcon}
              alt=""
              aria-hidden="true"
              className="
                absolute
                top-[7.5%]
                max-[640px]:top-[6%]
                left-1/2
                -translate-x-1/2
                w-18
                h-18
                sm:w-20
                sm:h-20
                max-[640px]:w-16
                max-[640px]:h-16
                object-contain
                pointer-events-none
                select-none
                z-10
              "
              draggable={false}
            />

            <h2 className="text-[22px] sm:text-[27px] max-[640px]:text-[22px] font-black text-[#9b5718] leading-none">
              ¡Cosecha diaria!
            </h2>

            <p className="mt-1 text-[11px] sm:text-[13px] max-[640px]:text-[10px] font-bold text-[#7b5c3c]">
              Iniciá sesión todos los días y obtené más semillas
            </p>

            <div className="mx-auto mt-2 w-fit rounded-full bg-[#f3d7a6]/90 border border-[#b98a54] px-3 max-[640px]:px-2 shadow-[inset_0_1px_0_rgba(255,255,255,0.45),0_2px_4px_rgba(91,60,24,0.12)]">
              <div className="flex items-center justify-center">
                <img
                  src={brote}
                  alt=""
                  aria-hidden="true"
                  className="w-8 h-8 sm:w-7 sm:h-7 max-[640px]:w-5 max-[640px]:h-5 object-contain pointer-events-none select-none shrink-0"
                  draggable={false}
                />

                <span className="text-[11px] sm:text-[12px] max-[640px]:text-[10px] font-black text-[#68451f] leading-none">
                  Racha actual:{' '}
                  <span className="text-[#4d8a2d]">{completed} días</span>
                </span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-7 max-[640px]:grid-cols-4 gap-x-0 max-[640px]:gap-x-2 max-[640px]:gap-y-2 mt-4 max-[640px]:mt-3">
            {loading ? (
              <div className="col-span-7 max-[640px]:col-span-4 flex justify-center py-12">
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
                      max-[640px]:aspect-[3/4.8]
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
                      <span className="absolute top-[9%] left-0 right-0 text-center text-[9px] sm:text-[12px] max-[640px]:text-[9px] font-black whitespace-nowrap leading-none text-[#fff]">
                        Día {day.dayNumber}
                      </span>

                      <span className="absolute bottom-[29%] left-0 right-0 text-center text-[15px] sm:text-[18px] max-[640px]:text-[15px] font-black text-[#5b3b1b] leading-none">
                        {day.coins}
                      </span>

                      {(isClaimed || isLocked) && (
                        <div className="absolute bottom-[15%] left-0 right-0 flex items-center justify-center">
                          {isClaimed ? (
                            <CheckIcon className="w-4 h-4 max-[640px]:w-3.5 max-[640px]:h-3.5 text-[#4d8a2d]" />
                          ) : (
                            <LockClosedIcon className="w-3.5 h-3.5 max-[640px]:w-3 max-[640px]:h-3 text-[#9a7541]" />
                          )}
                        </div>
                      )}

                      {isClaimable && (
                        <button
                          onClick={handleClaim}
                          disabled={claiming}
                          className="absolute bottom-[14%] left-1/2 -translate-x-1/2 rounded-full bg-[#6f9f38] px-2.5 max-[640px]:px-2 py-0.5 text-[8px] sm:text-[9px] max-[640px]:text-[7px] font-black text-white shadow-md active:scale-95 disabled:opacity-60 whitespace-nowrap"
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
            <div className="mt-2 sm:mt-3 max-[640px]:mt-3 mx-auto w-fit rounded-2xl px-5 max-[640px]:px-2 py-2 text-center">
              <div className="flex items-center justify-center gap-2">
                <div>
                  <p className="text-[14px] max-[640px]:text-[12px] font-black text-[#6b4a2a] leading-none">
                    Te esperan{' '}
                    <span className="text-[#4d8a2d]">
                      {status.canClaimToday ? claimableCoins : nextRewardCoins} semillas
                    </span>{' '}
                    {status.canClaimToday ? 'hoy' : 'mañana'}
                  </p>

                  <p className="text-[11px] max-[640px]:text-[9px] text-[#7b5c3c] font-bold mt-1">
                    {status.canClaimToday
                      ? '¡Recolectalas para hacer crecer tu jardín!'
                      : '¡Volvé cada día para hacer crecer tu jardín!'}
                  </p>
                </div>
              </div>
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
          className="
          fixed bottom-24 right-5 max-[640px]:right-4 max-[640px]:left-4
          z-[60]
          flex items-center justify-between gap-3
          rounded-2xl
          bg-[#f3d8aa]/95
          border border-[#b98a54]
          px-4 py-3
          shadow-[0_6px_12px_rgba(91,60,24,0.25),inset_0_1px_0_rgba(255,255,255,0.45)]
          text-[#6b4a2a]
        "
        >
          <div className="flex items-center gap-2">
            <img
              src={seedIcon}
              alt=""
              aria-hidden="true"
              className="w-10 h-10 object-contain shrink-0"
            />

            <div>
              <p className="text-[12px] font-black m-0 leading-none">
                ¡Recompensa reclamada!
              </p>

              <p className="text-[15px] font-black m-0 mt-1 leading-none">
                +<span className="text-[#4d8a2d]">{toastCoins} semillas</span>
              </p>
            </div>
          </div>

          <button
            aria-label="Cerrar notificación"
            onClick={() => setToastCoins(null)}
            className="rounded-full p-1.5 bg-[#d9b77e]/60 hover:bg-[#c99d61]/70 text-[#7b5c3c] transition"
          >
            <XMarkIcon className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  )
}