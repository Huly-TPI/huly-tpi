import { useCallback } from 'react'
import { X } from 'lucide-react'
import modalBg from '../../assets/suscription/modalSuscripcion.webp'
import cardGreen from '../../assets/suscription/cardGreen.webp'
import cardYellow from '../../assets/suscription/cardYellow.webp'
import cardPurple from '../../assets/suscription/cardPurple.webp'
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
  displayName: string
  cardImage: string
  features: string[]
  showSeeds?: boolean
  compact?: boolean
  buying: boolean
  disabled: boolean
  activeProductId: string | null
  buttonTheme: 'yellow' | 'purple'
  onBuy: (id: string) => void
}

const FREE_FEATURES = ['5 mensajes al día']

const PLAN_DISPLAY_NAMES: Record<string, string> = {
  BASIC: 'Basico',
  PREMIUM: 'Huly',
}

function getDisplayName(planCode: string, fallback: string): string {
  return PLAN_DISPLAY_NAMES[planCode] ?? fallback
}

function FreeCard({ isCurrentPlan }: { isCurrentPlan: boolean }) {
  return (
    <div className="relative h-full w-full min-w-0 transition-transform duration-200 origin-center hover:scale-[1.015]" style={{ aspectRatio: '353/706' }}>
      <img src={cardGreen} alt="" aria-hidden="true" className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_3px_4px_rgba(0,80,0,0.16)]" draggable={false} />

      <div className="absolute inset-x-[8%] top-[7%] bottom-[8%] flex flex-col items-center text-center overflow-hidden">
        <h3 className="text-[13px] sm:text-[15px] font-black text-[#1a4a1a] leading-none">Gratuito</h3>
        <div className="mt-3">
          <p className="font-black text-[16px] sm:text-[19px] text-[#2a6a2a] leading-none">$0</p>
          <p className="mt-1 text-[7px] sm:text-[10px] font-bold text-[#3a5c2a]">Siempre gratis</p>
        </div>

        <div className="mt-3 mb-2 w-[78%] h-px bg-gradient-to-r from-transparent via-[#6abf55]/35 to-transparent" />

        <ul className="w-full flex-1 space-y-1">
          {FREE_FEATURES.map((feature, index) => (
            <li key={index} className="flex items-start gap-1.5 text-left text-[10px] sm:text-[10px] text-[#3a5c2a] leading-tight">
              <span className="mt-[1px] flex h-3 w-3 shrink-0 items-center justify-center rounded-full bg-[#8ccf65] text-white text-[7px] font-black">✓</span>
              <span className="font-bold">{feature}</span>
            </li>
          ))}
        </ul>

        <div className="mt-2 rounded-full border-b-[3px] px-4 py-1.5 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] text-[8px] sm:text-[10px] whitespace-nowrap bg-[#7b8f45] border border-[#5f7332]">
          {isCurrentPlan ? 'Plan actual' : 'Plan base'}
        </div>
      </div>
    </div>
  )
}

function PaidCard({
  plan,
  displayName,
  cardImage,
  features,
  showSeeds = false,
  compact = false,
  buying,
  disabled,
  activeProductId,
  buttonTheme,
  onBuy,
}: PaidCardProps) {
  const isCurrentPlan = activeProductId === plan.id
  const buttonDisabled = disabled
  const isBasic = buttonTheme === 'yellow'

  const label = `Elegir ${displayName}`

  const textColor = isBasic ? 'text-[#8f541f]' : 'text-[#5d3a80]'
  const checkBg = isBasic ? 'bg-[#f5a623]' : 'bg-[#8b5a8e]'

  const buttonClass = isBasic
    ? 'bg-amber-500 border-amber-600 hover:bg-amber-600'
    : 'bg-gradient-to-r from-[#8b5a8e] to-[#a06f9e] border-[#6b4470] hover:from-[#7d4f80] hover:to-[#926390]'

  return (
    <div className="relative h-full w-full min-w-0 transition-transform duration-200 origin-center hover:scale-[1.015]" style={{ aspectRatio: '353/706' }}>
      <img src={cardImage} alt="" aria-hidden="true" className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_3px_4px_rgba(91,60,24,0.16)]" draggable={false} />

      <div className="absolute inset-x-[8%] top-[8%] bottom-[7%] flex flex-col items-center text-center overflow-hidden">
        <h3 className={`text-[13px] sm:text-[15px] font-black leading-none whitespace-nowrap ${textColor}`}>
          {displayName}
        </h3>

        <div className="mt-3">
          <p className={`font-black text-[16px] sm:text-[19px] leading-none ${textColor}`}>
            ${plan.price.toLocaleString('es-AR')}
            <span className="text-[10px] sm:text-[12px] font-black"> ARS</span>
          </p>
          <p className="mt-1 text-[7px] sm:text-[10px] font-bold text-[#7b5c3c]">
            Acceso por 30 días
          </p>
        </div>

        <div className="mt-3 mb-2 w-[78%] h-px bg-gradient-to-r from-transparent via-[#c9a96e]/40 to-transparent" />

        <ul className={`w-full flex-1 overflow-hidden ${compact ? 'space-y-[2px]' : 'space-y-[3px]'}`}>
          {showSeeds && (
            <li className="flex items-start gap-1.5 text-left text-[7px] sm:text-[8px] text-[#7b5c3c] leading-tight">
              <span className={`mt-[1px] flex h-3 w-3 shrink-0 items-center justify-center rounded-full ${checkBg} text-white text-[7px] font-black`}>✓</span>
              <span>{plan.coinsAmount.toLocaleString('es-AR')} semillas</span>
            </li>
          )}

          {features.map((feature, index) => (
            <li key={index} className={`flex items-start gap-1.5 text-left text-[10px] ${compact ? 'sm:text-[10px]' : 'sm:text-[10px]'} text-[#7b5c3c] leading-tight`}>
              <span className={`mt-[1px] flex h-3 w-3 shrink-0 items-center justify-center rounded-full ${checkBg} text-white text-[7px] font-black`}>✓</span>
              <span className="font-bold">{feature}</span>
            </li>
          ))}
        </ul>

        {isCurrentPlan ? (
          <div className={`mt-2 rounded-full border-b-[3px] px-4 py-1.5 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] text-[8px] sm:text-[10px] whitespace-nowrap ${buttonClass}`}>
            Plan actual
          </div>
        ) : (
          <button
            type="button"
            onClick={() => onBuy(plan.id)}
            disabled={buttonDisabled}
            className={`mt-2 rounded-full border-b-[3px] px-4 py-1.5 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] transition-all active:translate-y-[1px] active:border-b-[1px] disabled:opacity-60 whitespace-nowrap flex items-center justify-center gap-1 text-[8px] sm:text-[10px] ${buttonClass}`}
          >
            {buying ? 'Procesando…' : label}
          </button>
        )}
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
    <div className="fixed inset-0 z-[400] flex items-center justify-center p-3 pt-16 sm:p-4 sm:pt-16 bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div role="dialog" aria-modal="true" aria-label="Planes de suscripción" className="relative z-10 w-full max-w-[720px] max-[640px]:max-w-[390px]" onClick={e => e.stopPropagation()}>
        <img src={modalBg} alt="" aria-hidden="true" className="w-full h-auto select-none pointer-events-none block" draggable={false} />

        <div className="absolute inset-0 px-[8%] pt-[9%] pb-[8%] flex flex-col overflow-hidden">
          <button onClick={onClose} aria-label="Cerrar planes de suscripción" className="absolute top-[3%] right-[3%] w-8 h-8 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20">
            <X className="w-5 h-5" />
          </button>

          <div className="text-center shrink-0">
            <h2 className="text-[16px] sm:text-[20px] font-black text-[#9b5718] leading-none">
              Planes de Suscripción
            </h2>
          </div>

          <p className="shrink-0 text-center mt-[5%] text-[10px] sm:text-[13px] font-bold text-[#7b5c3c]">
            Elegí el plan que mejor se adapte a vos
          </p>

          <div className="flex justify-center items-stretch gap-2 sm:gap-3 mt-2 px-[1%]">
            {plansLoading ? (
              <div className="flex justify-center py-12">
                <div className="w-7 h-7 border-4 border-[#8B6914] border-t-transparent rounded-full animate-spin" />
              </div>
            ) : (
              <>
                <div className="w-[28%] h-[clamp(220px,38vh,290px)]">
                  <FreeCard isCurrentPlan={!activeProductId} />
                </div>

                {basicPlan && (
                  <div className="w-[28%] h-[clamp(220px,38vh,290px)]">
                    <PaidCard
                      plan={basicPlan}
                      displayName={getDisplayName(basicPlan.planCode, basicPlan.name)}
                      cardImage={cardYellow}
                      showSeeds={false}
                      features={[
                        'Chatbot libre',
                        'Items exclusivos de la tienda',
                        'Mandalas exclusivos',
                        '3 audios por día',
                        '1.5x recompensas diarias',
                      ]}
                      buying={buyingId === basicPlan.id}
                      disabled={buyingId !== null}
                      activeProductId={activeProductId}
                      buttonTheme="yellow"
                      onBuy={handleBuy}
                    />
                  </div>
                )}

                {premiumPlan && (
                  <div className="w-[28%] h-[clamp(220px,38vh,290px)]">
                    <PaidCard
                      plan={premiumPlan}
                      displayName={getDisplayName(premiumPlan.planCode, premiumPlan.name)}
                      cardImage={cardPurple}
                      showSeeds={false}
                      compact
                      features={[
                        'Chatbot libre',
                        'Items exclusivos de la tienda',
                        'Mandalas exclusivos',
                        '1000 monedas',
                        'Audios libres',
                        '1.5x recompensas diarias',
                      ]}
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