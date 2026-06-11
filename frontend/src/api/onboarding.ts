import { api } from './client'


export const completeOnboarding = (answer1: string, answer2: string, answer3: string) =>
    api.post<void>('/onboarding/complete', { answer1, answer2, answer3 })

export const completeTutorial = () =>
    api.post<void>('/onboarding/tutorial/complete', null)
