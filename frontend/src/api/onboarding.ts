import { api } from './client'

export type GenerateOptionsResponse = {
    options: string[]
}

export const generateOnboardingOptions = (step: number, previousAnswer: string) =>
    api.post<GenerateOptionsResponse>('/onboarding/generate-options', { step, previousAnswer })

export const completeOnboarding = (answer1: string, answer2: string, answer3: string) =>
    api.post<void>('/onboarding/complete', { answer1, answer2, answer3 })