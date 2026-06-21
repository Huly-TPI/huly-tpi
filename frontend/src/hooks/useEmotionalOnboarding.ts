import { useRef, useState } from 'react'
import { Cloud, PauseCircle, Search, Sparkles } from 'lucide-react'
import { completeOnboarding } from '../api/onboarding'

export type Step1Option = {
    Icon: React.ComponentType<{ className?: string }>
    title: string
    subtitle: string
}

type Step = 0 | 1 | 2 | 3

const STEP1_OPTIONS: Step1Option[] = [
    { Icon: PauseCircle, title: 'Un momento para mí', subtitle: 'Para pausar y respirar' },
    { Icon: Cloud, title: 'Soltar lo que cargo', subtitle: 'Tengo cosas en la cabeza' },
    { Icon: Search, title: 'Entender lo que siento', subtitle: 'Quiero explorar mis emociones' },
    { Icon: Sparkles, title: 'Descansar un rato', subtitle: 'Busco desconectarme un poco' },
]

const STEP2_MAP: Record<string, string[]> = {
    'Un momento para mí': [
        'Bajar el ritmo',
        'Despejar la cabeza',
        'Encontrar calma',
        'Tomarme un respiro',
    ],
    'Soltar lo que cargo': [
        'Poner en palabras lo que siento',
        'Ordenar mis pensamientos',
        'Liberar un poco la mente',
        'Sentirme más tranquilo/a',
    ],
    'Entender lo que siento': [
        'Conocerme un poco más',
        'Conectar conmigo',
        'Ordenar mis ideas',
        'Expresar lo que siento',
    ],
    'Descansar un rato': [
        'Desconectarme un momento',
        'Jugar algo tranquilo',
        'Recargar energía',
        'Despejar la cabeza',
    ],
}

const STEP3_OPTIONS = [
    'Mi espacio de pausa diaria',
    'Un compañero en el día a día',
    'Un lugar para conocerme mejor',
    'Todavía lo estoy descubriendo',
]

export function useEmotionalOnboarding(onComplete: () => Promise<void> | void) {
    const [step, setStep] = useState<Step>(0)
    const [pillOptions, setPillOptions] = useState<string[]>([])
    const [answers, setAnswers] = useState<{ a1?: string, a2?: string }>({})
    const [submitting, setSubmitting] = useState(false)
    const submittingRef = useRef(false)
    const advance = () => setStep(1)

    const selectOption = async (option: string) => {
        if (submittingRef.current) return

        if (step === 1) {
            setAnswers({ a1: option })
            setPillOptions(STEP2_MAP[option] ?? STEP3_OPTIONS)
            setStep(2)
        } else if (step === 2) {
            setAnswers(prev => ({ ...prev, a2: option }))
            setPillOptions(STEP3_OPTIONS)
            setStep(3)
        } else if (step === 3) {
            submittingRef.current = true
            setSubmitting(true)
            try {
                await completeOnboarding(answers.a1!, answers.a2!, option)
                await onComplete()
            } finally {
                submittingRef.current = false
                setSubmitting(false)
            }
        }
    }

    const skip = async () => {
        if (submittingRef.current) return
        submittingRef.current = true
        setSubmitting(true)
        try {
            await completeOnboarding('Prefiero no decirlo', 'Prefiero no decirlo', 'Todavía lo estoy descubriendo')
            await onComplete()
        } finally {
            submittingRef.current = false
            setSubmitting(false)
        }
    }

    return { step, step1Options: STEP1_OPTIONS, pillOptions, advance, selectOption, skip, submitting }
}
