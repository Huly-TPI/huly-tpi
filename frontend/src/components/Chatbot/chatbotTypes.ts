import { type GeneratedChallengeDto, type SuggestedActionDto } from '../../api/chat'

export interface AssistantMessage {
  role: 'assistant'
  content: string
  detected_emotion?: string | null
  intensity?: number | null
  suggested_action?: SuggestedActionDto | null
  suggestedActionDecision?: 'accepted' | 'rejected'
  suggestedActionDecisionLoading?: boolean
  suggestedActionDecisionError?: string
  generated_challenge?: GeneratedChallengeDto | null
  challengeDecision?: 'accepted' | 'rejected'
}

export interface UserMessage {
  role: 'user'
  content: string
}

export type ChatbotMessage = AssistantMessage | UserMessage

