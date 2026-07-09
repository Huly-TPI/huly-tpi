import { useState, useEffect, useCallback, type ReactNode } from 'react'
import { X, CheckIcon, Lock } from 'lucide-react'
import modalBg from '../../assets/rewards/modal.webp'
import modalBgMobile from '../../assets/rewards/modalMobile.webp'
import cardBg from '../../assets/rewards/cardSeed.webp'
import giftIcon from '../../assets/rewards/gift.webp'
import brote from '../../assets/rewards/brote.webp'
import { useDailyRewards } from '../../hooks/shop/useDailyRewards'
import type { DailyRewardDay } from '../../api/dailyRewards'
import { RewardToast } from '../Shop/RewardToast'


const MAX_STREAK_DAYS = 7
const MOBILE_QUERY = '(max-width: 640px)'
const MOBILE_ASPECT = '1024 / 1536'

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

function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState<boolean>(
    () => typeof window !== 'undefined' && window.matchMedia(query).matches,
  )

  useEffect(() => {
    const mql = window.matchMedia(query)
    const handleChange = (event: MediaQueryListEvent) => setMatches(event.matches)
    setMatches(mql.matches)
    mql.addEventListener('change', handleChange)
    return () => mql.removeEventListener('change', handleChange)
  }, [query])

  return matches
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
            <Lock className="w-3.5 h-3.5 max-[640px]:w-3 max-[640px]:h-3 text-[#9a7541]" />
          </div>
        )}

        {cardStatus === 'claimable' && (
          <button
            onClick={onClaim}
            disabled={claiming}
            className="absolute bottom-[13%] left-1/2 -translate-x-1/2 rounded-lg border-b-[3px] bg-[#7b8f45] border border-[#5f7332] hover:bg-[#6d8139] px-2.5 max-[640px]:px-2 py-0.5 text-[8px] sm:text-[9px] max-[640px]:text-[7px] font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] transition-all active:translate-y-[1px] active:border-b-[1px] disabled:opacity-60 whitespace-nowrap"
          >
            {claiming ? '...' : 'Recolectar'}
          </button>
        )}
      </div>
    </div>
  )
}

/* ─── Main Component ─── */

const TOAST_DURATION_MS = 3500

export default function RewardsModal({ isOpen, onClose, onClaimed }: RewardsModalProps) {
  const { status, loading, claiming, error, claim } = useDailyRewards()
  const [toastCoins, setToastCoins] = useState<number | null>(null)
  const isMobile = useMediaQuery(MOBILE_QUERY)

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

  /* Contenido compartido por desktop y mobile. */
  const content: ReactNode = (
    <>
      <button
        onClick={onClose}
        aria-label="Cerrar recompensas"
        className="absolute top-[4%] right-[4%] max-[640px]:top-[15%] max-[640px]:right-[8%] w-8 h-8 max-[640px]:w-7 max-[640px]:h-7 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20"
      >
        <X className="w-5 h-5 max-[640px]:w-4 max-[640px]:h-4" />
      </button>

      {/* Regalo apoyado sobre la solapa superior */}
      <img
        src={giftIcon}
        alt=""
        aria-hidden="true"
        className="absolute top-[7.5%] max-[640px]:top-[6%] left-1/2 -translate-x-1/2 w-16 h-16 sm:w-20 sm:h-20 max-[640px]:w-14 max-[640px]:h-14 object-contain pointer-events-none select-none z-10"
        draggable={false}
      />

      {/* Encabezado */}
      <div className="text-center">
        <h2 className="text-[22px] sm:text-[27px] font-black text-[#9b5718] leading-none">
          ¡Cosecha diaria!
        </h2>

        <p className="mt-1 text-[11px] sm:text-[13px] font-bold text-[#7b5c3c]">
          Iniciá sesión todos los días y obtené más semillas
        </p>

        <div className="mx-auto mt-2 w-fit rounded-full bg-[#f3d7a6]/90 border border-[#b98a54] px-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.45),0_2px_4px_rgba(91,60,24,0.12)]">
          <div className="flex items-center justify-center">
            <img
              src={brote}
              alt=""
              aria-hidden="true"
              className="w-7 h-7 max-[640px]:w-5 max-[640px]:h-5 object-contain pointer-events-none select-none shrink-0"
              draggable={false}
            />
            <span className="text-[11px] sm:text-[12px] max-[640px]:text-[10px] font-black text-[#68451f] leading-none">
              Racha actual:{' '}
              <span className="text-[#4d8a2d]">
                {completedDays} {completedDays === 1 ? 'día' : 'días'}
              </span>
            </span>
          </div>
        </div>
      </div>

      {/* Grilla de recompensas */}
      <div className="grid grid-cols-7 max-[640px]:grid-cols-4 gap-x-0 max-[640px]:gap-x-2 gap-y-0 max-[640px]:gap-y-2 mt-4">
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

      {/* Pie */}
      {!loading && status && (
        <div className="mt-3 mx-auto w-fit rounded-2xl px-5 max-[640px]:px-2 py-2 text-center">
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
        <p className="mt-2 text-red-600 text-[10px] text-center px-4 font-bold">
          {error}
        </p>
      )}
    </>
  )

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      {isMobile ? (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Recompensas diarias"
          onClick={e => e.stopPropagation()}
          style={{
            backgroundImage: `url(${modalBgMobile})`,
            backgroundSize: '100% 100%',
            backgroundRepeat: 'no-repeat',
            aspectRatio: MOBILE_ASPECT,
          }}
          className="relative z-10 w-full max-w-[360px] px-[14%] pt-[37%] pb-[18%]"
        >
          {content}
        </div>
      ) : (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Recompensas diarias"
          onClick={e => e.stopPropagation()}
          className="relative z-10 w-full max-w-[700px]"
        >
          <img
            src={modalBg}
            alt=""
            aria-hidden="true"
            className="block w-full h-auto select-none pointer-events-none"
            draggable={false}
          />
          <div className="absolute inset-0 px-[8%] pt-[20%] pb-[7%]">
            {content}
          </div>
        </div>
      )}

      {toastCoins !== null && (
        <RewardToast coins={toastCoins} onClose={dismissToast} />
      )}
    </div>
  )
}