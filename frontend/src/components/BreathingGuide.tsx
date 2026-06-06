import { useState, useEffect } from 'react'
import { useAuthGate } from '../context/authGate'
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
    hulyNormal?: string
    hulyInhalando?: string
    hulyExhalando?: string
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

export function BreathingGuide({ techniques = DEFAULT_BREATHING_TECHNIQUES, hulyNormal, hulyInhalando, hulyExhalando }: BreathingGuideProps) {
    const { requireAuth } = useAuthGate()
    const [selected, setSelected] = useState<BreathingTechnique | null>(null)
    const [isRunning, setIsRunning] = useState(false)
    const [currentPhaseIndex, setCurrentPhaseIndex] = useState(0)
    const [timeLeft, setTimeLeft] = useState(0)
    const [currentRound, setCurrentRound] = useState(1)
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


  const hulyImage = (() => {
            if (!isRunning || !selected) return hulyNormal
            const phases = getPhases(selected)
            const phaseName = phases[currentPhaseIndex]?.name ?? ''
            if (/inhalá/i.test(phaseName)) return hulyInhalando ?? hulyNormal
            if (/exhalá/i.test(phaseName)) return hulyExhalando ?? hulyNormal
            return hulyNormal
        })()

        const hulyEl = hulyNormal ? (
            <div key={`${currentPhaseIndex}-${isRunning}`} className="fixed bottom-8 right-8 w-48 z-20 huly-wind">
                <img src={hulyImage} className="w-full" alt="Huly" />
            </div>
        ) : null

    if (selected && isRunning) {
        const phases = getPhases(selected)
        const currentPhase = phases[currentPhaseIndex]
    
        return (
            <div className="flex flex-col items-center justify-center w-full relative">
                {hulyEl}  
                <button
                    onClick={() => {
                        setSelected(null)
                        setCurrentPhaseIndex(0)
                        setCurrentRound(1)
                        setIsRunning(false)
                        setIsPaused(false)
                    }}
                    className="fixed top-20 left-6 bg-white/80 backdrop-blur-sm rounded-full px-4 py-2 text-violeta text-sm flex items-center gap-2 shadow-sm hover:bg-white transition-colors"
                >
                    ← Volver
                </button>
                <div className="relative flex items-center justify-center">
                    <div className="absolute rounded-full bg-white/30 w-80 h-80" />
                    <div
                        key={`${currentPhaseIndex}-${currentRound}`}
                        data-testid="breathing-circle"
                        className={`flex flex-col items-center justify-center rounded-full bg-white shadow-xl w-64 h-64 ${getPhaseClass(currentPhase.name)}`}
                        style={{
                            '--phase-duration': `${currentPhase.duration}s`,
                            animationPlayState: isPaused ? 'paused' : 'running',
                        } as React.CSSProperties}
                    >
                        <p className="text-sm font-semibold tracking-widest text-gray-500 uppercase">
                            {currentPhase.name}
                        </p>
                        <p className="text-6xl font-light text-gray-800">{timeLeft}</p>
                    </div>
                </div>
                <button
                    onClick={() => setIsPaused(!isPaused)}
                    className="mt-12 px-6 py-2 rounded-full bg-white/90 backdrop-blur-sm text-violeta text-sm font-medium shadow-sm hover:bg-white transition-colors"
                >
                    {isPaused ? 'Reanudar' : 'Pausar'}
                </button>

            
            </div>
        )
    }

    if (selected) {
        return (
            <div className="flex flex-col items-center justify-center w-full">
                  {hulyEl}
                <button
                    onClick={() => setSelected(null)}
                    className="fixed top-20 left-6 bg-white/80 backdrop-blur-sm rounded-full px-4 py-2 text-violeta text-sm flex items-center gap-2 shadow-sm hover:bg-white transition-colors"
                >
                    ← Volver
                </button>

                <div className="bg-white backdrop-blur-sm rounded-2xl p-6 shadow-md w-80">
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
            {hulyEl} 
            <BackButton to="/" />
            <div className="bg-white backdrop-blur-sm rounded-2xl p-6 shadow-md w-80">
                <h2 className="text-xl font-bold text-gray-800 mb-1">Respiración guiada</h2>
                <p className="text-sm text-gray-500 mb-4">
                    Tómate un momento, Elegí un método y deja que el círculo acompañe tu respiración
                </p>
                <div className="flex flex-col gap-3">
                    {techniques.map(technique => (
                        <div
                            key={technique.id}
                            className="relative flex flex-col"
                        >
                            <button
                                onClick={() => requireAuth(() => setSelected(technique))}
                                className="w-full py-3 rounded-full border border-violeta text-violeta hover:bg-violeta-claro transition-colors font-medium"
                            >
                                {technique.name}
                            </button>
                        
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}