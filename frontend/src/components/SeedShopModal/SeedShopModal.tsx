import { useCallback } from 'react'
import { XMarkIcon } from '@heroicons/react/24/solid'
import modalBg from '../../assets/rewards/modal.webp'
import cardBg from '../../assets/shop-seed/card.png'
import startIcon from '../../assets/shop-seed/start.png'
import seedIcon from '../../assets/rewards/seed.webp'
import oneSeedIcon from '../../assets/shop-seed/1seed.png'
import twoSeedsIcon from '../../assets/shop-seed/2seed.png'
import threeSeedsIcon from '../../assets/shop-seed/3seed.png'
import { useProducts } from '../../hooks/shop/useProducts'
import { usePurchase } from '../../hooks/shop/usePurchase'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { useRefreshOnReturn } from '../../hooks/shop/useRefreshOnReturn'
import type { Product } from '../../api/payment'

interface SeedShopModalProps {
  isOpen: boolean
  onClose: () => void
}

interface SeedCardProps {
  product: Product
  buying: boolean
  disabled: boolean
  featured?: boolean
  image: string
  onBuy: (id: string) => void
}

function SeedCard({ product, buying, disabled, featured = false, image, onBuy }: SeedCardProps) {
  return (
    <div className={`relative aspect-[3/4] w-full transition-transform duration-200 origin-center hover:scale-[1.03] ${featured ? 'scale-[1.03] z-10' : ''}`}>
      {/* Star icon for featured */}
      {featured && (
        <div className="absolute top-1 right-1 z-30 rotate-12">
          <img src={startIcon} alt="" aria-hidden="true" className="w-7 h-7 sm:w-8 sm:h-8 object-contain drop-shadow-[0_2px_4px_rgba(180,120,30,0.4)]" />
        </div>
      )}

      {/* Golden glow behind card when featured */}
      {featured && (
        <div className="absolute -inset-1 rounded-2xl bg-gradient-to-b from-[#f0b83a]/30 via-[#d4952b]/20 to-[#f0b83a]/30 blur-sm pointer-events-none" />
      )}

      <img
        src={cardBg}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-fill pointer-events-none select-none drop-shadow-[0_3px_4px_rgba(91,60,24,0.16)]"
        draggable={false}
      />

      <div className="absolute inset-[8%] flex flex-col items-center text-center">
        {/* Título */}
        <h3 className="text-[12px] sm:text-[14px] font-black text-[#5b3b1b] leading-tight line-clamp-2">
          {product.name}
        </h3>

        {/* Imagen con glow */}
        <div className="relative mt-auto mb-1">
          <div className={`absolute inset-0 rounded-full blur-md scale-125 ${featured ? 'bg-[#f0b83a]/30' : 'bg-[#a8d860]/25'}`} />
          <img
            src={image}
            alt=""
            aria-hidden="true"
            className={`relative aspect-square object-contain ${featured ? 'w-16 sm:w-[4.5rem] drop-shadow-[0_2px_8px_rgba(180,120,30,0.4)]' : 'w-14 sm:w-16 drop-shadow-[0_2px_6px_rgba(77,138,45,0.3)]'}`}
          />
        </div>

        {/* Badge de semillas */}
        <div className="flex items-center gap-1 rounded-full border border-[#b98a54] bg-[#f3d7a6]/70 px-2 py-0.5 shadow-[inset_0_1px_0_rgba(255,255,255,0.35)]">
          <img
            src={seedIcon}
            alt=""
            aria-hidden="true"
            className="w-3 h-3 object-contain shrink-0"
          />
          <span className="text-[9px] sm:text-[11px] font-black whitespace-nowrap text-[#3d7a1a]">
            {product.coinsAmount.toLocaleString('es-AR')} semillas
          </span>
        </div>

        {/* Descripción */}
        <p className="mt-1 text-[7px] sm:text-[9px] text-[#7b5c3c] leading-snug line-clamp-2 px-1">
          {product.description}
        </p>

        {/* Separador */}
        <div className={`mt-auto mb-1 w-[60%] h-px bg-gradient-to-r from-transparent to-transparent ${featured ? 'via-[#d4a23a]/50' : 'via-[#c9a96e]/40'}`} />

        {/* Precio */}
        <span className="font-black leading-none tracking-tight text-[15px] sm:text-[17px] text-[#8f541f]">
          ${product.price.toLocaleString('es-AR')}
          <span className="text-[8px] sm:text-[9px] font-bold text-[#7b5c3c] ml-0.5">
            ARS
          </span>
        </span>

        {/* Botón */}
        <button
          type="button"
          onClick={() => onBuy(product.id)}
          disabled={disabled}
          className="mt-1.5 rounded-lg bg-[#7b8f45] border border-[#5f7332] border-b-[3px] px-4 sm:px-5 py-1 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] transition-all active:translate-y-[1px] active:border-b-[1px] disabled:opacity-60 whitespace-nowrap flex items-center justify-center gap-1 text-[9px] sm:text-[11px]"
        >
          {buying ? (
            <>
              <span className="w-2.5 h-2.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
              Procesando…
            </>
          ) : (
            'Comprar'
          )}
        </button>
      </div>
    </div>
  )
}

export default function SeedShopModal({ isOpen, onClose }: SeedShopModalProps) {
  const { products, loading, error } = useProducts()
  const { coins, refresh: refreshCoins } = useUserCoins()
  const { buyingId, error: purchaseError, buy } = usePurchase()
  const productImages = [oneSeedIcon, twoSeedsIcon, threeSeedsIcon]

  const markPendingPayment = useRefreshOnReturn(
    useCallback(() => { refreshCoins() }, [refreshCoins]),
  )

  const handleBuy = useCallback(
    (productId: string) => {
      markPendingPayment()
      buy(productId)
    },
    [markPendingPayment, buy],
  )

  if (!isOpen) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Tienda de semillas"
        className="relative z-10 w-full max-w-[730px] max-[640px]:max-w-[370px]"
        onClick={e => e.stopPropagation()}
      >
        <img
          src={modalBg}
          alt=""
          aria-hidden="true"
          className="w-full h-auto max-[640px]:h-[580px] max-[640px]:object-fill select-none pointer-events-none block"
          draggable={false}
        />

        <div className="absolute inset-0 px-[9%] pt-[19%] pb-[7%] max-[640px]:px-[8%] max-[640px]:pt-[22%] max-[640px]:pb-[6%] flex flex-col">
          {/* Botón cerrar */}
          <button
            onClick={onClose}
            aria-label="Cerrar tienda de semillas"
            className="absolute top-[4%] right-[4%] w-8 h-8 max-[640px]:w-7 max-[640px]:h-7 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20"
          >
            <XMarkIcon className="w-5 h-5 max-[640px]:w-4 max-[640px]:h-4" />
          </button>

          {/* Saldo de semillas */}
          {coins !== null && (
            <div className="absolute top-[4%] right-[13%] max-[640px]:right-[16%] z-20 rounded-full bg-[#f3d7a6]/90 border border-[#b98a54] px-2.5 max-[640px]:px-2 shadow-[inset_0_1px_0_rgba(255,255,255,0.45),0_2px_4px_rgba(91,60,24,0.12)]">
              <div className="flex items-center gap-0.5 py-0.5">
                <img src={seedIcon} alt="" aria-hidden="true" className="w-4 h-4 max-[640px]:w-3.5 max-[640px]:h-3.5 object-contain shrink-0" />
                <span className="text-[10px] sm:text-[11px] max-[640px]:text-[9px] font-black text-[#68451f] leading-none">
                  <span className="text-[#4d8a2d]">{coins.toLocaleString('es-AR')}</span>
                </span>
              </div>
            </div>
          )}

          {/* Header */}
          <div className="text-center shrink-0">
            <img
              src={seedIcon}
              alt=""
              aria-hidden="true"
              className="absolute top-[6.5%] max-[640px]:top-[5%] left-1/2 -translate-x-1/2 w-18 h-18 sm:w-20 sm:h-20 max-[640px]:w-14 max-[640px]:h-14 object-contain pointer-events-none select-none z-10"
              draggable={false}
            />

            <h2 className="text-[22px] sm:text-[27px] max-[640px]:text-[20px] font-black text-[#9b5718] mt-2 leading-none">
              Tienda de semillas
            </h2>

            <p className="mt-1 text-[11px] sm:text-[13px] max-[640px]:text-[10px] font-bold text-[#7b5c3c]">
              Comprá semillas para decorar tu jardín
            </p>
          </div>

          {/* Grid de cards */}
          <div className="grid grid-cols-3 max-[640px]:grid-cols-2 gap-x-4 max-[640px]:gap-x-2 gap-y-2 max-[640px]:mt-3 px-[2%] max-[640px]:px-0 flex-1 min-h-0 overflow-visible">
            {loading ? (
              <div className="col-span-3 max-[640px]:col-span-2 flex justify-center py-12">
                <div className="w-7 h-7 border-4 border-[#8B6914] border-t-transparent rounded-full animate-spin" />
              </div>
            ) : (
              products.map((product, index) => (
                <SeedCard
                  key={product.id}
                  product={product}
                  buying={buyingId === product.id}
                  disabled={buyingId !== null}
                  featured={index === products.length - 1}
                  image={productImages[index] ?? seedIcon}
                  onBuy={handleBuy}
                />
              ))
            )}
          </div>

          {(error || purchaseError) && (
            <p className="shrink-0 mt-1 text-red-600 text-[10px] text-center px-4 font-bold">
              {error ?? purchaseError}
            </p>
          )}
        </div>
      </div>
    </div>
  )
}