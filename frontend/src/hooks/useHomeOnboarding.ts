import { useEffect, useState } from 'react'
import { useAuth } from '../context/auth'
import { completeTutorial } from '../api/onboarding'

export type HomeOnboardingMode = 'hidden' | 'intro' | 'steps'

export function useHomeOnboarding(totalSteps: number) {
  const { user, refreshUser } = useAuth()
  const [onboardingMode, setOnboardingMode] = useState<HomeOnboardingMode>('hidden')
  const [onboardingStepIndex, setOnboardingStepIndex] = useState(0)
  const [showNotificationsPrompt, setShowNotificationsPrompt] = useState(false)

  useEffect(() => {
    if (!user || user.onBoardingCompleted !== true) {
      setOnboardingMode('hidden')
      return
    }

    setOnboardingMode(user.onboardingTutorialCompleted === true ? 'hidden' : 'intro')
  }, [user])

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
      setShowNotificationsPrompt(true)
      return
    }

    setOnboardingStepIndex(currentIndex => currentIndex + 1)
  }

  const closeNotificationsPrompt = () => setShowNotificationsPrompt(false)

  return {
    onboardingMode,
    onboardingStepIndex,
    shouldRenderOnboarding: onboardingMode !== 'hidden',
    startOnboarding,
    advanceOnboarding,
    showNotificationsPrompt,
    closeNotificationsPrompt,
  }
}
