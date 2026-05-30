import type { SceneElementDefinition } from '../../Scene/types'
import type { SceneTheme } from '../../Scene/SceneElement/SceneElement'

export interface HomeOnboardingStep {
  id: string
  title: string
  description: string
  icon: string
  elementIds: string[]
  cardClassName: string
}

export interface HomeOnboardingProps {
  mode: 'intro' | 'steps'
  theme: SceneTheme
  sceneElements: SceneElementDefinition[]
  steps: HomeOnboardingStep[]
  currentStepIndex: number
  onStart: () => void
  onAdvance: () => void
}
