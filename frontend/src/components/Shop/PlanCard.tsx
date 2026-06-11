import type { Plan } from '../../api/payment'

interface PlanCardProps {
  plan: Plan
  buying: boolean
  disabled: boolean
  onBuy: (planId: string) => void
}

export function PlanCard({ plan, buying, disabled, onBuy }: PlanCardProps) {
  return (
    <div className="bg-white rounded-2xl shadow-md p-6 flex flex-col gap-4 border border-gray-100 hover:shadow-lg transition-shadow">
      <div className="flex-1">
        <h2 className="text-xl font-bold text-gray-800 mb-1">{plan.name}</h2>
        <div className="mb-2">
          <span className="inline-block bg-green-50 border border-green-200 text-green-700 text-xs font-semibold rounded-full px-2.5 py-0.5">
            {plan.planCode}
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
        disabled={disabled}
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
          'Suscribirme'
        )}
      </button>
    </div>
  )
}
