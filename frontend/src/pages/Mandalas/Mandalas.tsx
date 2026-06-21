import { useState } from "react";
import BackButton from "../../components/Buttons/BackButton/BackButton";
import {
  MandalaColoringActivity,
  MandalaGallery,
  mandalaCatalog,
} from "../../components/Mandalas";
import type { MandalaCatalogItem } from "../../components/Mandalas/mandalaTypes";

export default function Mandalas() {
  const [selectedMandala, setSelectedMandala] =
    useState<MandalaCatalogItem | null>(null);

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
          mandalas={mandalaCatalog}
          onSelectMandala={setSelectedMandala}
        />
      )}
    </main>
  );
}
