import { useStoreItems } from '../../hooks/store/useStoreItems'
import { useCosmeticActions } from '../../hooks/store/useCosmeticActions'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { CosmeticCard } from './CosmeticCard'
import { CoinsBadge } from './CoinsBadge'
import { XMarkIcon } from '@heroicons/react/24/solid'
import type { InventoryItemResponse } from '../../api/store'
interface StoreModalProps {
  isOpen: boolean
  onClose: () => void
  inventory?: InventoryItemResponse[]
  refetchInventory?: () => Promise<void>
}


export default function StoreModal({ isOpen, onClose, inventory = [], refetchInventory = async () => { } }: StoreModalProps) {
  const { items, loading: itemsLoading, error: itemsError } = useStoreItems()
  const { coins, refresh: refetchCoins } = useUserCoins()
  const { busyId, error: actionError, buy, equip, unequip } = useCosmeticActions()

  if (!isOpen) return null

  const ownedById = new Map(inventory.map(i => [i.storeItemId, i]))

  const handleBuy = async (id: number) => {
    const ok = await buy(id)
    if (ok) {
      await Promise.all([refetchInventory(), refetchCoins()])
    }
  }

  const handleEquip = async (id: number) => {
    const ok = await equip(id)
    if (ok) {
      await refetchInventory()
    }
  }

  const handleUnequip = async (id: number) => {
    const ok = await unequip(id)
    if (ok) {
      await refetchInventory()
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
      <button type="button" aria-label="Cerrar tienda" className="absolute inset-0 cursor-default" onClick={onClose} />
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Tienda de decoración"
       className="relative z-10 flex max-h-[85dvh] w-full max-w-2xl flex-col overflow-hidden rounded-2xl bg-[#fdfbf6] shadow-2xl"
      >
        <div className="flex items-center justify-between gap-2 bg-[#4C7C64] px-4 py-3 text-white sm:px-5 sm:py-4">
          <div className="min-w-0">
            <p className="text-[10px] font-medium uppercase tracking-[0.18em] opacity-70">Decorá tu jardín</p>
            <h2 className="font-nunito text-xl font-black leading-tight sm:text-2xl">Tienda</h2>
          </div>
          <div className="flex shrink-0 items-center gap-2">
            {coins !== null && <CoinsBadge coins={coins} />}
            <button onClick={onClose} aria-label="Cerrar" className="rounded-full p-1.5 transition hover:bg-white/20">
              <XMarkIcon className="h-5 w-5" />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-3 py-4 sm:px-5 sm:py-5">
          {(actionError || itemsError) && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-center text-sm text-red-700">
              {actionError ?? itemsError}
            </div>
          )}
          {itemsLoading ? (
            <p className="py-8 text-center text-sm text-[#4C7C64]">Cargando tienda...</p>
          ) : (
            <div className="grid grid-cols-2 gap-3 sm:gap-4 md:grid-cols-3">
              {items.map(item => {
                const owned = ownedById.get(item.id)
                return (
                  <CosmeticCard
                    key={item.id}
                    item={item}
                    owned={owned !== undefined}
                    equipped={owned?.equipped ?? false}
                    busy={busyId === item.id}
                    disabled={busyId !== null}
                    onBuy={handleBuy}
                    onEquip={handleEquip}
                    onUnequip={handleUnequip}
                  />
                )
              })}
            </div>
          )}
        </div>
      </div>
    </div >
  )
}
