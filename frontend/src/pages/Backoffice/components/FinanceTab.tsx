import { DollarSign } from 'lucide-react'
import { UserFinancialsResponse } from '../../../api/admin'

interface FinanceTabProps {
  financials: UserFinancialsResponse | null
  financialsLoading: boolean
  financialsError: string | null
}

export function FinanceTab({
  financials,
  financialsLoading,
  financialsError,
}: FinanceTabProps) {
  if (financialsLoading && !financials) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-violeta border-t-transparent"></div>
        <p className="text-xs text-gray-550 dark:text-gray-400 font-bold">Cargando métricas financieras...</p>
      </div>
    )
  }

  if (financialsError) {
    return (
      <div className="py-12 text-center text-sm text-red-500 font-semibold flex items-center justify-center">
        {financialsError}
      </div>
    )
  }

  if (!financials) return null

  const payments = financials.paymentEvents
  const totalEarnings = financials.totalEarnings
  const approvedCount = payments.filter((p) => p.status === 'APPROVED').length

  return (
    <div className="flex flex-col gap-6 animate-fadeIn">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 shrink-0">
        <div className="bg-gradient-to-br from-violeta/10 to-violeta-claro/5 dark:from-[#2A233C]/20 dark:to-transparent border border-violeta/20 rounded-xl p-5 flex flex-col justify-between">
          <span className="text-xs font-bold text-gray-400 dark:text-gray-555">Total Ganancias Generadas</span>
          <span className="text-3xl font-black text-violeta dark:text-violeta-claro mt-2">
            ${totalEarnings.toLocaleString('es-ES', { minimumFractionDigits: 2 })}
          </span>
        </div>
        <div className="bg-gradient-to-br from-emerald-500/10 to-emerald-500/5 dark:from-emerald-950/20 dark:to-transparent border border-emerald-500/20 rounded-xl p-5 flex flex-col justify-between">
          <span className="text-xs font-bold text-gray-400 dark:text-gray-555">Transacciones Aprobadas</span>
          <span className="text-3xl font-black text-emerald-600 dark:text-emerald-400 mt-2">{approvedCount}</span>
        </div>
      </div>

      <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-5">
        <div className="flex items-center gap-2 mb-4">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
            <DollarSign className="h-5 w-5" strokeWidth={1.8} />
          </div>
          <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Historial de Transacciones</h2>
        </div>
        {payments.length === 0 ? (
          <div className="py-8 text-center text-xs text-gray-400">No se registran transacciones financieras para este usuario.</div>
        ) : (
          <div className="space-y-3">
            {payments.map((p) => {
              const isApproved = p.status === 'APPROVED'
              const isPending = p.status === 'PENDING'
              return (
                <div key={p.id} className="bg-white dark:bg-[#172033] p-4 rounded-xl border border-gray-100 dark:border-gray-800/60 shadow-sm flex items-center justify-between gap-4 text-xs">
                  <div className="flex flex-col gap-1 truncate">
                    <span className="font-extrabold text-gray-750 dark:text-gray-200 truncate">{p.productName}</span>
                    <span className="text-[10px] text-gray-400 dark:text-gray-550 font-semibold truncate">
                      Ref: {p.externalReference} {p.mpPaymentId ? `| MP ID: ${p.mpPaymentId}` : ''}
                    </span>
                  </div>
                  <div className="flex items-center gap-4 shrink-0">
                    <span className="font-black text-gray-800 dark:text-gray-200 text-sm">
                      ${p.productPrice.toLocaleString('es-ES', { minimumFractionDigits: 2 })}
                    </span>
                    <span className={`px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider ${
                      isApproved
                        ? 'bg-green-100 dark:bg-green-955/30 text-green-800 dark:text-green-400'
                        : isPending
                        ? 'bg-amber-100 dark:bg-amber-955/35 text-amber-800 dark:text-amber-400'
                        : 'bg-red-100 dark:bg-red-955/35 text-red-800 dark:text-red-400'
                    }`}>
                      {p.status}
                    </span>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
