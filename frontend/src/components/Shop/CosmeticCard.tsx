import type { StoreItemResponse } from '../../api/store'
import { cosmeticAssets } from '../Scene/cosmeticAssets'
import seedIcon from '../../assets/rewards/seed.webp'

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
}

export function CosmeticCard({ item, owned, equipped, busy, disabled, onBuy, onBuyWithMoney, onEquip, onUnequip }: CosmeticCardProps) {
  const preview = cosmeticAssets[item.assetKey]?.light
  return (
    <div className="flex flex-col gap-1.5 rounded-2xl border border-[#ACCCA4]/50 bg-white p-2.5 shadow-sm transition-shadow hover:shadow-md sm:gap-2 sm:p-3">
      {preview && (
        <img src={preview} alt={item.name} className="mx-auto h-14 w-14 object-contain sm:h-20 sm:w-20" />
      )}
      <div className="flex-1">
        <h3 className="font-nunito text-sm font-bold leading-tight text-[#4C7C64]">{item.name}</h3>
        <p className="mt-0.5 line-clamp-2 text-xs leading-snug text-gray-500">{item.description}</p>
      </div>
      {item.price != null ? (
        <div className="flex items-center gap-1">
          <span className="text-xs font-semibold text-[#4C7C64]">${item.price.toLocaleString('es-AR')}</span>
        </div>
      ) : (
        <div className="flex items-center gap-1">
          <span className="text-sm text-yellow-500">🌱</span>
          <span className="text-xs font-semibold text-yellow-700">{item.priceCoins.toLocaleString('es-AR')}</span>
        </div>
      )}
      {equipped ? (
        <button
          type="button"
          onClick={() => onUnequip(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#E9F1EA] py-2 text-xs font-semibold text-[#4C7C64] transition-all hover:bg-[#d8e6da] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Quitando…' : 'Quitar'}
        </button>
      ) : owned ? (
        <button
          type="button"
          onClick={() => onEquip(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#8869AC] py-2 text-xs font-semibold text-white transition-all hover:bg-[#75588f] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Equipando…' : 'Equipar'}
        </button>
      ) : item.price != null ? (
        <button
          type="button"
          onClick={() => onBuyWithMoney(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#4C7C64] py-2 text-xs font-semibold text-white transition-all hover:bg-[#3d6450] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Comprar con MercadoPago
        </button>
      ) : (
        <button
          type="button"
          onClick={() => onBuy(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#4C7C64] py-2 text-xs font-semibold text-white transition-all hover:bg-[#3d6450] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Comprando…' : 'Comprar'}
        </button>
      )}
    </div>
  )
}