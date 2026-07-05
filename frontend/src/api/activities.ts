import { api, type RequestOptions } from './client'
import { Wind, NotebookText, Sparkles, Bubbles, Swords, Shell, Brush } from 'lucide-react'

export enum ActivityType {
  BREATHING = 'BREATHING',
  DIARY = 'DIARY',
  LANTERN = 'LANTERN',
  BUBBLE = 'BUBBLE',
  CHALLENGE = 'CHALLENGE',
  ZEN_GARDEN = 'ZEN_GARDEN',
  MANDALA = 'MANDALA',
}

export const ACTIVITY_ICONS: Record<ActivityType, any> = {
  [ActivityType.BREATHING]: Wind,
  [ActivityType.DIARY]: NotebookText,
  [ActivityType.LANTERN]: Sparkles,
  [ActivityType.BUBBLE]: Bubbles,
  [ActivityType.CHALLENGE]: Swords,
  [ActivityType.ZEN_GARDEN]: Shell,
  [ActivityType.MANDALA]: Brush,
}

export interface RegisterActivitySessionRequest {
  activityType: ActivityType
  contextId?: string
}

export const registerActivitySession = (
  data: RegisterActivitySessionRequest,
  options?: RequestOptions,
) => api.post<null>('/activities/sessions', data, options)
