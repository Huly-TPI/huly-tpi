import type { SceneElementDefinition } from '../../Scene/types'
import type { SceneTheme } from '../../Scene/SceneElement/SceneElement'

export interface HomeOnboardingMascot {
  imageSrc: string
  imageAlt?: string
  anchorElementId?: string
  placementClassName: string
  imageClassName: string
}

export interface HomeOnboardingStep {
  id: string
  title: string
  description: string
  icon: string
  elementIds: string[]
  cardClassName: string
  mascot?: HomeOnboardingMascot
}

export interface HomeOnboardingProps {
  mode: 'intro' | 'steps'
  theme: SceneTheme
  sceneElements: SceneElementDefinition[]
  steps: HomeOnboardingStep[]
  currentStepIndex: number
  onStart: () => void
  onAdvance: () => void
  intro?: HomeOnboardingIntroContent
}

export interface HomeOnboardingIntroContent {
  kicker?: string
  showBrand?: boolean
  title?: string
  subtitle?: string
  startLabel?: string
}

export interface HomeOnboardingIntroProps extends HomeOnboardingIntroContent {
  onStart: () => void
}

export interface HomeOnboardingStepProps {
  currentStepIndex: number
  onAdvance: () => void
  sceneElements: SceneElementDefinition[]
  step: HomeOnboardingStep
  theme: SceneTheme
  totalSteps: number
}
