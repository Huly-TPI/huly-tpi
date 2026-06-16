import type { StoreItemResponse } from '../../api/store'

interface CosmeticCardProps {
  item: StoreItemResponse
  owned: boolean
  equipped: boolean
  busy: boolean
  disabled: boolean
  onBuy: (id: number) => void
  onEquip: (id: number) => void
  onUnequip: (id: number) => void
}

export function CosmeticCard({ item, owned, equipped, busy, disabled, onBuy, onEquip, onUnequip }: CosmeticCardProps) {
  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-[#ACCCA4]/50 bg-white p-4 shadow-sm transition-shadow hover:shadow-md">
      <div className="flex-1">
        <h3 className="font-nunito text-base font-bold text-[#4C7C64]">{item.name}</h3>
        <p className="mt-1 text-sm leading-relaxed text-gray-500">{item.description}</p>
      </div>
      <div className="flex items-center gap-1">
        <span className="text-yellow-500">🪙</span>
        <span className="text-sm font-semibold text-yellow-700">{item.priceCoins.toLocaleString('es-AR')}</span>
      </div>
      {equipped ? (
      <button
          type="button"
          onClick={() => onUnequip(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#E9F1EA] py-2.5 text-sm font-semibold text-[#4C7C64] transition-all hover:bg-[#d8e6da] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Quitando…' : 'Quitar'}
        </button>
      ) : owned ? (
        <button
          type="button"
          onClick={() => onEquip(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#8869AC] py-2.5 text-sm font-semibold text-white transition-all hover:bg-[#75588f] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Equipando…' : 'Equipar'}
        </button>
      ) : (
        <button
          type="button"
          onClick={() => onBuy(item.id)}
          disabled={disabled}
          className="w-full rounded-xl bg-[#4C7C64] py-2.5 text-sm font-semibold text-white transition-all hover:bg-[#3d6450] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {busy ? 'Comprando…' : 'Comprar'}
        </button>
      )}
    </div>
  )
}