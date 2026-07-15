import { useStoreItems } from "../../hooks/store/useStoreItems";
import { useCosmeticActions } from "../../hooks/store/useCosmeticActions";
import { useUserCoins } from "../../hooks/shop/useUserCoins";
import { CosmeticCard } from "./CosmeticCard";
import { X } from "lucide-react";
import seedIcon from "../../assets/rewards/seed.webp";
import type { InventoryItemResponse } from "../../api/store";
import { createStoreItemPreference } from "../../api/payment";
import { useEffect, useRef, useState } from "react";
import { useMembership } from "../../hooks/shop/useMembership";
import {
  LucideIcon,
  CreditCard,
  Crown,
  Flower2,
  Home,
  Notebook,
  Sprout,
  Trees,
  LayoutGrid,
  Shirt,
} from "lucide-react";
import HulyAvatar from "../HulyAvatar/HulyAvatar";
import { getEquippedAvatarItems } from "../HulyAvatar/avatarEquip";

const CATEGORY_SECTIONS: {
  id: string;
  categories: string[];
  label: string;
  Icon: LucideIcon;
}[] = [
    { id: "HOUSE", categories: ["HOUSE"], label: "Casas", Icon: Home },
    { id: "NOTEBOOK", categories: ["NOTEBOOK"], label: "Diarios", Icon: Notebook },
    { id: "TREE", categories: ["TREE"], label: "Árboles", Icon: Trees },
    { id: "MANDALA", categories: ["MANDALA"], label: "Mandalas", Icon: Flower2 },
    { id: "CLOTHES", categories: ["SHIRT", "SHOES", "HAT"], label: "Ropa", Icon: Shirt },
  ];

const PURCHASE_FILTERS: { type: string; label: string; Icon: LucideIcon }[] = [
  { type: "COINS", label: "Con semillas", Icon: Sprout },
  { type: "PREMIUM", label: "Solo premium", Icon: Crown },
  { type: "MONEY", label: "Con dinero", Icon: CreditCard },
];

const ALL_TAB: { id: string; categories: string[]; label: string; Icon: LucideIcon } = {
  id: "ALL",
  categories: [],
  label: "Todos",
  Icon: LayoutGrid,
};

function purchaseTypeOf(item: {
  price: number | null;
  premiumOnly: boolean;
}): string {
  if (item.price != null) return "MONEY";
  if (item.premiumOnly) return "PREMIUM";
  return "COINS";
}

interface StoreModalProps {
  isOpen: boolean;
  onClose: () => void;
  inventory?: InventoryItemResponse[];
  refetchInventory?: () => Promise<void>;
  coins?: number | null;
  refetchCoins?: () => Promise<void>;
}

export default function StoreModal({
  isOpen,
  onClose,
  inventory = [],
  refetchInventory = async () => { },
  coins: coinsProp,
  refetchCoins: refetchCoinsProp,
}: StoreModalProps) {
  const { items, loading: itemsLoading, error: itemsError } = useStoreItems();
  const { coins: coinsOwn, refresh: refetchCoinsOwn } = useUserCoins();
  const coins = coinsProp !== undefined ? coinsProp : coinsOwn;
  const refetchCoins = refetchCoinsProp ?? refetchCoinsOwn;
  const {
    busyId,
    error: actionError,
    buy,
    equip,
    unequip,
  } = useCosmeticActions();
  const awaitingPaymentRef = useRef(false);
  const { membership } = useMembership();
  const userIsPremium =
    membership?.active === true && membership?.planCode === "PREMIUM";
  const [activeTypes, setActiveTypes] = useState<Set<string>>(new Set());
  const [activeTabId, setActiveTabId] = useState<string>("ALL");
  const [previewOverrides, setPreviewOverrides] = useState<Record<string, string>>({});
  const [localError, setLocalError] = useState<string | null>(null);
  const [storeAnimation, setStoreAnimation] = useState<"idle" | "wave" | "jump" | "dance" | "look-around" | "blink">("idle");
  const equipAnimationTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const isEquipAnimatingRef = useRef(false);

  useEffect(() => {
    if (!isOpen || localError) return;

    let timeoutId: ReturnType<typeof setTimeout>;
    let resetTimeoutId: ReturnType<typeof setTimeout>;

    const triggerNextAnimation = () => {
      if (isEquipAnimatingRef.current) {
        timeoutId = setTimeout(triggerNextAnimation, 1000);
        return;
      }

      // Elegimos entre blink (muy frecuente) y wave (menos frecuente)
      const anims: ("blink" | "wave")[] = ["blink", "blink", "blink", "blink", "wave"];
      const nextAnim = anims[Math.floor(Math.random() * anims.length)];
      setStoreAnimation(nextAnim);

      const animDuration = nextAnim === "blink" ? 300 : 4000;

      // Volvemos a idle despues de que termina para poder repetir la misma animacion luego
      resetTimeoutId = setTimeout(() => {
        if (!isEquipAnimatingRef.current) setStoreAnimation("idle");
      }, animDuration);

      // Programamos la proxima animacion
      const nextWait = animDuration + (Math.random() * 2000 + 500); // Espera entre 0.5s y 2.5s en idle
      timeoutId = setTimeout(triggerNextAnimation, nextWait);
    };

    timeoutId = setTimeout(triggerNextAnimation, 1000); // Primer inicio rapido

    return () => {
      clearTimeout(timeoutId);
      clearTimeout(resetTimeoutId);
    };
  }, [isOpen, localError]);

  useEffect(() => {
    if (!isOpen) {
      setPreviewOverrides({});
      setLocalError(null);
    }
  }, [isOpen]);

  useEffect(() => {
    if (actionError) {
      setLocalError(actionError);
    }
  }, [actionError]);

  useEffect(() => {
    if (itemsError) {
      setLocalError(itemsError);
    }
  }, [itemsError]);

  const baseEquipped = getEquippedAvatarItems(inventory);
  const previewItems = baseEquipped.map(item => {
    if (previewOverrides[item.category]) {
      return { ...item, assetKey: previewOverrides[item.category] };
    }
    return item;
  });

  Object.entries(previewOverrides).forEach(([category, assetKey]) => {
    if (!previewItems.some(i => i.category === category)) {
      previewItems.push({ category, assetKey });
    }
  });

  useEffect(() => {
    const onFocus = () => {
      if (awaitingPaymentRef.current) {
        awaitingPaymentRef.current = false;
        void Promise.all([refetchInventory(), refetchCoins()]);
      }
    };
    window.addEventListener("focus", onFocus);
    return () => window.removeEventListener("focus", onFocus);
  }, [refetchInventory, refetchCoins]);

  if (!isOpen) return null;

  const ownedById = new Map(inventory.map((i) => [i.storeItemId, i]));
  const toggleType = (type: string) => {
    setActiveTypes((prev) => {
      const next = new Set(prev);
      if (next.has(type)) next.delete(type);
      else next.add(type);
      return next;
    });
  };

  const visibleItems =
    activeTypes.size === 0
      ? items
      : items.filter((item) => activeTypes.has(purchaseTypeOf(item)));

  const availableSections = CATEGORY_SECTIONS.filter((s) =>
    visibleItems.some((i) => s.categories.includes(i.category)),
  );
  const tabs = [ALL_TAB, ...availableSections];
  const activeTab = tabs.find((t) => t.id === activeTabId) ?? ALL_TAB;
  const sectionItems =
    activeTab.id === "ALL"
      ? visibleItems
      : visibleItems.filter((i) => activeTab.categories.includes(i.category));

  const handleBuy = async (id: number) => {
    const ok = await buy(id);
    if (ok) {
      await Promise.all([refetchInventory(), refetchCoins()]);
    }
  };

  const handleBuyWithMoney = (id: number) => {
    awaitingPaymentRef.current = true;
    const popup = window.open("", "_blank");
    createStoreItemPreference(String(id))
      .then(({ initPoint }) => {
        if (popup) popup.location.href = initPoint;
        else window.location.href = initPoint;
      })
      .catch(() => {
        awaitingPaymentRef.current = false;
        if (popup) popup.close();
      });
  };

  const handleEquip = async (id: number) => {
    const ok = await equip(id);
    if (ok) {
      await refetchInventory();

      const item = items.find((i) => i.id === id);
      const isClothing = item && ["SHIRT", "SHOES", "HAT"].includes(item.category);

      if (isClothing) {
        const anims: ("wave" | "jump" | "dance")[] = ["wave", "jump", "dance"];
        setStoreAnimation(anims[Math.floor(Math.random() * anims.length)]);
        isEquipAnimatingRef.current = true;
        if (equipAnimationTimeoutRef.current) clearTimeout(equipAnimationTimeoutRef.current);
        equipAnimationTimeoutRef.current = setTimeout(() => {
          setStoreAnimation("idle");
          isEquipAnimatingRef.current = false;
        }, 4000);
      }
    }
  };

  const handleUnequip = async (id: number) => {
    const ok = await unequip(id);
    if (ok) {
      await refetchInventory();
    }
  };

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] p-4 backdrop-blur-sm">
      <button
        type="button"
        aria-label="Cerrar tienda"
        className="absolute inset-0 cursor-default"
        onClick={onClose}
      />
      <div className="relative flex w-full max-w-3xl">
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Tienda de decoración"
          className="relative z-10 flex max-h-[85dvh] w-full flex-col overflow-hidden rounded-2xl bg-[#fdfbf6] shadow-2xl dark:bg-[#172033]"
        >
          <div className="flex items-center justify-between gap-2 bg-[#4C7C64] px-4 py-3 text-white sm:px-5 sm:py-4 dark:bg-[#375847] dark:border-b dark:border-slate-800">
            <div className="min-w-0">
              <p className="text-[11px] font-medium uppercase tracking-[0.18em] opacity-70">
                Decorá tu jardín
              </p>
              <h2 className="font-nunito text-xl font-black leading-tight sm:text-2xl">
                Tienda
              </h2>
            </div>
            <div className="flex shrink-0 items-center gap-2">
              {coins !== null && (
                <div className="flex items-center gap-1.5 rounded-xl bg-white/20 px-3 py-1.5">
                  <img
                    src={seedIcon}
                    alt=""
                    aria-hidden="true"
                    className="w-5 h-5 object-contain shrink-0"
                  />
                  <span className="font-bold text-sm text-white">
                    {coins.toLocaleString("es-AR")} semillas
                  </span>
                </div>
              )}
              <button
                onClick={onClose}
                aria-label="Cerrar"
                className="rounded-full p-1.5 transition hover:bg-white/20"
              >
                <X className="h-5 w-5" strokeWidth={2} />
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto px-3 py-4 sm:px-5 sm:py-5">
            {itemsLoading ? (
              <p className="py-8 text-center text-sm text-[#4C7C64] dark:text-slate-300">
                Cargando tienda...
              </p>
            ) : (
              <>
                <div role="tablist" className="mb-4 flex gap-1.5">
                  {tabs.map((tab) => {
                    const active = activeTab.id === tab.id;
                    const count =
                      tab.id === "ALL"
                        ? visibleItems.length
                        : visibleItems.filter((i) => tab.categories.includes(i.category))
                          .length;
                    return (
                      <button
                        key={tab.id}
                        type="button"
                        role="tab"
                        aria-selected={active}
                        aria-label={tab.label}
                        onClick={() => setActiveTabId(tab.id)}
                        className={`flex flex-1 min-w-0 items-center justify-center gap-1.5 rounded-xl px-2 py-2 text-[13px] font-semibold transition-colors ${active
                          ? "bg-[#4C7C64] text-white dark:bg-[#4C7C64]"
                          : "bg-[#E9F1EA]/60 text-[#4C7C64] hover:bg-[#E9F1EA] dark:bg-slate-800/60 dark:text-slate-300 dark:hover:bg-slate-800"
                          }`}
                      >
                        <tab.Icon
                          className="h-4 w-4 shrink-0"
                          strokeWidth={2}
                          aria-hidden="true"
                        />
                        <span className="truncate">{tab.label}</span>
                        <span
                          className={`shrink-0 rounded-full px-1.5 text-[12px] font-bold ${active ? "bg-white/25 text-white" : "bg-white text-[#4C7C64] dark:bg-slate-700 dark:text-slate-200"}`}
                        >
                          {count}
                        </span>
                      </button>
                    );
                  })}
                </div>

                <div className="mb-5 flex flex-wrap gap-2">
                  {PURCHASE_FILTERS.map((filter) => {
                    const active = activeTypes.has(filter.type);
                    return (
                      <button
                        key={filter.type}
                        type="button"
                        aria-pressed={active}
                        onClick={() => toggleType(filter.type)}
                        className={`flex items-center gap-1.5 rounded-full border px-3.5 py-1.5 text-xs font-semibold transition-all active:scale-95 sm:text-[13px] ${active
                          ? "border-[#4C7C64] bg-[#4C7C64] text-white shadow-sm shadow-[#4C7C64]/30 dark:border-[#4C7C64] dark:bg-[#4C7C64] dark:shadow-none"
                          : "border-[#ACCCA4]/60 bg-white text-[#4C7C64] hover:border-[#ACCCA4] hover:bg-[#E9F1EA] dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
                          }`}
                      >
                        <filter.Icon
                          className="h-[15px] w-[15px]"
                          strokeWidth={2}
                          aria-hidden="true"
                        />
                        {filter.label}
                      </button>
                    );
                  })}
                </div>

                {sectionItems.length === 0 ? (
                  <p className="py-8 text-center text-sm text-[#4C7C64] dark:text-slate-400">
                    No hay items para este filtro.
                  </p>
                ) : (
                  <div className="grid grid-cols-2 gap-3 sm:gap-4 md:grid-cols-3">
                    {sectionItems.map((item) => {
                      const owned = ownedById.get(item.id);
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
                          onPreview={(previewItem) => {
                            if (previewItem.assetKey) {
                              setPreviewOverrides(prev => ({ ...prev, [previewItem.category]: previewItem.assetKey! }));
                            }
                          }}
                        />
                      );
                    })}
                  </div>
                )}
              </>
            )}
          </div>

          {localError && (
            <div className="absolute inset-0 z-[500] flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
              <div className="w-full max-w-sm rounded-2xl bg-[#fdfbf6] p-6 shadow-2xl dark:bg-[#172033] border border-[#ACCCA4]/40 text-center animate-in fade-in zoom-in-95 duration-200">
                <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-[#E9F1EA] dark:bg-slate-800 text-3xl">
                  🌱
                </div>
                <h3 className="font-nunito text-lg font-extrabold text-[#4C7C64] dark:text-slate-100">
                  ¡Ups! Algo pasó
                </h3>
                <p className="mt-2 text-sm text-gray-600 dark:text-slate-300 px-2 leading-relaxed">
                  {localError}
                </p>
                <button
                  type="button"
                  onClick={() => setLocalError(null)}
                  className="mt-5 w-full rounded-xl bg-[#4C7C64] px-4 py-2.5 text-sm font-bold text-white transition-all hover:bg-[#375847] active:scale-95 shadow-md shadow-[#4C7C64]/20"
                >
                  Entendido
                </button>
              </div>
            </div>
          )}
        </div>
        <div className="absolute top-1/2 right-0 hidden aspect-square h-[70dvh] max-h-[1400px] -translate-y-1/2 translate-x-[90%] scale-x-[-1] lg:block">
          {/**
         * 
         * PARA CAMBIAR EL TAMAÑO DEL AVATAR
         * 
         * h-[70dvh] max-h-[600px]
         * 
         * Para que sea mas grande: 
         * h-[80dvh] max-h-[700px]
         * 
         * Para que sea mas chico:
         * h-[60dvh] max-h-[500px]
         * 
         * Hay que chequear el monitor de cada uno pero con la actual configuracion deberia estar bien para todos los monitores
         */}
          <HulyAvatar equippedItems={previewItems} animation={localError ? "stop-blow" : storeAnimation} />
        </div>
      </div>
    </div>
  );
}
