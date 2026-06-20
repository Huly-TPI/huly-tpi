import { CircleDollarSign } from 'lucide-react'
import type { Plan } from '../../api/payment'

interface PlanCardProps {
  plan: Plan
  buying: boolean
  disabled: boolean
  /** id del producto de la membresía activa del usuario, o null si no tiene una vigente. */
  activeProductId: string | null
  onBuy: (planId: string) => void
}

export function PlanCard({ plan, buying, disabled, activeProductId, onBuy }: PlanCardProps) {
  const isCurrentPlan = activeProductId === plan.id
  const blockedByOtherPlan = activeProductId !== null && !isCurrentPlan
  const buttonDisabled = disabled || blockedByOtherPlan

  const label = isCurrentPlan
    ? 'Renovar'
    : blockedByOtherPlan
      ? 'Ya tenés un plan activo'
      : 'Suscribirme'

  return (
    <div className="bg-white rounded-2xl shadow-md p-6 flex flex-col gap-4 border border-gray-100 hover:shadow-lg transition-shadow">
      <div className="flex-1">
        <h2 className="text-xl font-bold text-gray-800 mb-1">{plan.name}</h2>
        <div className="flex items-center gap-2 mb-2">
          <span className="inline-block bg-green-50 border border-green-200 text-green-700 text-xs font-semibold rounded-full px-2.5 py-0.5">
            {plan.planCode}
          </span>
          {isCurrentPlan && (
            <span className="inline-block bg-blue-50 border border-blue-200 text-blue-700 text-xs font-semibold rounded-full px-2.5 py-0.5">
              Tu plan
            </span>
          )}
        </div>
        <div className="flex items-center gap-1 mb-2">
          <CircleDollarSign className="w-4 h-4 text-yellow-500" strokeWidth={2} />
          <span className="text-yellow-700 font-semibold text-sm">
            {plan.coinsAmount.toLocaleString('es-AR')} monedas
          </span>
        </div>
        <p className="text-gray-500 text-sm leading-relaxed">{plan.description}</p>
      </div>
      <div className="text-2xl font-extrabold text-green-700">
        ${plan.price.toLocaleString('es-AR')}
        <span className="text-sm font-normal text-gray-400 ml-1">ARS</span>
      </div>
      <button
        onClick={() => onBuy(plan.id)}
        disabled={buttonDisabled}
        className="w-full py-3 rounded-xl bg-green-600 text-white font-semibold text-sm
                   hover:bg-green-700 active:scale-95 transition-all
                   disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        {buying ? (
          <>
            <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            Procesando…
          </>
        ) : (
          label
        )}
      </button>
    </div>
  )
}
