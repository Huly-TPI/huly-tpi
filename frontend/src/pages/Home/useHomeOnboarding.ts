import { useState } from 'react'

const HOME_ONBOARDING_COMPLETED_KEY = 'huly:home-onboarding-completed'

type HomeOnboardingEnvMode = 'auto' | 'always' | 'never'
type HomeOnboardingAccessState = {
  hasActiveSession: boolean
  hasCompletedOnboarding: boolean
}

export type HomeOnboardingMode = 'hidden' | 'intro' | 'steps'

function readHomeOnboardingEnvMode(): HomeOnboardingEnvMode {
  const rawValue = import.meta.env.VITE_HOME_ONBOARDING_MODE?.trim().toLowerCase()

  if (rawValue === 'always' || rawValue === 'true') return 'always'
  if (rawValue === 'never' || rawValue === 'false') return 'never'
  return 'auto'
}

function readHomeOnboardingAccessState(): HomeOnboardingAccessState {
  if (typeof window === 'undefined') {
    return {
      hasActiveSession: false,
      hasCompletedOnboarding: true,
    }
  }

  return {
    hasActiveSession: false,
    hasCompletedOnboarding:
      window.localStorage.getItem(HOME_ONBOARDING_COMPLETED_KEY) === 'true',
  }
}

function resolveInitialHomeOnboardingMode(accessState: HomeOnboardingAccessState): HomeOnboardingMode {
  const envMode = readHomeOnboardingEnvMode()

  if (envMode === 'always') return 'intro'
  if (envMode === 'never') return 'hidden'
  return accessState.hasCompletedOnboarding ? 'hidden' : 'intro'
}

function persistHomeOnboardingCompletion() {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(HOME_ONBOARDING_COMPLETED_KEY, 'true')
}

export function useHomeOnboarding(totalSteps: number) {
  const [onboardingMode, setOnboardingMode] = useState<HomeOnboardingMode>(() =>
    resolveInitialHomeOnboardingMode(readHomeOnboardingAccessState()),
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
