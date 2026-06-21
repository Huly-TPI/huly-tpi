import { useState, useCallback } from 'react'
import { XMarkIcon, LockClosedIcon, CheckIcon } from '@heroicons/react/24/solid'
import modalBg from '../../assets/rewards/modal.webp'
import cardBg from '../../assets/rewards/cardSeed.webp'
import giftIcon from '../../assets/rewards/gift.webp'
import brote from '../../assets/rewards/brote.webp'
import seedIcon from '../../assets/rewards/seed.webp'
import { useDailyRewards } from '../../hooks/shop/useDailyRewards'
import type { DailyRewardDay } from '../../api/dailyRewards'


const MAX_STREAK_DAYS = 7

const DEFAULT_REWARDS: DailyRewardDay[] = [
  { dayNumber: 1, coins: 10 },
  { dayNumber: 2, coins: 15 },
  { dayNumber: 3, coins: 20 },
  { dayNumber: 4, coins: 25 },
  { dayNumber: 5, coins: 30 },
  { dayNumber: 6, coins: 40 },
  { dayNumber: 7, coins: 100 },
]

interface RewardsModalProps {
  isOpen: boolean
  onClose: () => void
  onClaimed?: () => void
}

type CardStatus = 'claimed' | 'claimable' | 'locked'

interface RewardCardProps {
  day: DailyRewardDay
  cardStatus: CardStatus
  isToday: boolean
  claiming: boolean
  onClaim: () => void
}

interface ClaimToastProps {
  coins: number
  onDismiss: () => void
}

function getCardStatus(
  dayNumber: number,
  completedDays: number,
  claimableDay: number | undefined,
  canClaim: boolean,
): CardStatus {
  if (dayNumber <= completedDays) return 'claimed'
  if (dayNumber === claimableDay && canClaim) return 'claimable'
  return 'locked'
}

function findCoinsForDay(rewards: DailyRewardDay[], dayNumber: number): number {
  return rewards.find(day => day.dayNumber === dayNumber)?.coins ?? 0
}

/* ─── Subcomponents ─── */

function RewardCard({ day, cardStatus, isToday, claiming, onClaim }: RewardCardProps) {
  return (
    <div
      className={`
        relative aspect-[3/5] max-[640px]:aspect-[3/4.8]
        transition-all duration-200 origin-center
        ${isToday ? 'scale-[1.06] z-10' : ''}
        ${cardStatus === 'locked' ? 'opacity-95' : ''}
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

        {cardStatus === 'claimed' && (
          <div className="absolute bottom-[15%] left-0 right-0 flex items-center justify-center">
            <CheckIcon className="w-4 h-4 max-[640px]:w-3.5 max-[640px]:h-3.5 text-[#4d8a2d]" />
          </div>
        )}

        {cardStatus === 'locked' && (
          <div className="absolute bottom-[15%] left-0 right-0 flex items-center justify-center">
            <LockClosedIcon className="w-3.5 h-3.5 max-[640px]:w-3 max-[640px]:h-3 text-[#9a7541]" />
          </div>
        )}

        {cardStatus === 'claimable' && (
          <button
            onClick={onClaim}
            disabled={claiming}
            className="absolute bottom-[14%] left-1/2 -translate-x-1/2 rounded-full bg-[#6f9f38] px-2.5 max-[640px]:px-2 py-0.5 text-[8px] sm:text-[9px] max-[640px]:text-[7px] font-black text-white shadow-md active:scale-95 disabled:opacity-60 whitespace-nowrap"
          >
            {claiming ? '...' : 'Recolectar'}
          </button>
        )}
      </div>
    </div>
  )
}

function ClaimToast({ coins, onDismiss }: ClaimToastProps) {
  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed bottom-24 right-5 max-[640px]:right-4 max-[640px]:left-4 z-[60] flex items-center justify-between gap-3 rounded-2xl bg-[#f3d8aa]/95 border border-[#b98a54] px-4 py-3 shadow-[0_6px_12px_rgba(91,60,24,0.25),inset_0_1px_0_rgba(255,255,255,0.45)] text-[#6b4a2a]"
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
            +<span className="text-[#4d8a2d]">{coins} semillas</span>
          </p>
        </div>
      </div>

      <button
        aria-label="Cerrar notificación"
        onClick={onDismiss}
        className="rounded-full p-1.5 bg-[#d9b77e]/60 hover:bg-[#c99d61]/70 text-[#7b5c3c] transition"
      >
        <XMarkIcon className="w-4 h-4" />
      </button>
    </div>
  )
}

/* ─── Main Component ─── */

const TOAST_DURATION_MS = 3500

export default function RewardsModal({ isOpen, onClose, onClaimed }: RewardsModalProps) {
  const { status, loading, claiming, error, claim } = useDailyRewards()
  const [toastCoins, setToastCoins] = useState<number | null>(null)

  const dismissToast = useCallback(() => setToastCoins(null), [])

  const handleClaim = useCallback(async () => {
    const result = await claim()
    if (result) {
      setToastCoins(result.coins)
      setTimeout(dismissToast, TOAST_DURATION_MS)
      onClaimed?.()
    }
  }, [claim, onClaimed, dismissToast])

  if (!isOpen) return null

  const completedDays = status?.completedDays ?? 0
  const canClaimToday = Boolean(status?.canClaimToday)
  const rewards = status?.days?.length ? status.days : DEFAULT_REWARDS

  const currentDay = canClaimToday
    ? (status?.nextDay ?? 1)
    : Math.min(completedDays + 1, MAX_STREAK_DAYS)

  const nextRewardCoins = findCoinsForDay(rewards, currentDay)

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
              className="absolute top-[7.5%] max-[640px]:top-[6%] left-1/2 -translate-x-1/2 w-18 h-18 sm:w-20 sm:h-20 max-[640px]:w-16 max-[640px]:h-16 object-contain pointer-events-none select-none z-10"
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
                  <span className="text-[#4d8a2d]">{completedDays} días</span>
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
                const cardStatus = getCardStatus(
                  day.dayNumber,
                  completedDays,
                  status?.nextDay,
                  canClaimToday,
                )

                return (
                  <RewardCard
                    key={day.dayNumber}
                    day={day}
                    cardStatus={cardStatus}
                    isToday={day.dayNumber === currentDay}
                    claiming={claiming}
                    onClaim={handleClaim}
                  />
                )
              })
            )}
          </div>

          {!loading && status && (
            <div className="mt-2 sm:mt-3 max-[640px]:mt-3 mx-auto w-fit rounded-2xl px-5 max-[640px]:px-2 py-2 text-center">
              <p className="text-[14px] max-[640px]:text-[12px] font-black text-[#6b4a2a] leading-none">
                Te esperan{' '}
                <span className="text-[#4d8a2d]">
                  {nextRewardCoins} semillas
                </span>{' '}
                {canClaimToday ? 'hoy' : 'mañana'}
              </p>
              <p className="text-[11px] max-[640px]:text-[9px] text-[#7b5c3c] font-bold mt-1">
                {canClaimToday
                  ? '¡Recolectalas para hacer crecer tu jardín!'
                  : '¡Volvé cada día para hacer crecer tu jardín!'}
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
        <ClaimToast coins={toastCoins} onDismiss={dismissToast} />
      )}
    </div>
  )
}