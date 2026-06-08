import { useState, useEffect } from 'react'
import { useAuthGate } from '../context/authGate'
import { useTheme } from '../context/theme'
import BackButton from './Buttons/BackButton/BackButton'

export interface BreathingTechnique {
    id: number
    name: string
    description: string
    inhaleSeconds: number
    holdSeconds: number
    exhaleSeconds: number
    roundsInterval: number
    rounds: number
}

interface Phase {
    name: string
    duration: number
}

interface BreathingGuideProps {
    techniques?: BreathingTechnique[]
}

function getPhases(technique: BreathingTechnique): Phase[] {
    const phases: Phase[] = [
        { name: 'Inhalá', duration: technique.inhaleSeconds },
    ]
    if (technique.holdSeconds > 0) {
        phases.push({ name: 'Sostenélo', duration: technique.holdSeconds })
    }
    phases.push({ name: 'Exhalá', duration: technique.exhaleSeconds })
    return phases
}

function getPhaseClass(phaseName: string): string {
    if (/inhalá/i.test(phaseName)) return 'inhale'
    if (/sostenélo/i.test(phaseName)) return 'hold'
    if (/exhalá/i.test(phaseName)) return 'exhale'
    return ''
}

const DEFAULT_BREATHING_TECHNIQUES: BreathingTechnique[] = [
    {
        id: 1,
        name: 'Diafragmática',
        description: 'Inhala profundamente por la nariz, permitiendo que tu abdomen se expanda. Exhala lentamente por la boca, vaciando completamente tus pulmones.',
        inhaleSeconds: 4,
        holdSeconds: 0,
        exhaleSeconds: 4,
        roundsInterval: 1,
        rounds: 4,
    },
    {
        id: 2,
        name: 'Cuadrada',
        description: 'Inhala, mantén la respiración, exhala y mantene la exhalación durante el mismo tiempo.',
        inhaleSeconds: 4,
        holdSeconds: 4,
        exhaleSeconds: 4,
        roundsInterval: 1,
        rounds: 4,
    },
]

export function BreathingGuide({ techniques = DEFAULT_BREATHING_TECHNIQUES }: BreathingGuideProps) {
    const { requireAuth } = useAuthGate()
    const { theme } = useTheme()
    const isDark = theme === 'dark'
    const [selected, setSelected] = useState<BreathingTechnique | null>(null)
    const [isRunning, setIsRunning] = useState(false)
    const [currentPhaseIndex, setCurrentPhaseIndex] = useState(0)
    const [timeLeft, setTimeLeft] = useState(0)
    const [currentRound, setCurrentRound] = useState(1)
    const [hoveredId, setHoveredId] = useState<number | null>(null)
    const [isPaused, setIsPaused] = useState(false)

    useEffect(() => {
        if (!isRunning || !selected || isPaused) return

        if (timeLeft > 0) {
            const timer = setTimeout(() => {
                setTimeLeft(timeLeft - 1)
            }, 1000)
            return () => clearTimeout(timer)
        }

        const phases = getPhases(selected)

        if (currentPhaseIndex < phases.length - 1) {
            setCurrentPhaseIndex(currentPhaseIndex + 1)
            setTimeLeft(phases[currentPhaseIndex + 1].duration)
        } else if (currentRound < selected.rounds) {
            setCurrentRound(currentRound + 1)
            setCurrentPhaseIndex(0)
            setTimeLeft(phases[0].duration)
        } else {
            setIsRunning(false)
            setCurrentPhaseIndex(0)
            setCurrentRound(1)
            setSelected(null)
            setIsPaused(false)
        }
    }, [timeLeft, isRunning, selected, currentPhaseIndex, currentRound, isPaused])

    if (selected && isRunning) {
        const phases = getPhases(selected)
        const currentPhase = phases[currentPhaseIndex]
        return (
            <div className="flex flex-col items-center justify-center w-full relative">
                <button
                    onClick={() => {
                        setSelected(null)
                        setCurrentPhaseIndex(0)
                        setCurrentRound(1)
                        setIsRunning(false)
                        setIsPaused(false)
                    }}
                    className="fixed top-20 left-6 rounded-full bg-[var(--surface-tertiary)] px-4 py-2 text-sm text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110 flex items-center gap-2"
                >
                    ← Volver
                </button>
                <div className="relative flex items-center justify-center">
                    <div className="absolute rounded-full bg-white/30 w-80 h-80" />
                    <div
                        key={`${currentPhaseIndex}-${currentRound}`}
                        data-testid="breathing-circle"
                        className={`flex flex-col items-center justify-center rounded-full shadow-xl w-52 h-52 sm:w-64 sm:h-64 ${getPhaseClass(currentPhase.name)}`}
                        style={{
                            '--phase-duration': `${currentPhase.duration}s`,
                            animationPlayState: isPaused ? 'paused' : 'running',
                            backgroundColor: isDark ? 'rgba(20, 31, 53, 0.98)' : '#ffffff',
                            color: isDark ? '#e5eef7' : '#1f2937',
                            boxShadow: isDark
                                ? '0 18px 48px rgba(2, 6, 23, 0.55), inset 0 1px 0 rgba(255,255,255,0.06), inset 0 -10px 24px rgba(0,0,0,0.18)'
                                : '0 18px 48px rgba(15, 23, 42, 0.16)',
                        } as React.CSSProperties}
                    >
                        <p className="text-sm font-semibold tracking-widest text-[var(--text-secondary)] uppercase">
                            {currentPhase.name}
                        </p>
                        <p className="text-5xl sm:text-6xl font-light text-gray-800">{timeLeft}</p>
                    </div>
                </div>
                <button
                    onClick={() => setIsPaused(!isPaused)}
                    className="mt-12 rounded-full bg-[var(--surface-tertiary)] px-6 py-2 text-sm font-medium text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110"
                >
                    {isPaused ? 'Reanudar' : 'Pausar'}
                </button>
            </div>
        )
    }

    if (selected) {
        return (
            <div className="flex flex-col items-center justify-center w-full">
                <button
                    onClick={() => setSelected(null)}
                    className="fixed top-20 left-6 rounded-full bg-[var(--surface-tertiary)] px-4 py-2 text-sm text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110 flex items-center gap-2"
                >
                    ← Volver
                </button>

                <div className="bg-white backdrop-blur-sm rounded-2xl p-6 shadow-md w-72 sm:w-80 lg:w-96">
                    <h2 className="text-xl font-bold text-gray-800 mb-1">{selected.name}</h2>
                    <p className="text-sm text-gray-500 mb-4">{selected.description}</p>
                    <button
                        onClick={() => {
                            const phases = getPhases(selected)
                            setTimeLeft(phases[0].duration)
                            setIsRunning(true)
                        }}
                        className="w-full py-3 rounded-full bg-violeta text-white font-medium hover:opacity-100 transition-opacity"
                    >
                        Iniciar
                    </button>
                </div>
            </div>
        )
    }

    return (
        <div className="flex flex-col items-center justify-center w-full">
            <BackButton to="/" />
            <div className="bg-white backdrop-blur-sm rounded-2xl p-6 shadow-md w-72 sm:w-80 lg:w-96">
                <h2 className="text-xl lg:text-2xl font-bold text-gray-800 mb-1">Respiración guiada</h2>
                <p className="text-sm lg:text-base text-gray-500 mb-4">
                    Tómate un momento, Elegí un método y deja que el círculo acompañe tu respiración
                </p>
                <div className="flex flex-col gap-3">
                    {techniques.map(technique => (
                        <div
                            key={technique.id}
                            onMouseEnter={() => setHoveredId(technique.id)}
                            onMouseLeave={() => setHoveredId(null)}
                            className="relative flex flex-col"
                        >
                            <button
                                onClick={() => requireAuth(() => setSelected(technique))}
                                className="w-full py-3 lg:py-4 rounded-full border border-violeta text-violeta hover:bg-violeta-claro transition-colors font-medium"
                            >
                                {technique.name}
                            </button>
                            {hoveredId === technique.id && (
                                <div className="absolute top-full left-0 right-0 mt-1 z-10 bg-white/90 backdrop-blur-sm rounded-xl p-3 shadow-md">
                                    <p className="text-xs text-gray-500 mt-1 px-3 text-center">
                                        {technique.description}
                                    </p>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}
