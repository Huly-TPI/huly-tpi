import { ActivityType } from '../api/activities'
import { ActivityMetadata, ACTIVITY_METADATA } from './activities'
import { EmotionType, EMOTION_SPANISH_MAP, mapEmotionToSpanish } from './emotions'

export { ActivityType, ACTIVITY_METADATA }
export type { ActivityMetadata }
export { EmotionType, EMOTION_SPANISH_MAP, mapEmotionToSpanish }

export interface VectorMemory {
  id: string
  content: string
  sourceType: string
  contentType: string
  createdAt: string | null
}

export interface EmotionalEvent {
  id: number
  source: string | null
  inputText: string | null
  detectedEmotion: string | null
  recommendationDecision: string | null
  generatedRecommendation: string | null
}

export interface AiInsights {
  personalitySummary: string
  topicsDetected: string[]
  copingStrategies: string[]
  receptivityScore: number
  receptivityLabel: string
  acceptedActivities: string[]
  ignoredActivities: string[]
}
