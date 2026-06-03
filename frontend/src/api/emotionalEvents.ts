import { api } from './client'

export type RecommendationDecision = 'ACCEPTED' | 'IGNORED' | 'CHOSE_OTHER'

export interface EmotionalEventDecisionrequest {
    decision: RecommendationDecision,
    chosenActivityId?: number | null
}

export interface EmotionalEventResponseDto {
    id: number
    userId: number | null
    source: string
    inputText: string | null
    detectedEmotion: string
    confidence: number | null
    valence: number | null
    arousal: number | null
    dominance: number | null
    intensity: number | null
    userGoal: string | null
    generatedRecommendation: string | null
    recommendedActivityId: number | null
    chosenActivityId: number | null
    recommendationDecision: RecommendationDecision | null
    feedbackScore: number | null
    feedbackText: string | null
    createdAt: string
    updatedAt: string
}

export const emotionalEventsApi = {
    updateDecision: (id: number, data: EmotionalEventDecisionrequest) =>
        api.patch<EmotionalEventResponseDto>(`/emotional-events/${id}/decision`, data),
}