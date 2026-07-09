import { useCallback, type ReactNode } from 'react'
import { X, ShieldCheck } from 'lucide-react'
import modalBg from '../../assets/rewards/modal.webp'
import modalBgMobile from '../../assets/rewards/modalMobile.webp'
import cardBg from '../../assets/shop-seed/cardWhite.webp'
import seedIcon from '../../assets/rewards/seed.webp'
import violeta from '../../assets/shop-seed/violeta.webp'
import startIcon from '../../assets/shop-seed/start.webp'
import oneSeedIcon from '../../assets/shop-seed/1seed.webp'
import twoSeedsIcon from '../../assets/shop-seed/2seed.webp'
import threeSeedsIcon from '../../assets/shop-seed/3seed.webp'
import { useProducts } from '../../hooks/shop/useProducts'
import { usePurchase } from '../../hooks/shop/usePurchase'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { useRefreshOnReturn } from '../../hooks/shop/useRefreshOnReturn'
import { useMediaQuery } from '../../hooks/UseMediaQuery'
import type { Product } from '../../api/payment'

const MOBILE_QUERY = '(max-width: 640px)'
const MOBILE_ASPECT = '1024 / 1536'

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
    <div className={`relative aspect-[3/4] w-full transition-transform duration-200 origin-center hover:scale-[1.03] ${featured ? 'scale-[1.03] z-10 mt-1' : ''}`}>
      {/* Ribbon "Mejor valor" for featured */}
      {featured && (
        <div className="absolute -top-4 left-1/2 -translate-x-1/2 z-30 w-[75%]">
          <img
            src={violeta}
            alt=""
            aria-hidden="true"
            className="w-full h-auto object-contain pointer-events-none select-none"
            draggable={false}
          />
          <span className="absolute inset-0 flex items-center justify-center gap-1 text-[8px] sm:text-[10px] font-black text-white whitespace-nowrap tracking-wide drop-shadow-[0_1px_2px_rgba(0,0,0,0.3)]">
            <img src={startIcon} alt="" aria-hidden="true" className="w-3.5 h-3.5 sm:w-4 sm:h-4 object-contain" />
            Mejor valor
          </span>
        </div>
      )}

      {/* Glow behind card when featured - violeta tones */}
      {featured && (
        <div className="absolute -inset-1 rounded-2xl bg-gradient-to-b from-[#9b6b9e]/25 via-[#7a4f7d]/15 to-[#9b6b9e]/25 blur-sm pointer-events-none" />
      )}

      <img
        src={cardBg}
        alt=""
        aria-hidden="true"
        className={`absolute inset-0 w-full h-full object-fill pointer-events-none select-none ${featured ? 'drop-shadow-[0_4px_10px_rgba(120,70,130,0.25)]' : 'drop-shadow-[0_3px_4px_rgba(91,60,24,0.16)]'}`}
        draggable={false}
      />

      <div className="absolute inset-[8%] flex flex-col items-center text-center max-[640px]:justify-center max-[640px]:gap-1.5">
        {/* Título */}
        <h3 className={`text-[12px] sm:text-[14px] font-black text-[#5b3b1b] leading-tight line-clamp-2 ${featured ? 'mt-2 max-[640px]:mt-1' : ''}`}>
          {product.name.replace('Premium', 'Avanzado')}
        </h3>

        {/* Imagen con glow (oculta en mobile) */}
        <div className="relative mt-auto mb-1 max-[640px]:hidden">
          <div className={`absolute inset-0 rounded-full blur-md scale-125 ${featured ? 'bg-[#9b6b9e]/20' : 'bg-[#a8d860]/25'}`} />
          <img
            src={image}
            alt=""
            aria-hidden="true"
            className={`relative aspect-square object-contain ${featured ? 'w-16 sm:w-[4.5rem] max-[640px]:w-11' : 'w-14 sm:w-16 max-[640px]:w-11'} ${featured ? 'drop-shadow-[0_2px_8px_rgba(120,70,130,0.3)]' : 'drop-shadow-[0_2px_6px_rgba(77,138,45,0.3)]'}`}
          />
        </div>

        {/* Badge de semillas */}
        <div className="flex items-center gap-1 rounded-full border border-[#b98a54] bg-[#f3d7a6]/70 px-2 py-0.5 shadow-[inset_0_1px_0_rgba(255,255,255,0.35)]">
          <span className="text-[9px] sm:text-[11px] font-black whitespace-nowrap text-[#3d7a1a]">
            {product.coinsAmount.toLocaleString('es-AR')} semillas
          </span>
        </div>

        {/* Descripción (oculta en mobile para que el modal entre sin scroll) */}
        {/* Descripción (oculta en mobile) */}
        <p className="mt-1 max-[640px]:hidden text-[7px] sm:text-[9px] text-[#7b5c3c] leading-snug line-clamp-2 px-1">
          {product.description}
        </p>

        {/* Separador */}
        <div className={`mt-auto mb-1 max-[640px]:mt-0 w-[60%] h-px bg-gradient-to-r from-transparent to-transparent ${featured ? 'via-[#9b6b9e]/40' : 'via-[#c9a96e]/40'}`} />

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
          className={`mt-1.5 max-[640px]:mt-1 rounded-lg border-b-[3px] px-4 sm:px-5 py-1 font-black text-white shadow-[0_2px_2px_rgba(91,60,24,0.18)] transition-all active:translate-y-[1px] active:border-b-[1px] disabled:opacity-60 whitespace-nowrap flex items-center justify-center gap-1 text-[9px] sm:text-[11px] ${featured ? 'bg-gradient-to-r from-[#8b5a8e] to-[#a06f9e] border-[#6b4470] hover:from-[#7d4f80] hover:to-[#926390]' : 'bg-[#7b8f45] border border-[#5f7332] hover:bg-[#6d8139]'}`}
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
  const { refresh: refreshCoins } = useUserCoins()
  const { buyingId, error: purchaseError, buy } = usePurchase()
  const isMobile = useMediaQuery(MOBILE_QUERY)
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

  /* Contenido compartido por desktop y mobile. */
  const content: ReactNode = (
    <>
      {/* Botón cerrar */}
      <button
        onClick={onClose}
        aria-label="Cerrar tienda de semillas"
        className="absolute top-[4%] right-[4%] max-[640px]:top-[15%] max-[640px]:right-[8%] w-8 h-8 max-[640px]:w-7 max-[640px]:h-7 flex items-center justify-center rounded-full bg-[#a06f9e] hover:bg-[#875a86] text-white shadow-md transition z-20"
      >
        <X className="w-5 h-5 max-[640px]:w-4 max-[640px]:h-4" />
      </button>

      {/* Header */}
      <div className="text-center shrink-0">
        <img
          src={seedIcon}
          alt=""
          aria-hidden="true"
          className="absolute top-[6.5%] max-[640px]:top-[8%] left-1/2 -translate-x-1/2 w-16 h-16 sm:w-20 sm:h-20 max-[640px]:w-14 max-[640px]:h-14 object-contain pointer-events-none select-none z-10"
          draggable={false}
        />

        <h2 className="mt-2 text-[22px] sm:text-[27px] max-[640px]:text-[20px] font-black text-[#9b5718] leading-none">
          Tienda de semillas
        </h2>

        <p className="mt-1 text-[11px] sm:text-[13px] max-[640px]:text-[10px] font-bold text-[#7b5c3c]">
          Comprá semillas para decorar tu jardín
        </p>
      </div>

      {/* Grid de cards */}
      <div className="grid grid-cols-3 max-[640px]:grid-cols-2 gap-x-4 max-[640px]:gap-x-2 gap-y-2 max-[640px]:mt-3 px-[2%] max-[640px]:px-0 overflow-visible">
        {loading ? (
          <div className="col-span-3 max-[640px]:col-span-2 flex justify-center py-12">
            <div className="w-7 h-7 border-4 border-[#8B6914] border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          products.map((product, index) => {
            const isFeatured = index === products.length - 1
            const centerAlone = isFeatured && products.length % 2 === 1
            return (
              <div
                key={product.id}
                className={centerAlone ? 'max-[640px]:col-span-2 max-[640px]:w-1/2 max-[640px]:justify-self-center' : ''}
              >
                <SeedCard
                  product={product}
                  buying={buyingId === product.id}
                  disabled={buyingId !== null}
                  featured={isFeatured}
                  image={productImages[index] ?? seedIcon}
                  onBuy={handleBuy}
                />
              </div>
            )
          })
        )}
      </div>

      {/* Mensaje de confianza */}
      <div className="shrink-0 mx-auto mt-4 max-[640px]:mt-2 text-center">
        <p className="flex items-center justify-center gap-1 text-[10px] sm:text-[12px] font-bold text-[#7b5c3c] leading-tight">
          <ShieldCheck className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-[#9b6b9e] shrink-0" />
          Pago seguro y único
        </p>

        <p className="max-[640px]:hidden text-[8px] sm:text-[11px] font-bold text-[#7b5c3c] leading-tight">
          Tus semillas se agregan al instante
        </p>
      </div>

      {(error || purchaseError) && (
        <p className="shrink-0 mt-1 text-red-600 text-[10px] text-center px-4 font-bold">
          {error ?? purchaseError}
        </p>
      )}
    </>
  )

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-3 pt-16 sm:p-4 sm:pt-16 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      {isMobile ? (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Tienda de semillas"
          onClick={e => e.stopPropagation()}
          style={{
            backgroundImage: `url(${modalBgMobile})`,
            backgroundSize: '100% 100%',
            backgroundRepeat: 'no-repeat',
            aspectRatio: MOBILE_ASPECT,
          }}
          className="relative z-10 w-full max-w-[300px] px-[9%] pt-[36%] pb-[14%] flex flex-col"
        >
          {content}
        </div>
      ) : (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Tienda de semillas"
          onClick={e => e.stopPropagation()}
          className="relative z-10 w-full max-w-[760px]"
        >
          <img
            src={modalBg}
            alt=""
            aria-hidden="true"
            className="block w-full h-auto select-none pointer-events-none"
            draggable={false}
          />
          <div className="absolute inset-0 px-[9%] pt-[18%] pb-[7%] flex flex-col">
            {content}
          </div>
        </div>
      )}
    </div>
  )
}