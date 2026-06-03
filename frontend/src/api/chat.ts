import { api } from './client'

export interface SuggestedActionDto {
  type: 'RESPIRACION' | 'DIARIO' | 'NUBE' | 'BURBUJA' | string
  action_id: string
  title: string
  description: string
  action_url: string
  emotional_event_id?: number | null
}

export interface GeneratedChallengeDto {
  title: string
  description: string
}

export interface ChatMetadataDto {
  risk_detected: boolean
  matched_word: string | null
}

export interface ChatResponseDto {
  huly_reply: string
  detected_emotion?: string | null
  intensity?: number | null
  suggested_action?: SuggestedActionDto | null
  generated_challenge?: GeneratedChallengeDto | null
  metadata?: ChatMetadataDto | null
}

export interface ChatRequestDto {
  message: string
  conversationId: string
}

export interface ChatHistoryMessageDto {
  id: number
  role?: 'USER' | 'ASSISTANT' | null
  content: string
  risk_detected?: boolean | null
  detected_emotion?: string | null
  created_at: string
}

export interface ChatHistoryPageResponseDto {
  content: ChatHistoryMessageDto[]
  page_number: number
  page_size: number
  total_elements: number
  total_pages: number
  first: boolean
  last: boolean
}

export const chatApi = {
  sendMessage: (data: ChatRequestDto) =>
    api.post<ChatResponseDto>('/chat', data),
  getHistory: (conversationId: string, page = 0, size = 20) =>
    api.get<ChatHistoryPageResponseDto>(`/chat/${encodeURIComponent(conversationId)}/messages?page=${page}&size=${size}`),
}
