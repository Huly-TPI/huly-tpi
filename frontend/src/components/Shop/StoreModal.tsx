import { useStoreItems } from '../../hooks/store/useStoreItems'
import { useCosmeticActions } from '../../hooks/store/useCosmeticActions'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { CosmeticCard } from './CosmeticCard'
import { X } from 'lucide-react'
import seedIcon from '../../assets/rewards/seed.webp'
import type { InventoryItemResponse } from '../../api/store'
import { InlineError } from '../feedback/InlineError'
import { createStoreItemPreference } from '../../api/payment'
import { useEffect, useRef } from 'react'
import { useMembership } from '../../hooks/shop/useMembership'

interface StoreModalProps {
  isOpen: boolean
  onClose: () => void
  inventory?: InventoryItemResponse[]
  refetchInventory?: () => Promise<void>
  coins?: number | null
  refetchCoins?: () => Promise<void>
}


export default function StoreModal({ isOpen, onClose, inventory = [], refetchInventory = async () => { }, coins: coinsProp, refetchCoins: refetchCoinsProp }: StoreModalProps) {
  const { items, loading: itemsLoading, error: itemsError } = useStoreItems()
  const { coins: coinsOwn, refresh: refetchCoinsOwn } = useUserCoins()
  const coins = coinsProp !== undefined ? coinsProp : coinsOwn
  const refetchCoins = refetchCoinsProp ?? refetchCoinsOwn
  const { busyId, error: actionError, buy, equip, unequip } = useCosmeticActions()
  const awaitingPaymentRef = useRef(false)
  const { membership } = useMembership()
  const userIsPremium = membership?.active === true && membership?.planCode === 'PREMIUM'

  useEffect(() => {
    const onFocus = () => {
      if (awaitingPaymentRef.current) {
        awaitingPaymentRef.current = false
        void Promise.all([refetchInventory(), refetchCoins()])
      }
    }
    window.addEventListener('focus', onFocus)
    return () => window.removeEventListener('focus', onFocus)
  }, [refetchInventory, refetchCoins])


  if (!isOpen) return null

  const ownedById = new Map(inventory.map(i => [i.storeItemId, i]))

  const handleBuy = async (id: number) => {
    const ok = await buy(id)
    if (ok) {
      await Promise.all([refetchInventory(), refetchCoins()])
    }
  }


  const handleBuyWithMoney = (id: number) => {
    awaitingPaymentRef.current = true
    const popup = window.open('', '_blank')
    createStoreItemPreference(String(id))
      .then(({ initPoint }) => {
        if (popup) popup.location.href = initPoint
        else window.location.href = initPoint
      })
      .catch(() => {
        awaitingPaymentRef.current = false
        if (popup) popup.close()
      })
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
            {coins !== null && (
              <div className="flex items-center gap-1.5 rounded-xl bg-white/20 px-3 py-1.5">
                <img src={seedIcon} alt="" aria-hidden="true" className="w-5 h-5 object-contain shrink-0" />
                <span className="font-bold text-sm text-white">{coins.toLocaleString('es-AR')} semillas</span>
              </div>
            )}
            <button onClick={onClose} aria-label="Cerrar" className="rounded-full p-1.5 transition hover:bg-white/20">
              <X className="h-5 w-5" strokeWidth={2} />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-3 py-4 sm:px-5 sm:py-5">
          {(actionError || itemsError) && (
            <InlineError message={(actionError ?? itemsError) || ''} className="mb-4" />
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
                    onBuyWithMoney={handleBuyWithMoney}
                    onEquip={handleEquip}
                    onUnequip={handleUnequip}
                    userIsPremium={userIsPremium}
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
