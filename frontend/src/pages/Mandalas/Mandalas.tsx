import { useState } from "react";
import BackButton from "../../components/Buttons/BackButton/BackButton";
import {
  MandalaColoringActivity,
  MandalaGallery,
  mandalaCatalog,
} from "../../components/Mandalas";
import type { MandalaCatalogItem } from "../../components/Mandalas/mandalaTypes";
import { useInventory } from "../../hooks/store/useInventory";
import { useMembership } from "../../hooks/shop/useMembership";

export default function Mandalas() {
  const [selectedMandala, setSelectedMandala] =
    useState<MandalaCatalogItem | null>(null);

  const { inventory } = useInventory();
  const { membership } = useMembership();

  const purchasedMandalaIds = inventory
    .filter((item) => item.category === "MANDALA")
    .map((item) => item.assetKey);

  const hasActiveSubscription = membership?.active ?? false;

  const availableMandalas = mandalaCatalog
    .filter((mandala) => {
      if (mandala.unlockSource === 'free') return true;
      if (mandala.unlockSource === 'premiumPlan') return hasActiveSubscription;
      if (mandala.unlockSource === 'store') return purchasedMandalaIds.includes(mandala.id);
      return false;
    })
    .map((mandala) => {
      if (mandala.unlockSource === 'store') {
        return { ...mandala, accessStatus: 'available' as const };
      }
      if (mandala.unlockSource === 'premiumPlan') {
        return { ...mandala, accessStatus: 'included' as const };
      }
      return mandala;
    });

  return (
    <main className="relative min-h-full w-full overflow-x-hidden">
      <BackButton to="/minigames" />
      {selectedMandala ? (
        <MandalaColoringActivity
          mandala={selectedMandala}
          onBackToGallery={() => setSelectedMandala(null)}
        />
      ) : (
        <MandalaGallery
          mandalas={availableMandalas}
          onSelectMandala={setSelectedMandala}
        />
      )}
    </main>
  );
}
