import { useState } from 'react'
import { completeTutorial } from '../api/onboarding'
import type { UserProfile } from '../api/auth'
import { useSceneOnboarding, type SceneOnboardingMode } from './useSceneOnboarding'

export type HomeOnboardingMode = SceneOnboardingMode

const selectHomeTutorialCompleted = (user: UserProfile) => user.onboardingTutorialCompleted

export function useHomeOnboarding(totalSteps: number) {
  const scene = useSceneOnboarding({
    totalSteps,
    completedSelector: selectHomeTutorialCompleted,
    completeTutorial,
  })

  const [showNotificationsPrompt, setShowNotificationsPrompt] = useState(false)

  const advanceOnboarding = async () => {
    const willComplete = scene.onboardingStepIndex >= totalSteps - 1

    await scene.advanceOnboarding()

    if (willComplete) {
      setShowNotificationsPrompt(true)
    }
  }

  const closeNotificationsPrompt = () => setShowNotificationsPrompt(false)

  return { ...scene, advanceOnboarding, showNotificationsPrompt, closeNotificationsPrompt }
}
