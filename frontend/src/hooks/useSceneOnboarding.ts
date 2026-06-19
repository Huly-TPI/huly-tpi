import { useEffect, useState } from 'react'
import { useAuth } from '../context/auth'
import type { UserProfile } from '../api/auth'

export type SceneOnboardingMode = 'hidden' | 'intro' | 'steps'

interface UseSceneOnboardingOptions {
  totalSteps: number
  completedSelector: (user: UserProfile) => boolean | undefined
  completeTutorial: () => Promise<unknown>
  requiresCompletedEmotionalOnboarding?: boolean
}

export function useSceneOnboarding({
  totalSteps,
  completedSelector,
  completeTutorial,
  requiresCompletedEmotionalOnboarding = true,
}: UseSceneOnboardingOptions) {
  const { user, refreshUser } = useAuth()
  const [onboardingMode, setOnboardingMode] = useState<SceneOnboardingMode>('hidden')
  const [onboardingStepIndex, setOnboardingStepIndex] = useState(0)

  useEffect(() => {
    if (!user || (requiresCompletedEmotionalOnboarding && user.onBoardingCompleted !== true)) {
      setOnboardingMode('hidden')
      return
    }

    setOnboardingStepIndex(0)
    setOnboardingMode(completedSelector(user) === true ? 'hidden' : 'intro')
  }, [completedSelector, requiresCompletedEmotionalOnboarding, user])

  const startOnboarding = () => {
    setOnboardingStepIndex(0)
    setOnboardingMode('steps')
  }

  const advanceOnboarding = async () => {
    if (onboardingStepIndex >= totalSteps - 1) {
      await completeTutorial()
      await refreshUser()
      setOnboardingStepIndex(0)
      setOnboardingMode('hidden')
      return
    }

    setOnboardingStepIndex(currentIndex => currentIndex + 1)
  }

  return {
    onboardingMode,
    onboardingStepIndex,
    shouldRenderOnboarding: onboardingMode !== 'hidden',
    startOnboarding,
    advanceOnboarding,
  }
}
