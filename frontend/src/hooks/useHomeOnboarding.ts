import { completeTutorial } from '../api/onboarding'
import type { UserProfile } from '../api/auth'
import { useSceneOnboarding, type SceneOnboardingMode } from './useSceneOnboarding'

export type HomeOnboardingMode = SceneOnboardingMode

const selectHomeTutorialCompleted = (user: UserProfile) => user.onboardingTutorialCompleted

export function useHomeOnboarding(totalSteps: number) {
  return useSceneOnboarding({
    totalSteps,
    completedSelector: selectHomeTutorialCompleted,
    completeTutorial,
  })
}
