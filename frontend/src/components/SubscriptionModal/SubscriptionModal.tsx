import { useCallback } from 'react'
import { XMarkIcon } from '@heroicons/react/24/solid'
import modalBg from '../../assets/suscription/modalSuscripcion.png'
import seedIcon from '../../assets/shop-seed/1seed.webp'
import cardGreen from '../../assets/suscription/cardGreen.png'
import cardYellow from '../../assets/suscription/cardYellow.png'
import cardPurple from '../../assets/suscription/cardPurple.png'
import { usePlans } from '../../hooks/shop/usePlans'
import { useMembership } from '../../hooks/shop/useMembership'
import { usePurchase } from '../../hooks/shop/usePurchase'
import { useRefreshOnReturn } from '../../hooks/shop/useRefreshOnReturn'
import type { Plan } from '../../api/payment'

interface SubscriptionModalProps {
  isOpen: boolean
  onClose: () => void
  onRefreshMembership: () => void
}

interface PaidCardProps {
  plan: Plan
  cardImage: string
  buying: boolean
  disabled: boolean
  activeProductId: string | null
  buttonTheme: 'yellow' | 'purple'
  onBuy: (id: string) => void
}

function FreeCard({ isCurrentPlan }: { isCurrentPlan: boolean }) {
  return (
    <div className="relative h-full w-auto transition-transform duration-200 origin-center hover:scale-[1.03]"
      style={{ aspectRatio: '353/706' }}
    >
      <img
        src={cardGreen}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_3px_4px_rgba(0,80,0,0.16)]"
        draggable={false}
      />

      {/* Zona superior: título alineado debajo del emoji pintado */}
      <div className="absolute top-[8%] left-[28%] right-[8%]">
        <h3 className="text-[11px] sm:text-[13px] font-black text-[#1a4a1a] leading-tight">
          Plan Gratuito
        </h3>
      </div>

      {/* Zona inferior: contenido debajo de la línea divisoria (~35%) */}
      <div className="absolute top-[38%] bottom-[16%] left-[8%] right-[8%] flex flex-col items-center justify-between text-center">
        <p className="text-[8px] sm:text-[10px] text-[#3a5c2a] leading-snug px-1">
          Acceso básico a la plataforma
        </p>

        <div className="flex flex-col items-center gap-1.5">
          <span className="font-black text-[16px] sm:text-[19px] text-[#2a6a2a]">
            Gratis
          </span>
          <div
            className={`rounded-lg px-3 py-1 font-black text-[9px] sm:text-[11px] whitespace-nowrap ${
              isCurrentPlan
                ? 'bg-green-100 text-green-700 border border-green-300'
                : 'invisible'
            }`}
          >
            Tu plan actual
          </div>
        </div>
      </div>
    </div>
  )
}

function PaidCard({ plan, cardImage, buying, disabled, activeProductId, buttonTheme, onBuy }: PaidCardProps) {
  const isCurrentPlan = activeProductId === plan.id
  const blockedByOther = activeProductId !== null && !isCurrentPlan
  const buttonDisabled = disabled || blockedByOther

  const label = isCurrentPlan ? 'Renovar' : blockedByOther ? 'Plan activo' : 'Suscribirme'

  const buttonClass =
    buttonTheme === 'yellow'
      ? 'bg-amber-500 border border-amber-600 hover:bg-amber-600'
      : 'bg-gradient-to-r from-[#8b5a8e] to-[#a06f9e] border-[#6b4470] hover:from-[#7d4f80] hover:to-[#926390]'

  return (
    <div
      className="relative h-full w-auto transition-transform duration-200 origin-center hover:scale-[1.03]"
      style={{ aspectRatio: '353/706' }}
    >
      <img
        src={cardImage}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_3px_4px_rgba(91,60,24,0.16)]"
        draggable={false}
      />

      {/* Zona superior: título alineado debajo del emoji pintado */}
      <div className="absolute top-[8%] left-[28%] right-[8%]">
        <h3 className="text-[11px] sm:text-[13px] font-black text-[#3a2a10] leading-tight line-clamp-2">
          {plan.name}
        </h3>
      </div>

      {/* Zona inferior: contenido debajo de la línea divisoria (~35%) */}
      <div className="absolute top-[38%] bottom-[16%] left-[8%] right-[8%] flex flex-col items-center justify-between text-center">
        <div className="flex flex-col items-center gap-1">
          <div className="flex items-center gap-1 rounded-full border border-[#b98a54] bg-[#f3d7a6]/70 px-2 py-0.5 shadow-[inset_0_1px_0_rgba(255,255,255,0.35)]">
            <img src={seedIcon} alt="" aria-hidden="true" className="w-4 h-4 object-contain shrink-0" />
            <span className="text-[8px] sm:text-[10px] font-black whitespace-nowrap text-[#3d7a1a]">
              {plan.coinsAmount.toLocaleString('es-AR')} semillas
            </span>
          </div>
          <p className="text-[8px] sm:text-[10px] text-[#7b5c3c] leading-snug line-clamp-2 px-1">
            {plan.description}
          </p>
        </div>

        <div className="flex flex-col items-center gap-1.5">
          <span className="font-black text-[16px] sm:text-[19px] text-[#8f541f]">
            ${plan.price.toLocaleString('es-AR')}
            <span className="text-[8px] sm:text-[9px] font-bold text-[#7b5c3c] ml-0.5">ARS</span>
          </span>
          <button
            type="button"
            onClick={() => onBuy(plan.id)}
            disabled={buttonDisabled}
            className={`rounded-lg border-b-[3px] px-3 sm:px-4 py-1 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] transition-all active:translate-y-[1px] active:border-b-[1px] disabled:opacity-60 whitespace-nowrap flex items-center justify-center gap-1 text-[9px] sm:text-[11px] ${buttonClass}`}
          >
            {buying ? (
              <>
                <span className="w-2.5 h-2.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                Procesando…
              </>
            ) : (
              label
            )}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function SubscriptionModal({ isOpen, onClose, onRefreshMembership }: SubscriptionModalProps) {
  const { plans, loading: plansLoading, error: plansError } = usePlans()
  const { membership, refresh: refreshMembership } = useMembership()
  const { buyingId, error: purchaseError, buy } = usePurchase()

  const markPendingPayment = useRefreshOnReturn(
    useCallback(() => {
      refreshMembership()
      onRefreshMembership()
    }, [refreshMembership, onRefreshMembership]),
  )

  const handleBuy = useCallback(
    (productId: string) => {
      markPendingPayment()
      buy(productId)
    },
    [markPendingPayment, buy],
  )

  if (!isOpen) return null

  const basicPlan = plans.find(p => p.planCode === 'BASIC')
  const premiumPlan = plans.find(p => p.planCode === 'PREMIUM')
  const activeProductId = membership?.active ? membership.productId : null

  return (
    <div
      className="fixed inset-0 z-[400] flex items-center justify-center p-3 pt-16 sm:p-4 sm:pt-16 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Planes de suscripción"
        className="relative z-10 w-full max-w-[760px] max-[640px]:max-w-[380px]"
        onClick={e => e.stopPropagation()}
      >
        <img
          src={modalBg}
          alt=""
          aria-hidden="true"
          className="w-full h-auto select-none pointer-events-none block"
          draggable={false}
        />

        <div className="absolute inset-0 px-[8%] pt-[9%] pb-[5%] flex flex-col">
          <button
            onClick={onClose}
            aria-label="Cerrar planes de suscripción"
            className="absolute top-[3%] right-[3%] w-8 h-8 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20"
          >
            <XMarkIcon className="w-5 h-5" />
          </button>

          <div className="text-center shrink-0">
            <h2 className="text-[16px] sm:text-[20px] font-black text-[#9b5718] leading-none">
              Planes de Suscripción
            </h2>
          </div>

          <p className="shrink-0 text-center mt-[5%] text-[10px] sm:text-[13px] font-bold text-[#7b5c3c]">
            Elegí el plan que mejor se adapte a vos
          </p>

          <div className="flex justify-center items-start gap-2 sm:gap-3 mt-2 px-[1%]">
            {plansLoading ? (
              <div className="flex justify-center py-12">
                <div className="w-7 h-7 border-4 border-[#8B6914] border-t-transparent rounded-full animate-spin" />
              </div>
            ) : (
              <>
                <div className="h-[clamp(280px,50vh,400px)]">
                  <FreeCard isCurrentPlan={!activeProductId} />
                </div>
                {basicPlan && (
                  <div className="h-[clamp(280px,50vh,400px)]">
                    <PaidCard
                      plan={basicPlan}
                      cardImage={cardYellow}
                      buying={buyingId === basicPlan.id}
                      disabled={buyingId !== null}
                      activeProductId={activeProductId}
                      buttonTheme="yellow"
                      onBuy={handleBuy}
                    />
                  </div>
                )}
                {premiumPlan && (
                  <div className="h-[clamp(280px,50vh,400px)]">
                    <PaidCard
                      plan={premiumPlan}
                      cardImage={cardPurple}
                      buying={buyingId === premiumPlan.id}
                      disabled={buyingId !== null}
                      activeProductId={activeProductId}
                      buttonTheme="purple"
                      onBuy={handleBuy}
                    />
                  </div>
                )}
              </>
            )}
          </div>

          {(plansError || purchaseError) && (
            <p className="shrink-0 mt-1 text-red-600 text-[10px] text-center px-4 font-bold">
              {plansError ?? purchaseError}
            </p>
          )}
        </div>
      </div>
    </div>
  )
}