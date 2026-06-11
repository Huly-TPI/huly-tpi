import { useState } from 'react'
import { useProducts } from '../../hooks/shop/useProducts'
import { usePlans } from '../../hooks/shop/usePlans'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { useMembership } from '../../hooks/shop/useMembership'
import { usePurchase } from '../../hooks/shop/usePurchase'
import { ProductCard } from '../../components/Shop/ProductCard'
import { PlanCard } from '../../components/Shop/PlanCard'
import { CoinsBadge } from '../../components/Shop/CoinsBadge'
import { MembershipBadge } from '../../components/Shop/MembershipBadge'

type ShopTab = 'coins' | 'subscriptions'

export default function Shop() {
  const [tab, setTab] = useState<ShopTab>('coins')

  const { products, loading: productsLoading, error: productsError } = useProducts()
  const { plans, loading: plansLoading, error: plansError } = usePlans()
  const { coins } = useUserCoins()
  const { membership } = useMembership()
  const { buyingId, error: purchaseError, buy } = usePurchase()

  const loading = tab === 'coins' ? productsLoading : plansLoading
  const error = (tab === 'coins' ? productsError : plansError) ?? purchaseError

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
                onBuy={buy}
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
                onBuy={buy}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
