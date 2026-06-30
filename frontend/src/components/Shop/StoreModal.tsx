import { useStoreItems } from '../../hooks/store/useStoreItems'
import { useCosmeticActions } from '../../hooks/store/useCosmeticActions'
import { useUserCoins } from '../../hooks/shop/useUserCoins'
import { CosmeticCard } from './CosmeticCard'
import { X } from 'lucide-react'
import seedIcon from '../../assets/rewards/seed.webp'
import type { InventoryItemResponse } from '../../api/store'
import { InlineError } from '../feedback/InlineError'
import { createStoreItemPreference } from '../../api/payment'
import { useEffect, useRef, useState } from 'react'
import { useMembership } from '../../hooks/shop/useMembership'
import { LucideIcon, CreditCard, Crown, Flower2, Home, Notebook, Sprout, Trees, LayoutGrid } from 'lucide-react'


const CATEGORY_SECTIONS: { category: string; label: string; Icon: LucideIcon }[] = [
  { category: 'HOUSE', label: 'Casas', Icon: Home },
  { category: 'NOTEBOOK', label: 'Diarios', Icon: Notebook },
  { category: 'TREE', label: 'Árboles', Icon: Trees },
  { category: 'MANDALA', label: 'Mandalas', Icon: Flower2 },
]

const PURCHASE_FILTERS: { type: string; label: string; Icon: LucideIcon }[] = [
  { type: 'COINS', label: 'Con semillas', Icon: Sprout },
  { type: 'PREMIUM', label: 'Solo premium', Icon: Crown },
  { type: 'MONEY', label: 'Con dinero', Icon: CreditCard },
]

const ALL_TAB: { category: string; label: string; Icon: LucideIcon } = { category: 'ALL', label: 'Todos', Icon: LayoutGrid }

function purchaseTypeOf(item: { price: number | null; premiumOnly: boolean }): string {
  if (item.price != null) return 'MONEY'
  if (item.premiumOnly) return 'PREMIUM'
  return 'COINS'
}

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
  const [activeTypes, setActiveTypes] = useState<Set<string>>(new Set())
  const [activeCategory, setActiveCategory] = useState<string>('ALL')

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
  const toggleType = (type: string) => {
    setActiveTypes(prev => {
      const next = new Set(prev)
      if (next.has(type)) next.delete(type)
      else next.add(type)
      return next
    })
  }

  const visibleItems = activeTypes.size === 0
    ? items
    : items.filter(item => activeTypes.has(purchaseTypeOf(item)))

  const availableSections = CATEGORY_SECTIONS.filter(s => visibleItems.some(i => i.category === s.category))
  const tabs = [ALL_TAB, ...availableSections]
  const activeTab = tabs.find(t => t.category === activeCategory) ?? ALL_TAB
  const sectionItems = activeTab.category === 'ALL'
    ? visibleItems
    : visibleItems.filter(i => i.category === activeTab.category)

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
    <div className="fixed inset-0 z-[400] flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
      <button type="button" aria-label="Cerrar tienda" className="absolute inset-0 cursor-default" onClick={onClose} />
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Tienda de decoración"
        className="relative z-10 flex max-h-[85dvh] w-full max-w-3xl flex-col overflow-hidden rounded-2xl bg-[#fdfbf6] shadow-2xl"
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
            <>
              <div role="tablist" className="mb-4 flex gap-1.5">
                {tabs.map(tab => {
                  const active = activeTab.category === tab.category
                  const count = tab.category === 'ALL'
                    ? visibleItems.length
                    : visibleItems.filter(i => i.category === tab.category).length
                  return (
                    <button
                      key={tab.category}
                      type="button"
                      role="tab"
                      aria-selected={active}
                      aria-label={tab.label}
                      onClick={() => setActiveCategory(tab.category)}
                      className={`flex flex-1 min-w-0 items-center justify-center gap-1.5 rounded-xl px-2 py-2 text-[13px] font-semibold transition-colors ${active
                        ? 'bg-[#4C7C64] text-white'
                        : 'bg-[#E9F1EA]/60 text-[#4C7C64] hover:bg-[#E9F1EA]'
                        }`}
                    >
                      <tab.Icon className="h-4 w-4 shrink-0" strokeWidth={2} aria-hidden="true" />
                      <span className="truncate">{tab.label}</span>
                      <span className={`shrink-0 rounded-full px-1.5 text-[11px] font-bold ${active ? 'bg-white/25 text-white' : 'bg-white text-[#4C7C64]'}`}>
                        {count}
                      </span>
                    </button>
                  )
                })}
              </div>

              <div className="mb-5 flex flex-wrap gap-2">
                {PURCHASE_FILTERS.map(filter => {
                  const active = activeTypes.has(filter.type)
                  return (
                    <button
                      key={filter.type}
                      type="button"
                      aria-pressed={active}
                      onClick={() => toggleType(filter.type)}
                      className={`flex items-center gap-1.5 rounded-full border px-3.5 py-1.5 text-xs font-semibold transition-all active:scale-95 sm:text-[13px] ${active
                        ? 'border-[#4C7C64] bg-[#4C7C64] text-white shadow-sm shadow-[#4C7C64]/30'
                        : 'border-[#ACCCA4]/60 bg-white text-[#4C7C64] hover:border-[#ACCCA4] hover:bg-[#E9F1EA]'
                        }`}
                    >
                      <filter.Icon className="h-[15px] w-[15px]" strokeWidth={2} aria-hidden="true" />
                      {filter.label}
                    </button>
                  )
                })}
              </div>

              {sectionItems.length === 0 ? (
                <p className="py-8 text-center text-sm text-[#4C7C64]">No hay items para este filtro.</p>
              ) : (
                <div className="grid grid-cols-2 gap-3 sm:gap-4 md:grid-cols-3">
                  {sectionItems.map(item => {
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
            </>
          )}
        </div>
      </div>
    </div>
  )
}