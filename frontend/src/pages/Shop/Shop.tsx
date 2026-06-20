import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { X } from 'lucide-react'
import { useProducts } from '../../hooks/shop/useProducts'
import { usePlans } from '../../hooks/shop/usePlans'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { useMembership } from '../../hooks/shop/useMembership'
import { usePurchase } from '../../hooks/shop/usePurchase'
import { useRefreshOnReturn } from '../../hooks/shop/useRefreshOnReturn'
import { ProductCard } from '../../components/Shop/ProductCard'
import { PlanCard } from '../../components/Shop/PlanCard'
import { CoinsBadge } from '../../components/Shop/CoinsBadge'
import { MembershipBadge } from '../../components/Shop/MembershipBadge'
import { DailyRewardCalendar } from '../../components/Shop/DailyRewardCalendar'

type ShopTab = 'coins' | 'subscriptions'

const PAYMENT_BANNERS: Record<string, { text: string; className: string }> = {
  success: {
    text: '¡Pago aprobado! Acreditamos tu compra.',
    className: 'bg-green-50 border-green-200 text-green-700',
  },
  pending: {
    text: 'Tu pago quedó pendiente de aprobación. Te avisaremos cuando se acredite.',
    className: 'bg-yellow-50 border-yellow-200 text-yellow-700',
  },
  failure: {
    text: 'El pago no se completó. Podés intentar de nuevo.',
    className: 'bg-red-50 border-red-200 text-red-700',
  },
}

export default function Shop() {
  const [tab, setTab] = useState<ShopTab>('coins')
  const [searchParams, setSearchParams] = useSearchParams()
  const [paymentBanner, setPaymentBanner] = useState<string | null>(null)

  const { products, loading: productsLoading, error: productsError } = useProducts()
  const { plans, loading: plansLoading, error: plansError } = usePlans()
  const { coins, refresh: refreshCoins } = useUserCoins()
  const { membership, refresh: refreshMembership } = useMembership()
  const { buyingId, error: purchaseError, buy } = usePurchase()

  // Tras pagar en MercadoPago, el webhook acredita monedas / activa el plan de
  // forma asíncrona. Al volver a la pestaña re-pedimos ambos (con reintentos).
  const markPendingPayment = useRefreshOnReturn(
    useCallback(() => {
      refreshCoins()
      refreshMembership()
    }, [refreshCoins, refreshMembership]),
  )

  const handleBuy = useCallback(
    (productId: string) => {
      markPendingPayment()
      buy(productId)
    },
    [markPendingPayment, buy],
  )

  // MercadoPago vuelve al sitio con ?payment=success|pending|failure: mostramos
  // un cartel, refrescamos saldo/membresía si fue exitoso y limpiamos el query param.
  const paymentStatus = searchParams.get('payment')
  useEffect(() => {
    if (!paymentStatus) return
    setPaymentBanner(paymentStatus)
    if (paymentStatus === 'success') {
      refreshCoins()
      refreshMembership()
    }
    const next = new URLSearchParams(searchParams)
    next.delete('payment')
    setSearchParams(next, { replace: true })
  }, [paymentStatus, searchParams, setSearchParams, refreshCoins, refreshMembership])

  useEffect(() => {
    if (!paymentBanner) return
    const id = setTimeout(() => setPaymentBanner(null), 6000)
    return () => clearTimeout(id)
  }, [paymentBanner])

  const loading = tab === 'coins' ? productsLoading : plansLoading
  const error = (tab === 'coins' ? productsError : plansError) ?? purchaseError
  const activeProductId = membership?.active ? membership.productId : null

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-3xl font-bold text-gray-800">Planes y Productos</h1>
          <div className="flex items-center gap-3">
            {membership !== null && <MembershipBadge membership={membership} />}
            {coins !== null && <CoinsBadge coins={coins} />}
          </div>
        </div>
        <p className="text-gray-500 mb-6">Elegí el plan que mejor se adapte a vos</p>

        {paymentBanner && PAYMENT_BANNERS[paymentBanner] && (
          <div
            className={`mb-6 flex items-center justify-between gap-3 rounded-lg border px-4 py-3 text-sm ${PAYMENT_BANNERS[paymentBanner].className}`}
          >
            <span>{PAYMENT_BANNERS[paymentBanner].text}</span>
            <button
              type="button"
              aria-label="Cerrar"
              onClick={() => setPaymentBanner(null)}
              className="shrink-0 rounded-full p-1 hover:bg-black/5 transition bg-transparent border-0 cursor-pointer"
            >
              <X className="w-4 h-4" strokeWidth={2} />
            </button>
          </div>
        )}

        <DailyRewardCalendar onClaimed={refreshCoins} />

        <div className="inline-flex items-center gap-1 bg-gray-100 rounded-xl p-1 mb-8">
          <button
            type="button"
            onClick={() => setTab('coins')}
            className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
              tab === 'coins'
                ? 'bg-white text-green-700 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            Monedas
          </button>
          <button
            type="button"
            onClick={() => setTab('subscriptions')}
            className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
              tab === 'subscriptions'
                ? 'bg-white text-green-700 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            Suscripciones
          </button>
        </div>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3 text-sm text-center">
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex justify-center items-center h-40">
            <div className="w-8 h-8 border-4 border-gray-300 border-t-green-600 rounded-full animate-spin" />
          </div>
        ) : tab === 'coins' ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                buying={buyingId === product.id}
                disabled={buyingId !== null}
                onBuy={handleBuy}
              />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {plans.map((plan) => (
              <PlanCard
                key={plan.id}
                plan={plan}
                buying={buyingId === plan.id}
                disabled={buyingId !== null}
                activeProductId={activeProductId}
                onBuy={handleBuy}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
