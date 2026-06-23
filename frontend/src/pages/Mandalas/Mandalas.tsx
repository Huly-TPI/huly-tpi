import { useState } from "react";
import BackButton from "../../components/Buttons/BackButton/BackButton";
import {
  MandalaColoringActivity,
  MandalaGallery,
} from "../../components/Mandalas";
import type { MandalaCatalogItem } from "../../components/Mandalas/mandalaTypes";
import { useAvailableMandalas } from "../../hooks/useAvailableMandalas";

export default function Mandalas() {
  const [selectedMandala, setSelectedMandala] =
    useState<MandalaCatalogItem | null>(null);

  const { mandalas, loading, error } = useAvailableMandalas();

  return (
    <main className="relative min-h-full w-full overflow-x-hidden">
      <BackButton to="/minigames" />
      {selectedMandala ? (
        <MandalaColoringActivity
          mandala={selectedMandala}
          onBackToGallery={() => setSelectedMandala(null)}
        />
      ) : (
        <>
          {loading && (
            <div className="mandala-gallery" role="status">
              Cargando mandalas...
            </div>
          )}
          {!loading && error && (
            <div className="mandala-gallery" role="alert">
              {error}
            </div>
          )}
          {!loading && !error && (
            <MandalaGallery
              mandalas={mandalas}
              onSelectMandala={setSelectedMandala}
            />
          )}
        </>
      )}
    </main>
  );
}
