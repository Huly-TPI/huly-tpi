import { useState, useEffect } from "react";
import { useActivitySessionTracker } from "../hooks/useActivitySessionTracker";
import { useAuthGate } from "../context/authGate";
import { useTheme } from "../context/theme";
import BackButton from "./Buttons/BackButton/BackButton";
import { ActivityType } from "../api/activities";
import HulyAvatar, { type AvatarAnimation } from "./HulyAvatar/HulyAvatar";
import { getEquippedAvatarItems } from "./HulyAvatar/avatarEquip";
import { useInventory } from "../hooks/store/useInventory";
import cloudBreathing from "../assets/breathing/cloud-breathing.png";

export interface BreathingTechnique {
  id: number;
  name: string;
  description: string;
  inhaleSeconds: number;
  holdSeconds: number;
  exhaleSeconds: number;
  roundsInterval: number;
  rounds: number;
}

interface Phase {
  name: string;
  duration: number;
}

interface BreathingGuideProps {
  techniques?: BreathingTechnique[];
}

function getPhases(technique: BreathingTechnique): Phase[] {
  const phases: Phase[] = [
    { name: "Inhalá", duration: technique.inhaleSeconds },
  ];
  if (technique.holdSeconds > 0) {
    phases.push({ name: "Sostenélo", duration: technique.holdSeconds });
  }
  phases.push({ name: "Exhalá", duration: technique.exhaleSeconds });
  return phases;
}

function getPhaseClass(phaseName: string): string {
  if (/inhalá/i.test(phaseName)) return "inhale";
  if (/sostenélo/i.test(phaseName)) return "hold";
  if (/exhalá/i.test(phaseName)) return "exhale";
  return "";
}

const DEFAULT_BREATHING_TECHNIQUES: BreathingTechnique[] = [
  {
    id: 1,
    name: "Diafragmática",
    description:
      "Inhala profundamente por la nariz, permitiendo que tu abdomen se expanda. Exhala lentamente por la boca, vaciando completamente tus pulmones.",
    inhaleSeconds: 4,
    holdSeconds: 0,
    exhaleSeconds: 4,
    roundsInterval: 1,
    rounds: 4,
  },
  {
    id: 2,
    name: "Cuadrada",
    description:
      "Inhala, mantén la respiración, exhala y mantene la exhalación durante el mismo tiempo.",
    inhaleSeconds: 4,
    holdSeconds: 4,
    exhaleSeconds: 4,
    roundsInterval: 1,
    rounds: 4,
  },
];

export function BreathingGuide({
  techniques = DEFAULT_BREATHING_TECHNIQUES,
}: BreathingGuideProps) {
  const { requireAuth } = useAuthGate();
  const { theme } = useTheme();
  const isDark = theme === "dark";
  const { inventory } = useInventory();
  const equippedItems = getEquippedAvatarItems(inventory);
  const [selected, setSelected] = useState<BreathingTechnique | null>(null);
  const [isRunning, setIsRunning] = useState(false);
  const [currentPhaseIndex, setCurrentPhaseIndex] = useState(0);
  const [timeLeft, setTimeLeft] = useState(0);
  const [currentRound, setCurrentRound] = useState(1);
  const [isPaused, setIsPaused] = useState(false);

  const { startSession, markConditionMet, saveSession, stopSession } =
    useActivitySessionTracker(ActivityType.BREATHING, {
      minDurationSeconds: 10,
    });

  useEffect(() => {
    if (!isRunning || !selected || isPaused) return;

    if (timeLeft > 0) {
      const timer = setTimeout(() => {
        setTimeLeft(timeLeft - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }

    const phases = getPhases(selected);

    if (currentPhaseIndex < phases.length - 1) {
      setCurrentPhaseIndex(currentPhaseIndex + 1);
      setTimeLeft(phases[currentPhaseIndex + 1].duration);
    } else if (currentRound < selected.rounds) {
      setCurrentRound(currentRound + 1);
      setCurrentPhaseIndex(0);
      setTimeLeft(phases[0].duration);
    } else {
      void saveSession().catch(console.error);

      setIsRunning(false);
      setCurrentPhaseIndex(0);
      setCurrentRound(1);
      setSelected(null);
      setIsPaused(false);
      stopSession();
    }
  }, [
    timeLeft,
    isRunning,
    selected,
    currentPhaseIndex,
    currentRound,
    isPaused,
    saveSession,
    stopSession,
  ]);

  const avatarAnimation: AvatarAnimation = (() => {
    if (!isRunning || !selected) return "idle";
    const phases = getPhases(selected);
    const phaseName = phases[currentPhaseIndex]?.name ?? "";
    if (/inhalá/i.test(phaseName)) return "inhale";
    if (/sostenélo/i.test(phaseName)) return "hold";
    if (/exhalá|soplá/i.test(phaseName)) return "blow";
    return "idle";
  })();

  const hulyEl = (
    <div
      key={`${currentPhaseIndex}-${isRunning}`}
      className="fixed top-32 right-3 sm:top-auto sm:bottom-8 sm:right-8 z-20 huly-wind pointer-events-none"
    >
      <div className="relative h-[28vh] sm:h-[50vh] aspect-square flex justify-center items-end">
        <img
          src={cloudBreathing}
          alt="Nube"
          className="w-full absolute bottom-0 left-0 z-10 pointer-events-none"
        />
        <div className="absolute z-[15] w-[20%] h-[20%] bottom-[52%] right-[30%] bg-black/25 blur-md rounded-[100%] rotate-[10deg] "></div>

        <div className="absolute z-20 w-[80%] bottom-[50%] right-[5%] scale-x-[-1] rotate-[15deg] drop-shadow-lg">
          <HulyAvatar
            equippedItems={equippedItems}
            animation={avatarAnimation}
            pose="sitting"
            view="guided-breathing"
          />
        </div>
      </div>
    </div>
  );

  if (selected && isRunning) {
    const phases = getPhases(selected);
    const currentPhase = phases[currentPhaseIndex];

    return (
      <div className="flex flex-col items-center justify-center w-full relative">
        {hulyEl}
        <button
          onClick={() => {
            void saveSession().catch(console.error);
            setSelected(null);
            setCurrentPhaseIndex(0);
            setCurrentRound(1);
            setIsRunning(false);
            setIsPaused(false);
            stopSession();
          }}
          className="fixed top-20 left-6 rounded-full bg-[var(--surface-tertiary)] px-4 py-2 text-sm text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110 flex items-center gap-2"
        >
          ← Volver
        </button>
        <div className="relative flex items-center justify-center">
          <div className="absolute rounded-full bg-white/30 w-64 h-64 sm:w-80 sm:h-80" />
          <div
            key={`${currentPhaseIndex}-${currentRound}`}
            data-testid="breathing-circle"
            className={`flex flex-col items-center justify-center rounded-full shadow-xl w-52 h-52 sm:w-64 sm:h-64  ${getPhaseClass(currentPhase.name)}`}
            style={
              {
                "--phase-duration": `${currentPhase.duration}s`,
                animationPlayState: isPaused ? "paused" : "running",
                backgroundColor: isDark ? "rgba(20, 31, 53, 0.98)" : "#ffffff",
                color: isDark ? "#e5eef7" : "#1f2937",
                boxShadow: isDark
                  ? "0 18px 48px rgba(2, 6, 23, 0.55), inset 0 1px 0 rgba(255,255,255,0.06), inset 0 -10px 24px rgba(0,0,0,0.18)"
                  : "0 18px 48px rgba(15, 23, 42, 0.16)",
                zIndex: 200,
              } as React.CSSProperties
            }
          >
            <p className="text-sm font-semibold tracking-widest text-[var(--text-secondary)] uppercase">
              {currentPhase.name}
            </p>
            <p
              className={`text-5xl sm:text-6xl font-light ${isDark ? "text-[var(--text-primary)]" : "text-gray-800"}`}
            >
              {timeLeft}
            </p>
          </div>
        </div>
        <button
          onClick={() => setIsPaused(!isPaused)}
          className="mt-12 rounded-full bg-[var(--surface-tertiary)] px-6 py-2 text-sm font-medium text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110"
        >
          {isPaused ? "Reanudar" : "Pausar"}
        </button>
      </div>
    );
  }

  if (selected) {
    return (
      <div className="flex flex-col items-center justify-center w-full">
        {hulyEl}
        <button
          onClick={() => setSelected(null)}
          className="fixed top-20 left-6 rounded-full bg-[var(--surface-tertiary)] px-4 py-2 text-sm text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110 flex items-center gap-2"
        >
          ← Volver
        </button>

        <div
          className="backdrop-blur-sm rounded-2xl p-6 shadow-md w-72 sm:w-80 lg:w-96"
          style={{
            backgroundColor: isDark ? "rgba(28, 40, 63, 0.94)" : "#ffffff",
            color: isDark ? "#e5eef7" : "#1f2937",
          }}
        >
          <h2
            className={`text-xl font-bold mb-1 ${isDark ? "text-[var(--text-primary)]" : "text-gray-800"}`}
          >
            {selected.name}
          </h2>
          <p
            className={`text-sm mb-4 ${isDark ? "text-[var(--text-secondary)]" : "text-gray-500"}`}
          >
            {selected.description}
          </p>
          <button
            onClick={() => {
              const phases = getPhases(selected);
              setTimeLeft(phases[0].duration);
              startSession();
              markConditionMet();
              setIsRunning(true);
            }}
            className="w-full py-3 rounded-full bg-violeta text-white font-medium hover:opacity-100 transition-opacity"
          >
            Iniciar
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center w-full">
      {hulyEl}
      <BackButton to="/" />
      <div
        className="backdrop-blur-sm rounded-2xl p-6 shadow-md w-72 sm:w-80 lg:w-96"
        style={{
          backgroundColor: isDark ? "rgba(28, 40, 63, 0.94)" : "#ffffff",
          color: isDark ? "#e5eef7" : "#1f2937",
        }}
      >
        <h2
          className={`text-xl lg:text-2xl font-bold mb-1 ${isDark ? "text-[var(--text-primary)]" : "text-gray-800"}`}
        >
          Respiración guiada
        </h2>
        <p
          className={`text-sm lg:text-base mb-4 ${isDark ? "text-[var(--text-secondary)]" : "text-gray-500"}`}
        >
          Tómate un momento, Elegí un método y deja que el círculo acompañe tu
          respiración
        </p>
        <div className="flex flex-col gap-3">
          {techniques.map((technique) => (
            <div key={technique.id} className="relative flex flex-col">
              <button
                onClick={() => requireAuth(() => setSelected(technique))}
                className={`w-full py-3 lg:py-4 rounded-full border transition-colors font-medium ${isDark
                  ? "border-[#8d78bd] bg-[rgba(95,74,138,0.18)] text-[#d8c9f5] hover:bg-[rgba(95,74,138,0.3)]"
                  : "border-violeta text-violeta hover:bg-violeta-claro"
                  }`}
              >
                {technique.name}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
