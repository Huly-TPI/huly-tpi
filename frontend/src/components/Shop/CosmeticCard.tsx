import type { StoreItemResponse } from '../../api/store'
import { cosmeticAssets } from '../Scene/cosmeticAssets'
import { mandalaAssetByKey } from '../Mandalas/mandalaAssets'
import seedIcon from '../../assets/shop-seed/1seed.webp'
import { getBackendOrigin } from '../../api/client'

interface CosmeticCardProps {
  item: StoreItemResponse
  owned: boolean
  equipped: boolean
  busy: boolean
  disabled: boolean
  onBuy: (id: number) => void
  onBuyWithMoney: (id: number) => void
  onEquip: (id: number) => void
  onUnequip: (id: number) => void
  userIsPremium: boolean
  onPreview?: (item: StoreItemResponse) => void
}

export function CosmeticCard({ item, owned, equipped, busy, disabled, onBuy, onBuyWithMoney, onEquip, onUnequip, userIsPremium, onPreview }: CosmeticCardProps) {
  const resolveImageUrl = (url: string) => url.startsWith('http') ? url : `${getBackendOrigin()}${url}`

  let preview: string | undefined
  if (item.imageUrlLight) {
    preview = resolveImageUrl(item.imageUrlLight)
  } else if (item.category === 'MANDALA') {
    preview = item.assetKey ? mandalaAssetByKey[item.assetKey] : undefined
  } else {
    preview = item.assetKey ? cosmeticAssets[item.assetKey]?.light : undefined
  }
  
  return (
    <div className="flex flex-col gap-1.5 rounded-2xl border border-[#ACCCA4]/50 bg-white p-2.5 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md sm:gap-2 sm:p-3">
      {preview && (
        <div 
          onClick={() => onPreview?.(item)}
          className={`mx-auto flex h-20 w-full items-center justify-center rounded-xl bg-gradient-to-b from-[#E9F1EA]/70 to-transparent sm:h-24 relative ${onPreview ? 'cursor-pointer hover:bg-[#E9F1EA]' : ''}`}
          title={onPreview ? "Probar" : ""}
        >
          <img src={preview} alt={item.name} className="h-14 w-14 object-contain sm:h-20 sm:w-20" />
          {onPreview && (
            <div className="absolute top-1.5 right-1.5 text-xs opacity-60">👁️</div>
          )}
        </div>
      )}
      {item.premiumOnly && (
        <span className="mt-0.5 inline-block rounded-full bg-[#8869AC]/15 px-2 py-0.5 text-[10px] lg:text-[11px] font-semibold text-[#8869AC]">
          Solo premium
        </span>
      )}
      <div className="flex-1">
        <h3 className="font-nunito text-sm lg:text-[15px] font-bold leading-tight text-[#4C7C64]">{item.name}</h3>
        <p className="mt-0.5 line-clamp-2 text-xs lg:text-[13px] leading-snug text-gray-500">{item.description}</p>
      </div>
      {item.price != null ? (
        <div className="flex items-center gap-1">
          <span className="text-xs lg:text-[13px] font-semibold text-[#4C7C64]">${item.price.toLocaleString('es-AR')}</span>
        </div>
      ) : (
        <div className="flex items-center gap-1">
          <img src={seedIcon} alt="" aria-hidden="true" className="w-6 h-6 object-contain shrink-0" />
          <span className="text-xs lg:text-[13px] font-semibold text-yellow-700">{item.priceCoins.toLocaleString('es-AR')} semillas</span>
        </div>
      )}
      {equipped ? (
        <button
          type="button"
          onClick={() => onUnequip(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#E9F1EA] py-2 text-xs lg:text-[13px] font-semibold text-[#4C7C64] transition-all hover:bg-[#d8e6da] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Quitando…' : 'Quitar'}
        </button>
      ) : owned ? (
        item.category === 'MANDALA' ? (
          <button
            type="button"
            disabled
            className="w-full rounded-xl bg-[#E9F1EA] py-2 text-xs lg:text-[13px] font-semibold text-[#4C7C64] opacity-70 cursor-not-allowed"
          >
            Adquirido
          </button>
        ) : (
          <button
            type="button"
            onClick={() => onEquip(item.id)}
            disabled={disabled}
            className="w-full rounded-xl bg-[#8869AC] py-2 text-xs lg:text-[13px] font-semibold text-white transition-all hover:bg-[#75588f] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busy ? 'Equipando…' : 'Equipar'}
          </button>
        )
      ) : item.price != null ? (
        <button
          type="button"
          onClick={() => onBuyWithMoney(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#4C7C64] py-2 text-xs lg:text-[13px] font-semibold text-white transition-all hover:bg-[#3d6450] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Comprar con MercadoPago
        </button>
      ) : item.premiumOnly && !userIsPremium ? (
        <button
          type="button"
          disabled
          className="w-full rounded-xl bg-[#8869AC]/20 py-2 text-xs lg:text-[13px] font-semibold text-[#8869AC] cursor-not-allowed"
        >
          Solo premium
        </button>
      ) : (
        <button
          type="button"
          onClick={() => onBuy(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#4C7C64] py-2 text-xs lg:text-[13px] font-semibold text-white transition-all hover:bg-[#3d6450] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Comprando…' : 'Comprar'}
        </button>
      )}
    </div>
  )
}
