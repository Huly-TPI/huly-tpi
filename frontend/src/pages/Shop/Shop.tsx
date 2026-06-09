import { useProducts } from '../../hooks/shop/useProducts'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { usePurchase } from '../../hooks/shop/usePurchase'
import { ProductCard } from '../../components/Shop/ProductCard'
import { CoinsBadge } from '../../components/Shop/CoinsBadge'

export default function Shop() {
  const { products, loading, error: productsError } = useProducts()
  const { coins } = useUserCoins()
  const { buyingId, error: purchaseError, buy } = usePurchase()

  const error = productsError ?? purchaseError

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-3xl font-bold text-gray-800">Planes y Productos</h1>
          {coins !== null && <CoinsBadge coins={coins} />}
        </div>
        <p className="text-gray-500 mb-10">Elegí el plan que mejor se adapte a vos</p>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3 text-sm text-center">
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex justify-center items-center h-40">
            <div className="w-8 h-8 border-4 border-gray-300 border-t-green-600 rounded-full animate-spin" />
          </div>
        ) : (
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
        )}
      </div>
    </div>
  )
}
