import { api } from './client'

export type RiskSeverity = 'LOW' | 'MEDIUM' | 'HIGH'

export interface RiskWordResponse {
  id: number
  word: string
  description: string
  severity: RiskSeverity
  active: boolean
}

export interface RiskWordPageResponse {
  content: RiskWordResponse[]
  page_number: number
  page_size: number
  total_elements: number
  total_pages: number
  first: boolean
  last: boolean
}

export interface RiskWordRequest {
  word: string
  description?: string
  severity: RiskSeverity
}

export interface BotConfigResponse {
  id: number
  riskDetectionEnabled: boolean
  systemPrompt: string
  preferredNameQuestionEnabled?: boolean
  communicationStyleQuestionEnabled?: boolean
}

export interface UpdateBotConfigRequest {
  riskDetectionEnabled: boolean
  systemPrompt: string
  preferredNameQuestionEnabled?: boolean
  communicationStyleQuestionEnabled?: boolean
}

export type EmotionSeverityLabel = 'ALTA' | 'MEDIA' | 'BAJA'

export interface EmotionalCategoryResponse {
  name: string
  detections: number
  detect: number
  severity: EmotionSeverityLabel
}

export interface ActivityResponse {
  name: string
  type: string
  pct: number
}

export interface WellbeingResponse {
  points: number[]
  labels: string[]
}

export const chatbotApi = {
  listRiskWords: (params?: { page?: number; size?: number; word?: string; active?: boolean; severity?: RiskSeverity }) => {
    const query = new URLSearchParams()
    if (params?.page !== undefined) query.set('page', String(params.page))
    if (params?.size !== undefined) query.set('size', String(params.size))
    if (params?.word) query.set('word', params.word)
    if (params?.active !== undefined) query.set('active', String(params.active))
    if (params?.severity) query.set('severity', params.severity)
    const qs = query.toString()
    return api.get<RiskWordPageResponse>(`/risk-words${qs ? `?${qs}` : ''}`)
  },

  createRiskWord: (data: RiskWordRequest) =>
    api.post<RiskWordResponse>('/risk-words', data),

  updateRiskWord: (id: number, data: RiskWordRequest) =>
    api.put<RiskWordResponse>(`/risk-words/${id}`, data),

  deleteRiskWord: (id: number) =>
    api.delete<void>(`/risk-words/${id}`),

  getBotConfig: () =>
    api.get<BotConfigResponse>('/admin/chat/config'),

  updateBotConfig: (data: UpdateBotConfigRequest) =>
    api.put<BotConfigResponse>('/admin/chat/config', data),

  getEmotionalCategories: () =>
    api.get<EmotionalCategoryResponse[]>('/admin/chatbot/emotional-categories'),

  getActivities: () =>
    api.get<ActivityResponse[]>('/admin/chatbot/activities'),

  getWellbeing: () =>
    api.get<WellbeingResponse>('/admin/chatbot/wellbeing'),
}
