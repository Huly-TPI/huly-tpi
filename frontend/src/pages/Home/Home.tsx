import seedIcon from "../../assets/garden/seedIcon.webp";
import rewardsIcon from "../../assets/garden/rewardsIcon.webp";
import dayBackgroundImage from "../../assets/garden/light-theme/background/day-background.webp";
import dayMobileBackgroundImage from "../../assets/garden/light-theme/background/mobile/day-background.webp";
import { useEffect, useState, useRef } from "react";
import cloudImage from "../../assets/garden/light-theme/cloud.webp";
import houseImage from "../../assets/garden/light-theme/house.webp";
import notebookImage from "../../assets/garden/light-theme/notebook.webp";
import todoBoardImage from "../../assets/garden/light-theme/to-do-board.webp";
import treeImage from "../../assets/garden/light-theme/tree.webp";
import wateringCanImage from "../../assets/garden/light-theme/watering-can-plant.webp";
import nightBackgroundImage from "../../assets/garden/dark-theme/background/night-background.webp";
import nightMobileBackgroundImage from "../../assets/garden/dark-theme/background/mobile/night-background.webp";
import darkHouseImage from "../../assets/garden/dark-theme/house.webp";
import darkNotebookImage from "../../assets/garden/dark-theme/notebook.webp";
import darkTodoBoardImage from "../../assets/garden/dark-theme/to-do-board.webp";
import darkTreeImage from "../../assets/garden/dark-theme/tree.webp";
import darkWateringCanImage from "../../assets/garden/dark-theme/watering-can-plant.webp";
import darkCloudImage from "../../assets/garden/dark-theme/cloud.webp";
import HomeOnboarding from "../../components/Onboarding/HomeOnboarding/HomeOnboarding";
import NotificationsPrompt from "../../components/Notifications/NotificationsPrompt/NotificationsPrompt";
import StoreModal from "../../components/Shop/StoreModal";
import storeButtonImage from "../../assets/garden/store-button.webp";
import RewardsModal from "../../components/RewardsModal/RewardsModal";
import SeedShopModal from "../../components/SeedShopModal/SeedShopModal";
import ComebackRewardModal from "../../components/Shop/ComebackRewardModal";
import SceneElement, {
  type SceneTheme,
} from "../../components/Scene/SceneElement/SceneElement";
import type { SceneElementDefinition } from "../../components/Scene/types";
import { useTheme } from "../../context/theme";
import { useAuth } from "../../context/auth";
import { useHomeOnboarding } from "../../hooks/useHomeOnboarding";
import { useInventory } from "../../hooks/store/useInventory";
import { useUserCoins } from "../../hooks/shop/useUserCoins";
import { useDailyRewards } from "../../hooks/shop/useDailyRewards";
import { resolveEquippedImages } from "../../components/Scene/CosmeticAssets";
import { createHomeOnboardingSteps } from "./homeOnboardingSteps";
import HulyAvatar from "../../components/HulyAvatar/HulyAvatar";
import { getEquippedAvatarItems } from "../../components/HulyAvatar/avatarEquip";
import "./Home.css";

const THEME_BEHAVIOR: Record<
  SceneTheme,
  { restrictedElementIds: Set<string> }
> = {
  light: {
    restrictedElementIds: new Set<string>(),
  },
  dark: {
    restrictedElementIds: new Set<string>(),
  },
};

const CATEGORY_BY_ELEMENT_ID: Record<string, string> = {
  house: "HOUSE",
  notebook: "NOTEBOOK",
  tree: "TREE",
};

const cloudClipPath =
  "polygon(1% 60%, 7% 38%, 22% 32%, 33% 12%, 57% 0%, 76% 18%, 84% 34%, 98% 41%, 100% 62%, 86% 72%, 74% 88%, 52% 96%, 30% 89%, 17% 79%, 5% 74%)";

const createCloudElement = (
  id: string,
  placementClassName: string,
  tooltipClassName = "top-full mt-2",
): SceneElementDefinition => ({
  id,
  title: "Respiraciones guiadas",
  imageAlt: "Nube del jardin para respiraciones guiadas",
  image: { light: cloudImage, dark: darkCloudImage },
  placementClassName,
  imageClassName: "w-full",
  hotspotClassName: "left-[2%] top-[5%] h-[88%] w-[96%]",
  clipPath: cloudClipPath,
  tooltipClassName,
  to: "/guided-breathing",
});

const cloudElements: SceneElementDefinition[] = [
  createCloudElement(
    "cloud-top-left",
    "left-[7%] top-[5.5%] z-10 w-[10%] md:left-[8%] md:top-[2%] md:w-[8.8%] min-[1400px]:top-[1.2%]",
  ),
  createCloudElement(
    "cloud-upper-left",
    "left-[28%] top-[3.8%] z-10 w-[22%] md:left-[22%] md:top-[8.5%] md:w-[13.2%] min-[1400px]:top-[7.2%]",
  ),
  createCloudElement(
    "cloud-center",
    "left-[60%] top-[12%] z-10 w-[14.5%] md:left-[46.8%] md:top-[6.8%] md:w-[19.8%] min-[1400px]:top-[5.2%]",
  ),
  createCloudElement(
    "cloud-bottom-left",
    "left-[15%] top-[16.5%] z-10 w-[20%] md:hidden md:left-[-3.2%] md:top-[30%] md:z-10 md:w-[7%]",
    "top-full mt-1",
  ),
  createCloudElement(
    "cloud-bottom-right",
    "left-[82%] top-[23%] z-10 w-[10%] md:hidden md:left-[88.2%] md:top-[39.5%] md:z-10 md:w-[10%]",
    "top-full mt-1",
  ),
];

const gardenElements: SceneElementDefinition[] = [
  {
    id: "tree",
    title: "Minijuegos",
    imageAlt: "Arbol con hamaca en el jardin",
    image: { light: treeImage, dark: darkTreeImage },
    placementClassName:
      "left-[5%] top-[32%] z-20 w-[47%] md:left-[2.5%] md:top-[29%] md:w-[29%] min-[1400px]:top-[18%]",
    imageClassName: "w-full",
    hotspotClassName: "left-[5%] top-[3%] h-[94%] w-[88%]",
    clipPath:
      "polygon(8% 23%, 22% 8%, 53% 2%, 84% 10%, 98% 35%, 91% 55%, 79% 59%, 73% 97%, 34% 99%, 26% 64%, 5% 54%)",
    tooltipClassName:
      "bottom-full mb-2 md:bottom-auto md:mb-0 md:top-[10%] min-[1400px]:top-[6%]",
    to: "/minigames",
  },
  {
    id: "house",
    title: "Perfil",
    imageAlt: "Casa con forma de hongo en el jardin",
    image: { light: houseImage, dark: darkHouseImage },
    placementClassName:
      "left-[44%] top-[39%] z-30 w-[50%] md:left-[38.2%] md:top-[36%] md:w-[24.8%] min-[1400px]:top-[28.2%]",
    imageClassName: "w-full",
    hotspotClassName: "left-[6%] top-[1%] h-[92%] w-[88%]",
    clipPath:
      "polygon(8% 40%, 26% 10%, 52% 1%, 79% 8%, 95% 33%, 93% 79%, 73% 94%, 24% 96%, 6% 80%)",
    tooltipClassName: "top-[18%] md:top-auto md:bottom-full md:mb-2",
    to: "/profile",
  },
  {
    id: "todo-board",
    title: "Pendientes",
    imageAlt: "Cartel de pendientes en el jardin",
    image: { light: todoBoardImage, dark: darkTodoBoardImage },
    placementClassName:
      "left-[72.5%] top-[63.5%] z-20 w-[22%] md:left-[66.2%] md:top-[50%] md:w-[10.5%] min-[1400px]:top-[44%]",
    imageClassName: "w-full",
    hotspotClassName: "left-[7%] top-[1%] h-[98%] w-[86%]",
    clipPath: "polygon(12% 5%, 84% 0%, 97% 13%, 98% 99%, 7% 99%, 1% 15%)",
    tooltipClassName: "bottom-full mb-2",
    to: "/pending",
  },
  {
    id: "watering-can-plant",
    title: "Retos",
    imageAlt: "Regadera y maceta en el jardin",
    image: { light: wateringCanImage, dark: darkWateringCanImage },
    placementClassName:
      "left-[10%] top-[75.5%] z-[85] w-[28%] md:left-[26%] md:top-[70.4%] md:w-[11.5%]",
    imageClassName: "w-full",
    hotspotClassName: "left-[2%] top-[4%] h-[92%] w-[96%]",
    clipPath:
      "polygon(3% 56%, 18% 16%, 47% 12%, 68% 2%, 96% 26%, 94% 95%, 56% 97%, 34% 82%, 8% 83%)",
    tooltipClassName: "bottom-full mb-1",
    to: "/challenges",
  },
  {
    id: "notebook",
    title: "Diario",
    imageAlt: "Banco con cuaderno en el jardin",
    image: { light: notebookImage, dark: darkNotebookImage },
    placementClassName:
      "left-[7%] top-[61%] z-[75] w-[28%] md:left-[73%] md:top-[70.8%] md:w-[14.5%]",
    imageClassName: "w-full",
    imageVariantClassName: "scene-element__image--mirror-mobile",
    hotspotClassName: "left-[2%] top-[8%] h-[84%] w-[96%]",
    clipPath: "polygon(9% 31%, 93% 9%, 99% 44%, 85% 95%, 18% 96%, 1% 56%)",
    tooltipClassName: "bottom-full mb-2",
    to: "/diary",
  },
];

const sceneElements = [...cloudElements, ...gardenElements];
const cloudElementIds = cloudElements.map((element) => element.id);
const homeOnboardingSteps = createHomeOnboardingSteps(cloudElementIds);

let rewardAutoOpenedForUserId: number | null = null;

function HomeWanderingAvatar({ equippedItems }: { equippedItems: any }) {
  const [pos, setPos] = useState({ x: 10, y: 60 });
  const [direction, setDirection] = useState<1 | -1>(1);
  const [isWalking, setIsWalking] = useState(false);
  const [duration, setDuration] = useState(0);

  const posRef = useRef(pos);

  useEffect(() => {
    let timeoutId: NodeJS.Timeout;
    let isMounted = true;

    const wander = () => {
      if (!isMounted) return;

      const currentPos = posRef.current;
      // Grass area bounds
      const nextX = Math.random() * 70 + 5;
      const nextY = Math.random() * 40 + 45;

      const dist = Math.hypot(nextX - currentPos.x, nextY - currentPos.y);
      const time = dist * 180; // 180ms per 1% distance

      setDuration(time);
      setDirection(nextX < currentPos.x ? -1 : 1);
      setIsWalking(true);

      const nextPos = { x: nextX, y: nextY };
      setPos(nextPos);
      posRef.current = nextPos;

      timeoutId = setTimeout(() => {
        if (!isMounted) return;
        setIsWalking(false);
        timeoutId = setTimeout(wander, Math.random() * 3000 + 2000);
      }, time);
    };

    timeoutId = setTimeout(wander, 1000);

    return () => {
      isMounted = false;
      clearTimeout(timeoutId);
    };
  }, []);

  return (
    <div
      className="absolute w-[30%] md:w-[15%] aspect-square pointer-events-none"
      style={{
        left: `${pos.x}%`,
        top: `${pos.y}%`,
        zIndex: Math.floor(pos.y + 25), // El z-index se calcula en base a los pies (pos.y + altura del 25%)
        transition: isWalking ? `left ${duration}ms linear, top ${duration}ms linear` : 'none',
      }}
    >
      <div
        className="w-full h-full"
        style={{
          transform: `scaleX(${direction})`,
          transition: 'transform 0.3s ease'
        }}
      >
        {/* Shadow (doesn't bob) */}
        <div className="absolute bottom-[-5%] left-1/2 w-[60%] h-[10%] -translate-x-1/2 rounded-[100%] bg-black/40 blur-[6px]" />

        {/* Avatar (bobs only when walking) */}
        <div className={isWalking ? "walking-avatar-bob relative z-10 w-full h-full" : "relative z-10 w-full h-full"}>
          <HulyAvatar equippedItems={equippedItems} animation={isWalking ? "walking" : "idle"} />
        </div>
      </div>
    </div>
  );
}

export default function Home() {
  const { theme: sceneTheme } = useTheme();
  const [isStoreOpen, setIsStoreOpen] = useState(false);
  const [isRewardsOpen, setIsRewardsOpen] = useState(false);
  const [isSeedShopOpen, setIsSeedShopOpen] = useState(false);
  const {
    inventory,
    loading: inventoryLoading,
    refetch: refetchInventory,
  } = useInventory();
  const { coins, refresh: refreshCoins } = useUserCoins();
  const { status: rewardsStatus } = useDailyRewards();
  const equippedByCategory = resolveEquippedImages(inventory);
  const equippedItems = getEquippedAvatarItems(inventory);
  const { user } = useAuth();

  useEffect(() => {
    if (
      user?.id &&
      user.onboardingTutorialCompleted &&
      rewardsStatus?.canClaimToday &&
      rewardAutoOpenedForUserId !== user.id
    ) {
      rewardAutoOpenedForUserId = user.id;
      setIsRewardsOpen(true);
    }
  }, [
    user?.id,
    user?.onboardingTutorialCompleted,
    rewardsStatus?.canClaimToday,
  ]);
  const {
    onboardingMode,
    onboardingStepIndex,
    shouldRenderOnboarding,
    startOnboarding,
    advanceOnboarding,
    showNotificationsPrompt,
    closeNotificationsPrompt,
  } = useHomeOnboarding(homeOnboardingSteps.length);

  useEffect(() => {
    if (shouldRenderOnboarding) {
      document.body.setAttribute("data-home-onboarding-active", "true");
      window.dispatchEvent(
        new CustomEvent("home-onboarding-visibility-change"),
      );
      return () => {
        document.body.removeAttribute("data-home-onboarding-active");
        window.dispatchEvent(
          new CustomEvent("home-onboarding-visibility-change"),
        );
      };
    }

    document.body.removeAttribute("data-home-onboarding-active");
    window.dispatchEvent(new CustomEvent("home-onboarding-visibility-change"));
    return undefined;
  }, [shouldRenderOnboarding]);

  useEffect(() => {
    const sources = new Set<string>([
      dayBackgroundImage,
      dayMobileBackgroundImage,
      nightBackgroundImage,
      nightMobileBackgroundImage,
    ]);
    for (const element of sceneElements) {
      sources.add(element.image.light);
      if (element.image.dark) {
        sources.add(element.image.dark);
      }
    }
    for (const step of homeOnboardingSteps) {
      if (step.mascot) sources.add(step.mascot.imageSrc);
    }

    sources.forEach((src) => {
      const image = new Image();
      image.src = src;
    });
  }, []);

  const currentThemeBehavior = THEME_BEHAVIOR[sceneTheme];
  const renderedSceneElements = sceneElements.map((element) => {
    const category = CATEGORY_BY_ELEMENT_ID[element.id];
    const equipped = category ? equippedByCategory[category] : undefined;
    const withCosmetic = equipped
      ? { ...element, image: { light: equipped.light, dark: equipped.dark } }
      : element;

    const isRestrictedForTheme = currentThemeBehavior.restrictedElementIds.has(
      element.id,
    );
    if (!isRestrictedForTheme && !shouldRenderOnboarding) return withCosmetic;

    return {
      ...withCosmetic,
      interactive: false,
    };
  });

  return (
    <main
      className={`garden-page ${sceneTheme === "dark" ? "garden-page--dark" : ""}`}
    >
      <section className="garden-scene">
        <img
          src={dayBackgroundImage}
          alt={sceneTheme === "light" ? "Fondo del jardin" : ""}
          aria-hidden={sceneTheme !== "light"}
          className={`garden-scene__background garden-scene__background--desktop garden-scene__background--light pointer-events-none select-none ${sceneTheme === "light" ? "garden-scene__background--active" : ""}`}
        />
        <img
          src={dayMobileBackgroundImage}
          alt={sceneTheme === "light" ? "Fondo del jardin para celular" : ""}
          aria-hidden={sceneTheme !== "light"}
          className={`garden-scene__background garden-scene__background--mobile garden-scene__background--light pointer-events-none select-none ${sceneTheme === "light" ? "garden-scene__background--active" : ""}`}
        />
        <img
          src={nightBackgroundImage}
          alt={sceneTheme === "dark" ? "Fondo del jardin nocturno" : ""}
          aria-hidden={sceneTheme !== "dark"}
          className={`garden-scene__background garden-scene__background--desktop garden-scene__background--dark pointer-events-none select-none ${sceneTheme === "dark" ? "garden-scene__background--active" : ""}`}
        />
        <img
          src={nightMobileBackgroundImage}
          alt={
            sceneTheme === "dark"
              ? "Fondo del jardin nocturno para celular"
              : ""
          }
          aria-hidden={sceneTheme !== "dark"}
          className={`garden-scene__background garden-scene__background--mobile garden-scene__background--dark pointer-events-none select-none ${sceneTheme === "dark" ? "garden-scene__background--active" : ""}`}
        />

        <div
          className={`absolute inset-0 transition-opacity duration-300 ${inventoryLoading ? "opacity-0" : "opacity-100"}`}
        >
          {renderedSceneElements.map((element) => (
            <SceneElement key={element.id} theme={sceneTheme} {...element} />
          ))}
        </div>

        {onboardingMode !== "hidden" ? (
          <HomeOnboarding
            mode={onboardingMode}
            theme={sceneTheme}
            sceneElements={renderedSceneElements}
            steps={homeOnboardingSteps}
            currentStepIndex={onboardingStepIndex}
            onStart={startOnboarding}
            onAdvance={advanceOnboarding}
          />
        ) : null}
        {showNotificationsPrompt && (
          <NotificationsPrompt onClose={closeNotificationsPrompt} />
        )}

        {user?.id && user?.onboardingTutorialCompleted && !isStoreOpen && (
          <HomeWanderingAvatar equippedItems={equippedItems} />
        )}
      </section>

      {user?.onboardingTutorialCompleted && (
        <button
          type="button"
          onClick={() => setIsStoreOpen(true)}
          aria-label="Abrir tienda"
          className="fixed bottom-24 right-4 z-[150] h-20 w-20 transition hover:scale-105 active:scale-95"
        >
          <img
            src={storeButtonImage}
            alt="Abrir tienda"
            className="h-full w-full object-contain"
          />
        </button>
      )}

      {user?.onboardingTutorialCompleted && (
        <div className="fixed top-16 right-4 z-[150] select-none flex gap-2 items-start">
          <button
            type="button"
            aria-label="Ver recompensas diarias"
            onClick={() => setIsRewardsOpen(true)}
            className="relative w-24 cursor-pointer hover:scale-105 transition-transform active:scale-95"
          >
            <img
              src={rewardsIcon}
              alt="Recompensas diarias"
              className="w-full h-auto scale-[0.97] origin-top"
            />
            <span className="absolute bottom-[24%] left-1/2 -translate-x-1/2 text-[13px] font-bold text-[#4E3523] whitespace-nowrap">
              Reclamar
            </span>
          </button>
          <button
            type="button"
            aria-label="Abrir tienda de semillas"
            onClick={() => setIsSeedShopOpen(true)}
            className="relative w-24 cursor-pointer hover:scale-105 transition-transform active:scale-95"
          >
            <img
              src={seedIcon}
              alt="Semillas en colección"
              className="w-full h-auto"
            />
            {coins !== null && (
              <span className="absolute bottom-[25%] left-1/2 -translate-x-1/2 text-[13px] font-bold text-[#4E3523]">
                {coins.toLocaleString("es-AR")}
              </span>
            )}
          </button>
        </div>
      )}

      <StoreModal
        isOpen={isStoreOpen}
        onClose={() => setIsStoreOpen(false)}
        inventory={inventory}
        refetchInventory={refetchInventory}
        coins={coins}
        refetchCoins={refreshCoins}
      />
      <RewardsModal
        isOpen={isRewardsOpen}
        onClose={() => setIsRewardsOpen(false)}
        onClaimed={refreshCoins}
      />
      <SeedShopModal
        isOpen={isSeedShopOpen}
        onClose={() => setIsSeedShopOpen(false)}
      />

      {user?.onboardingTutorialCompleted && <ComebackRewardModal />}
    </main>
  );
}
