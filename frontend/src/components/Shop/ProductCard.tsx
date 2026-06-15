import { CurrencyDollarIcon } from '@heroicons/react/24/solid'
import type { Product } from '../../api/payment'

interface ProductCardProps {
  product: Product
  buying: boolean
  disabled: boolean
  onBuy: (productId: string) => void
}

export function ProductCard({ product, buying, disabled, onBuy }: ProductCardProps) {
  return (
    <div className="bg-white rounded-2xl shadow-md p-6 flex flex-col gap-4 border border-gray-100 hover:shadow-lg transition-shadow">
      <div className="flex-1">
        <h2 className="text-xl font-bold text-gray-800 mb-1">{product.name}</h2>
        <div className="flex items-center gap-1 mb-2">
          <CurrencyDollarIcon className="w-4 h-4 text-yellow-500" />
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
        onClick={() => onBuy(product.id)}
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
          'Comprar'
        )}
      </button>
    </div>
  )
}
