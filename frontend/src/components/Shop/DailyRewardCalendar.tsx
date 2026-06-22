import { useState } from 'react'
import { Check, CircleDollarSign, Trophy, Zap } from 'lucide-react'
import { useDailyRewards } from '../../hooks/shop/useDailyRewards'
import { InlineError } from '../feedback/InlineError'
import { RewardToast } from './RewardToast'

interface DailyRewardCalendarProps {
  /** Se llama tras reclamar con éxito, para refrescar el saldo de monedas. */
  onClaimed?: () => void
}

export function DailyRewardCalendar({ onClaimed }: DailyRewardCalendarProps) {
  const { status, loading, claiming, error, claim } = useDailyRewards()
  const [toastCoins, setToastCoins] = useState<number | null>(null)

  const handleClaim = async () => {
    const result = await claim()
    if (result) {
      setToastCoins(result.coins)
      onClaimed?.()
    }
  }

  if (loading) {
    return (
      <div className="bg-white rounded-2xl shadow-md p-6 mb-8 border border-gray-100 flex justify-center items-center h-32">
        <div className="w-7 h-7 border-4 border-gray-300 border-t-yellow-500 rounded-full animate-spin" />
      </div>
    )
  }

  if (!status || status.days.length === 0) {
    return null
  }

  // Días del ciclo ya completados (lo calcula el backend) y cuál se puede reclamar hoy.
  const completed = status.completedDays
  const claimableDay = status.canClaimToday ? status.nextDay : null
  const claimableCoins = status.days.find((d) => d.dayNumber === claimableDay)?.coins ?? 0

  return (
    <div className="bg-white rounded-2xl shadow-md p-6 mb-8 border border-gray-100">
      <div className="flex items-center justify-between mb-1 flex-wrap gap-2">
        <h2 className="text-xl font-bold text-gray-800">Recompensa diaria</h2>
        <span className="inline-flex items-center gap-1 text-sm font-semibold text-yellow-700 bg-yellow-50 border border-yellow-200 rounded-full px-3 py-1">
          <Zap className="w-4 h-4 text-amber-500" strokeWidth={2} />
          Racha: {status.currentStreak} {status.currentStreak === 1 ? 'día' : 'días'}
        </span>
      </div>
      <p className="text-gray-500 text-sm mb-5">
        Entrá cada día y reclamá monedas. ¡Si te salteás un día, la racha vuelve al Día 1!
      </p>

      {error && (
        <InlineError message={error} className="mb-4" />
      )}


      <div className="grid grid-cols-4 sm:grid-cols-7 gap-3 mb-6">
        {status.days.map((day) => {
          const isClaimed = day.dayNumber <= completed
          const isClaimable = day.dayNumber === claimableDay
          const isLast = day.dayNumber === status.days.length

          return (
            <div
              key={day.dayNumber}
              className={[
                'rounded-xl border px-2 py-3 flex flex-col items-center justify-center gap-1 text-center transition-all',
                isClaimed
                  ? 'bg-yellow-50 border-yellow-300'
                  : isClaimable
                    ? 'bg-white border-yellow-400 ring-2 ring-yellow-300 shadow-md scale-105'
                    : 'bg-gray-50 border-gray-200 opacity-60',
              ].join(' ')}
            >
              <span className="inline-flex items-center gap-0.5 text-[11px] font-semibold text-gray-500">
                Día {day.dayNumber}
                {isLast && <Trophy className="w-3.5 h-3.5 text-amber-500" strokeWidth={2} />}
              </span>
              {isClaimed ? (
                <Check className="w-6 h-6 text-yellow-600" strokeWidth={2} />
              ) : (
                <CircleDollarSign
                  className={`w-6 h-6 ${isClaimable ? 'text-yellow-500' : 'text-gray-400'}`}
                  strokeWidth={2}
                />
              )}
              <span
                className={`text-sm font-bold ${
                  isClaimed ? 'text-yellow-700' : isClaimable ? 'text-yellow-600' : 'text-gray-400'
                }`}
              >
                {day.coins}
              </span>
              {isClaimable && (
                <span className="text-[10px] font-bold uppercase tracking-wide text-yellow-600">Hoy</span>
              )}
            </div>
          )
        })}
      </div>

      {status.canClaimToday ? (
        <button
          onClick={handleClaim}
          disabled={claiming}
          className="w-full py-3 rounded-xl bg-yellow-500 text-white font-semibold text-sm
                     hover:bg-yellow-600 active:scale-95 transition-all
                     disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          {claiming ? (
            <>
              <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              Reclamando…
            </>
          ) : (
            <>Reclamar recompensa de hoy (+{claimableCoins} 🪙)</>
          )}
        </button>
      ) : (
        <div className="w-full py-3 rounded-xl bg-gray-100 text-gray-500 font-semibold text-sm flex items-center justify-center gap-1.5">
          <Check className="w-5 h-5 text-green-500" strokeWidth={2} />
          Ya reclamaste hoy — Volvé mañana
        </div>
      )}

      {toastCoins !== null && (
        <RewardToast coins={toastCoins} onClose={() => setToastCoins(null)} />
      )}
    </div>
  )
}
