import { useCallback, type ReactNode } from 'react'
import { X } from 'lucide-react'
import modalBg from '../../assets/suscription/modalSuscripcion.webp'
import modalBgMobile from '../../assets/suscription/modalSuscripcionMobile.webp'
import cardGreen from '../../assets/suscription/cardGreen.webp'
import cardYellow from '../../assets/suscription/cardYellow.webp'
import cardPurple from '../../assets/suscription/cardPurple.webp'
import { usePlans } from '../../hooks/shop/usePlans'
import { useMembership } from '../../hooks/shop/useMembership'
import { usePurchase } from '../../hooks/shop/usePurchase'
import { useRefreshOnReturn } from '../../hooks/shop/useRefreshOnReturn'
import { useMediaQuery } from '../../hooks/useMediaQuery'
import type { Plan } from '../../api/payment'

const MOBILE_QUERY = '(max-width: 640px)'
const MOBILE_ASPECT = '1 / 1'

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

const PLAN_CARD: Record<string, { image: string; theme: 'yellow' | 'purple' }> = {
  BASIC: { image: cardYellow, theme: 'yellow' },
  PREMIUM: { image: cardPurple, theme: 'purple' },
}

function buildFeatures(plan: Plan): string[] {
  const chat = plan.chatDailyLimit == null ? 'Chatbot libre' : `${plan.chatDailyLimit} mensajes al día`
  const audio = plan.audioDailyLimit == null ? 'Audios libres' : `${plan.audioDailyLimit} audios por día`
  const features = [chat, 'Items exclusivos de la tienda', 'Mandalas exclusivos', audio, '1.5x recompensas diarias']
  if (plan.coinsAmount > 0) features.push(`${plan.coinsAmount} monedas`)
  return features
}

function FreeCard({ isCurrentPlan }: { isCurrentPlan: boolean }) {
  return (
    <div className="relative h-full w-full min-w-0 transition-transform duration-200 origin-center hover:scale-[1.015]" style={{ aspectRatio: '353/706' }}>
      <img src={cardGreen} alt="" aria-hidden="true" className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_3px_4px_rgba(0,80,0,0.16)]" draggable={false} />

      <div className="absolute inset-x-[8%] top-[7%] bottom-[8%] max-[640px]:inset-x-[5%] flex flex-col items-center text-center overflow-hidden">
        <h3 className="text-[13px] sm:text-[15px] lg:text-[18px] font-black text-[#1a4a1a] leading-none">Gratuito</h3>
        <div className="mt-3 max-[640px]:mt-2">
          <p className="font-black text-[16px] sm:text-[19px] lg:text-[24px] text-[#2a6a2a] leading-none">$0</p>
          <p className="mt-1 text-[7px] sm:text-[10px] lg:text-[12px] font-bold text-[#3a5c2a]">Siempre gratis</p>
        </div>

        <div className="mt-3 mb-2 max-[640px]:mt-2 max-[640px]:mb-1 w-[78%] h-px bg-gradient-to-r from-transparent via-[#6abf55]/35 to-transparent" />

        <ul className="w-full flex-1 space-y-1">
          {FREE_FEATURES.map((feature, index) => (
            <li key={index} className="flex items-start gap-1.5 text-left text-[10px] sm:text-[10px] lg:text-[12px] max-[640px]:text-[9px] text-[#3a5c2a] leading-tight max-[640px]:leading-[1.15]">
              <span className="mt-[1px] flex h-3 w-3 shrink-0 items-center justify-center rounded-full bg-[#8ccf65] text-white text-[7px] font-black">✓</span>
              <span className="font-bold">{feature}</span>
            </li>
          ))}
        </ul>

        <div className="mt-2 rounded-full border-b-[3px] px-4 py-1.5 max-[640px]:px-3 max-[640px]:py-1 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] text-[8px] sm:text-[10px] lg:text-[12px] whitespace-nowrap bg-[#7b8f45] border border-[#5f7332]">
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
  const shortName = displayName.replace(/^Plan\s+/i, '')
  const label = `Elegir ${shortName}`
  const textColor = isBasic ? 'text-[#8f541f]' : 'text-[#5d3a80]'
  const checkBg = isBasic ? 'bg-[#f5a623]' : 'bg-[#8b5a8e]'

  const buttonClass = isBasic
    ? 'bg-amber-500 border-amber-600 hover:bg-amber-600'
    : 'bg-gradient-to-r from-[#8b5a8e] to-[#a06f9e] border-[#6b4470] hover:from-[#7d4f80] hover:to-[#926390]'

  return (
    <div className="relative h-full w-full min-w-0 transition-transform duration-200 origin-center hover:scale-[1.015]" style={{ aspectRatio: '353/706' }}>
      <img src={cardImage} alt="" aria-hidden="true" className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_3px_4px_rgba(91,60,24,0.16)]" draggable={false} />

      <div className="absolute inset-x-[8%] top-[8%] bottom-[7%] max-[640px]:inset-x-[5%] flex flex-col items-center text-center overflow-hidden">
        <h3 className={`text-[13px] sm:text-[15px] lg:text-[18px] font-black leading-none whitespace-nowrap max-[640px]:whitespace-normal max-[640px]:leading-tight ${textColor}`}>
          <span className="max-[640px]:hidden">{displayName}</span>
          <span className="hidden max-[640px]:inline">{shortName}</span>
        </h3>

        <div className="mt-3 max-[640px]:mt-2">
          <p className={`font-black text-[16px] sm:text-[19px] lg:text-[24px] leading-none ${textColor}`}>
            ${plan.price.toLocaleString('es-AR')}
            <span className="text-[10px] sm:text-[12px] lg:text-[14px] font-black"> ARS</span>
          </p>
          <p className="mt-1 text-[7px] sm:text-[10px] lg:text-[12px] font-bold text-[#7b5c3c]">
            Acceso por 30 días
          </p>
        </div>

        <div className="mt-3 mb-2 max-[640px]:mt-2 max-[640px]:mb-1 w-[78%] h-px bg-gradient-to-r from-transparent via-[#c9a96e]/40 to-transparent" />

        <ul className={`w-full flex-1 overflow-hidden ${compact ? 'space-y-[2px]' : 'space-y-[3px]'}`}>
          {showSeeds && (
            <li className="flex items-start gap-1.5 text-left text-[7px] sm:text-[8px] text-[#7b5c3c] leading-tight">
              <span className={`mt-[1px] flex h-3 w-3 shrink-0 items-center justify-center rounded-full ${checkBg} text-white text-[7px] font-black`}>✓</span>
              <span>{plan.coinsAmount.toLocaleString('es-AR')} semillas</span>
            </li>
          )}

          {features.map((feature, index) => (
            <li key={index} className={`flex items-start gap-1.5 text-left text-[10px] sm:text-[10px] lg:text-[12px] max-[640px]:text-[9px] text-[#7b5c3c] leading-tight max-[640px]:leading-[1.15]`}>
              <span className={`mt-[1px] flex h-3 w-3 shrink-0 items-center justify-center rounded-full ${checkBg} text-white text-[7px] font-black`}>✓</span>
              <span className="font-bold">{feature}</span>
            </li>
          ))}
        </ul>

        {isCurrentPlan ? (
          <div className={`mt-2 rounded-full border-b-[3px] px-4 py-1.5 max-[640px]:px-3 max-[640px]:py-1 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] text-[8px] sm:text-[10px] lg:text-[12px] whitespace-nowrap ${buttonClass}`}>
            Plan actual
          </div>
        ) : (
          <button
            type="button"
            onClick={() => onBuy(plan.id)}
            disabled={buttonDisabled}
            className={`mt-2 rounded-full border-b-[3px] px-4 py-1.5 max-[640px]:px-2.5 max-[640px]:py-1 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] transition-all active:translate-y-[1px] active:border-b-[1px] disabled:opacity-60 whitespace-nowrap flex items-center justify-center gap-1 text-[8px] sm:text-[10px] lg:text-[12px] ${buttonClass}`}
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
  const isMobile = useMediaQuery(MOBILE_QUERY)

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

  const activeProductId = membership?.active ? membership.productId : null

  const sortedPlans = [...plans].sort((a, b) => a.price - b.price)
  const many = 1 + sortedPlans.length > 3
  const gapClass = many ? 'gap-1 sm:gap-2 lg:gap-3' : 'gap-1.5 sm:gap-3 lg:gap-6'
  const cardClass = many
    ? 'flex-1 min-w-0 max-w-[30%] h-full max-[640px]:max-w-none'
    : 'w-[28%] shrink-0 h-full max-[640px]:w-auto max-[640px]:flex-1 max-[640px]:shrink max-[640px]:min-w-0'

  /* Contenido compartido por desktop y mobile. */
  const content: ReactNode = (
    <>
      <button onClick={onClose} aria-label="Cerrar planes de suscripción" className="absolute top-[3%] right-[3%] w-8 h-8 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20">
        <X className="w-5 h-5" />
      </button>

      <div className="text-center shrink-0">
        <h2 className="text-[13px] sm:text-[20px] lg:text-[24px] font-black text-[#9b5718] leading-none">
          Planes de Suscripción
        </h2>
      </div>

      <p className="shrink-0 text-center mt-[5%] max-[640px]:text-[10px] sm:text-[13px] lg:text-[15px] font-bold text-[#7b5c3c]">
        Elegí el plan que mejor se adapte a vos
      </p>

      <div className={`flex justify-center items-stretch mt-2 px-[1%] max-[640px]:px-0 flex-1 min-h-0 lg:mt-4 ${gapClass}`}>
        {plansLoading ? (
          <div className="flex justify-center py-12">
            <div className="w-7 h-7 border-4 border-[#8B6914] border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <>
            <div className={cardClass}>
              <FreeCard isCurrentPlan={!activeProductId} />
            </div>
            {sortedPlans.map(plan => {
              const card = PLAN_CARD[plan.planCode] ?? { image: cardPurple, theme: 'purple' as const }
              const features = buildFeatures(plan)
              return (
                <div key={plan.id} className={cardClass}>
                  <PaidCard
                    plan={plan}
                    displayName={plan.name}
                    cardImage={card.image}
                    features={features}
                    compact={features.length > 5}
                    buying={buyingId === plan.id}
                    disabled={buyingId !== null}
                    activeProductId={activeProductId}
                    buttonTheme={card.theme}
                    onBuy={handleBuy}
                  />
                </div>
              )
            })}
          </>
        )}
      </div>

      {(plansError || purchaseError) && (
        <p className="shrink-0 mt-1 text-red-600 text-[10px] text-center px-4 font-bold">
          {plansError ?? purchaseError}
        </p>
      )}
    </>
  )

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center px-1 pt-16 pb-2 sm:p-4 sm:pt-16 bg-black/50 backdrop-blur-sm" onClick={onClose}>
      {isMobile ? (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Planes de suscripción"
          onClick={e => e.stopPropagation()}
          style={{
            backgroundImage: `url(${modalBgMobile})`,
            backgroundSize: '100% 100%',
            backgroundRepeat: 'no-repeat',
            aspectRatio: MOBILE_ASPECT,
          }}
          className="relative z-10 w-[96vw] max-w-[520px] max-h-[500px] px-[5%] pt-[7%] pb-[3%] flex flex-col overflow-hidden"
        >
          {content}
        </div>
      ) : (
        <div role="dialog" aria-modal="true" aria-label="Planes de suscripción" className="relative z-10 w-full max-w-[720px] lg:max-w-[980px]" onClick={e => e.stopPropagation()}>
          <img src={modalBg} alt="" aria-hidden="true" className="w-full h-auto select-none pointer-events-none block" draggable={false} />

          <div className="absolute inset-0 px-[10.5%] pt-[9%] pb-[8%] flex flex-col overflow-hidden lg:px-[9.5%]">
            {content}
          </div>
        </div>
      )}
    </div>
  )
}