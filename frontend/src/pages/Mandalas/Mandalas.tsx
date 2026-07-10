import { useState } from "react";
import { useNavigate } from "react-router-dom";
import BackButton from "../../components/Buttons/BackButton/BackButton";
import {
  MandalaColoringActivity,
  MandalaGallery,
} from "../../components/Mandalas";
import type { MandalaCatalogItem, MandalaAccessType } from "../../components/Mandalas/mandalaTypes";
import { useAvailableMandalas } from "../../hooks/useAvailableMandalas";
import { useAuth } from "../../context/auth";
import { useSubscriptionModal } from "../../context/subscriptionModal";
import AuthGateModal from "../../components/AuthGateModal/AuthGateModal";
import ThemeBackground from "../../components/ThemeBackground/ThemeBackground";
import mandalaEleccionBackgroundDark from "../../assets/mandalas/dark-theme/background/mandala-eleccion-background-dark.webp";
import mandalaEleccionBackgroundLight from "../../assets/mandalas/light-theme/background/mandala-eleccion-background-light.webp";
import StoreModal from "../../components/Shop/StoreModal";

export default function Mandalas() {
  const [selectedMandala, setSelectedMandala] =
    useState<MandalaCatalogItem | null>(null);
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { openSubscriptionModal } = useSubscriptionModal();
  const [isStoreOpen, setIsStoreOpen] = useState(false);

  const handleLockedMandala = (accessType: MandalaAccessType) => {
    if (accessType === 'purchasable') {
      setIsStoreOpen(true);
    } else {
      openSubscriptionModal()
    }
  }
  const [modalOpen, setModalOpen] = useState(true);

  const {
    mandalas,
    loading,
    error,
    page,
    totalPages,
    first,
    last,
    setPage,
  } = useAvailableMandalas(8);

  const handleCloseModal = () => {
    setModalOpen(false);
    navigate("/minigames");
  };

  const handleLogin = () => {
    setModalOpen(false);
    navigate("/login");
  };

  const handleRegister = () => {
    setModalOpen(false);
    navigate("/register");
  };

  const pageClassName = selectedMandala
    ? "relative min-h-full w-full overflow-x-hidden"
    : "relative min-h-full w-full overflow-x-hidden mandala-selection-scene";

  return (
    <main className={pageClassName}>
      <BackButton to="/minigames" />
      {selectedMandala ? (
        <MandalaColoringActivity
          mandala={selectedMandala}
          onBackToGallery={() => setSelectedMandala(null)}
        />
      ) : (
        <>
          <ThemeBackground
            lightSrc={mandalaEleccionBackgroundLight}
            darkSrc={mandalaEleccionBackgroundDark}
            lightAlt="Fondo de día para selección de mandalas"
            darkAlt="Fondo nocturno para selección de mandalas"
          />
          {loading && (
            <div className="mandala-gallery mandala-gallery--status" role="status" aria-label="Cargando mandalas" />
          )}
          {!loading && error && (
            <>
              {isAuthenticated ? (
                <div className="mandala-gallery mandala-gallery--status" role="alert" aria-label={error} />
              ) : (
                <AuthGateModal
                  open={modalOpen}
                  onClose={handleCloseModal}
                  onLogin={handleLogin}
                  onRegister={handleRegister}
                />
              )}
            </>
          )}
          {!loading && !error && (
            <MandalaGallery
              first={first}
              mandalas={mandalas}
              last={last}
              onPageChange={setPage}
              onSelectMandala={setSelectedMandala}
              onLockedMandala={handleLockedMandala}
              page={page}
              totalPages={totalPages}
            />
          )}
        </>
      )}
      <StoreModal isOpen={isStoreOpen} onClose={() => setIsStoreOpen(false)} />
    </main>
  );
}
