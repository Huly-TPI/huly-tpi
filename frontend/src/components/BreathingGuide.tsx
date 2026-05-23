import { useState, useEffect} from 'react';

export interface BreathingTechnique {
    id: number
    name: string
    description: string
    inhale_seconds: number
    hold_seconds: number
    exhale_seconds: number
    rounds_interval: number
    round: number
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
        { name: 'Inhalá', duration: technique.inhale_seconds },
    ]
    if (technique.hold_seconds > 0) {
        phases.push({ name: 'Sostenélo', duration: technique.hold_seconds })
    }
    phases.push({ name: 'Exhalá', duration: technique.exhale_seconds })
    return phases
}

function getPhaseClass(phaseName: string): string {
   if(/inhalá/i.test(phaseName)) return 'inhale'
   if(/sostenélo/i.test(phaseName)) return 'hold'
   if(/exhalá/i.test(phaseName)) return 'exhale'
   return ''
}

const DEFAULT_BREATHING_TECHNIQUES: BreathingTechnique[] = [
    {
        id: 1,
        name: 'Diafragmática',
        description: 'Inhala profundamente por la nariz, permitiendo que tu abdomen se expanda. Exhala lentamente por la boca, vaciando completamente tus pulmones.',
        inhale_seconds: 4,
        hold_seconds: 4,
        exhale_seconds: 4,
        rounds_interval: 1,
        round: 4
    },
    {
        id: 2,
        name: 'Cuadrada',
        description: 'Inhala, mantén la respiración, exhala y mantene la exhalación durante el mismo tiempo.',
        inhale_seconds: 4,
        hold_seconds: 4,
        exhale_seconds: 4,
        rounds_interval: 1,
        round: 4
    }
];

export function BreathingGuide({ techniques = DEFAULT_BREATHING_TECHNIQUES }: BreathingGuideProps) {
    const [selected, setSelected] = useState<BreathingTechnique | null>(null)
    const [isRunning, setIsRunning] = useState(false)
    const [currentPhaseIndex, setCurrentPhaseIndex] = useState(0)
    const [timeLeft, setTimeLeft] = useState(0)
    const [currentRound, setCurrentRound] = useState(1)
  
    useEffect(() => {
        if(!isRunning || !selected) return

       if(timeLeft > 0) {
        const timer = setTimeout(() => {
            setTimeLeft(timeLeft - 1)
        }, 1000)
        return () => clearTimeout(timer)
         } else {
            const phases = getPhases(selected)
            if(currentPhaseIndex < phases.length - 1) {
                setCurrentPhaseIndex(currentPhaseIndex + 1)
                setTimeLeft(phases[currentPhaseIndex + 1].duration)}
                else if(currentRound < selected.round) {
                    setCurrentRound(currentRound + 1)
                    setCurrentPhaseIndex(0)
                    setTimeLeft(phases[0].duration)
            } else {
                setIsRunning(false)
                setCurrentPhaseIndex(0)
                setCurrentRound(1)
                setSelected(null)
            }} 
        
        
        
        }, [timeLeft, isRunning, selected, currentPhaseIndex])

    if (selected && isRunning) {
        const phases = getPhases(selected)
        const currentPhase = phases[currentPhaseIndex]
        return (
                <div>
                    <button onClick={() => {
                        setIsRunning(false)
                        setCurrentPhaseIndex(0)
                        setCurrentRound(1)
                        setSelected(null)
                    }}>Volver</button>
                    <div
                        data-testid="breathing-circle"
                        className={getPhaseClass(currentPhase.name)}>
                    <p>{currentPhase.name}</p>
                    <p>{timeLeft}</p>
                </div>
                   </div>
            )}

    if (selected) {
        return (
            <div>
                <button onClick={() => setSelected(null)}>Volver</button>
                <h2>{selected.name}</h2>
                <p>{selected.description}</p>
                <button onClick={() => {
                  const phases = getPhases(selected!)
                  setTimeLeft(phases[0].duration)
                  setIsRunning(true)
                }}>Iniciar</button>
            </div>
        )
    }

    
    return (
        <div>
            <h2>Guía de Respiración</h2>
            <p>Selecciona una técnica de respiración para comenzar tus ejercicios de relajación.</p>
            <ul>
                {techniques.map(technique => (
                    <li key={technique.id}>
                        <button onClick={() => setSelected(technique)}>{technique.name}</button>
                        <p>{technique.description}</p>
                    </li>
                ))}
            </ul>
        </div>
    )
}