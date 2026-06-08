import { useEffect, useState } from 'react'
import { getProducts, createPreference, getMyCoins, type Product } from '../../api/payment'

export default function Shop() {
  const [products, setProducts] = useState<Product[]>([])
  const [coins, setCoins] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [buyingId, setBuyingId] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([getProducts(), getMyCoins()])
      .then(([prods, { coins: c }]) => {
        setProducts(prods)
        setCoins(c)
      })
      .catch(() => setError('No se pudieron cargar los productos.'))
      .finally(() => setLoading(false))
  }, [])

  async function handleBuy(productId: string) {
    setBuyingId(productId)
    try {
      const { initPoint } = await createPreference(productId)
      window.location.href = initPoint
    } catch {
      setError('Error al iniciar el pago. Intentá de nuevo.')
      setBuyingId(null)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-2">
          <h1 className="text-3xl font-bold text-gray-800">Planes y Productos</h1>
          {coins !== null && (
            <div className="flex items-center gap-2 bg-yellow-50 border border-yellow-200 rounded-xl px-4 py-2">
              <span className="text-yellow-500 text-xl">🪙</span>
              <span className="text-yellow-700 font-bold text-lg">{coins.toLocaleString('es-AR')}</span>
              <span className="text-yellow-600 text-sm">monedas</span>
            </div>
          )}
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
              <div
                key={product.id}
                className="bg-white rounded-2xl shadow-md p-6 flex flex-col gap-4 border border-gray-100 hover:shadow-lg transition-shadow"
              >
                <div className="flex-1">
                  <h2 className="text-xl font-bold text-gray-800 mb-1">{product.name}</h2>
                  <div className="flex items-center gap-1 mb-2">
                    <span className="text-yellow-500">🪙</span>
                    <span className="text-yellow-700 font-semibold text-sm">
                      {product.coinsAmount.toLocaleString('es-AR')} monedas
                    </span>
                  </div>
                  <p className="text-gray-500 text-sm leading-relaxed">{product.description}</p>
                </div>
                <div className="text-2xl font-extrabold text-green-700">
                  ${product.price.toLocaleString('es-AR')}
                  <span className="text-sm font-normal text-gray-400 ml-1">ARS</span>
                </div>
                <button
                  onClick={() => handleBuy(product.id)}
                  disabled={buyingId !== null}
                  className="w-full py-3 rounded-xl bg-green-600 text-white font-semibold text-sm
                             hover:bg-green-700 active:scale-95 transition-all
                             disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  {buyingId === product.id ? (
                    <>
                      <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                      Procesando…
                    </>
                  ) : (
                    'Comprar'
                  )}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
