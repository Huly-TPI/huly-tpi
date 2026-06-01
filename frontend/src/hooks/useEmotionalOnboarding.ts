import { useState } from 'react'
import { generateOnboardingOptions, completeOnboarding } from '../api/onboarding'

const STEP1_OPTIONS = [
    'Calmar mi mente',
    'Manejar el estrés',
    'Explorar mis emociones',
    'Desconectarme un rato',
    'Expresar lo que siento',
    'Encontrar mi espacio de pausa'
]
const DEFAULT_OPTIONS = [
    'Explorar a tu ritmo',
    'Empezar de a poco',
    'Probar algo nuevo',
    'Seguir como estás',
]

export function useEmotionalOnboarding(onComplete: () => void) {
    const [step, setStep] = useState<1 | 2 | 3>(1)
    const [options, setOptions] = useState<string[]>(STEP1_OPTIONS)
    const [isLoading, setIsLoading] = useState(false)
    const [answers, setAnswers] = useState<{ a1?: string, a2?: string }>({})

    
   const selectOption = async (option: string) => {
        if (step === 1) {
            setAnswers({ a1: option })
            setIsLoading(true)
            try {
                const res = await generateOnboardingOptions(2, option)
                setOptions(res.options)
                setStep(2)
            } catch {
                setOptions(DEFAULT_OPTIONS)
                setStep(2)
            } finally {
                setIsLoading(false)
            }
        } else if (step === 2) {
            setAnswers(prev => ({ ...prev, a2: option }))
            setIsLoading(true)
            try {
                const res = await generateOnboardingOptions(3, option)
                setOptions(res.options)
                setStep(3)
            } catch {
                setOptions(DEFAULT_OPTIONS)
                setStep(3)
            } finally {
                setIsLoading(false)
            }
        } else {
                await completeOnboarding(answers.a1!, answers.a2!, option)    
                onComplete()
        }
    }

    return { step, options, isLoading, selectOption } 
}