import { useState } from 'react'

const HOME_ONBOARDING_COMPLETED_KEY = 'huly:home-onboarding-completed'

type HomeOnboardingEnvMode = 'auto' | 'always' | 'never'

export type HomeOnboardingMode = 'hidden' | 'intro' | 'steps'

function readHomeOnboardingEnvMode(): HomeOnboardingEnvMode {
  const rawValue = import.meta.env.VITE_HOME_ONBOARDING_MODE?.trim().toLowerCase()

  if (rawValue === 'always' || rawValue === 'true') return 'always'
  if (rawValue === 'never' || rawValue === 'false') return 'never'
  return 'auto'
}

function readHomeOnboardingCompletionState() {
  if (typeof window === 'undefined') {
    return true
  }

  return window.localStorage.getItem(HOME_ONBOARDING_COMPLETED_KEY) === 'true'
}

function resolveInitialHomeOnboardingMode(hasCompletedOnboarding: boolean): HomeOnboardingMode {
  const envMode = readHomeOnboardingEnvMode()

  if (envMode === 'always') return 'intro'
  if (envMode === 'never') return 'hidden'
  return hasCompletedOnboarding ? 'hidden' : 'intro'
}

function persistHomeOnboardingCompletion() {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(HOME_ONBOARDING_COMPLETED_KEY, 'true')
}

export function useHomeOnboarding(totalSteps: number) {
  const [onboardingMode, setOnboardingMode] = useState<HomeOnboardingMode>(() =>
    resolveInitialHomeOnboardingMode(readHomeOnboardingCompletionState()),
  )
  const [onboardingStepIndex, setOnboardingStepIndex] = useState(0)

  const startOnboarding = () => {
    setOnboardingStepIndex(0)
    setOnboardingMode('steps')
  }

  const advanceOnboarding = () => {
    if (onboardingStepIndex >= totalSteps - 1) {
      persistHomeOnboardingCompletion()
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
