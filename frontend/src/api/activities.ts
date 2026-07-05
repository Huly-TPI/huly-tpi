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



export const ACTIVITY_THEMES: Record<ActivityType, { bg: string; text: string; bar: string; bullet: string }> = {
  [ActivityType.BREATHING]: {
    bg: 'bg-teal-500/10 text-teal-600 dark:text-teal-400',
    text: 'text-teal-600 dark:text-teal-400',
    bar: 'bg-gradient-to-r from-teal-500/40 to-teal-400',
    bullet: 'bg-teal-500 dark:bg-teal-400',
  },
  [ActivityType.DIARY]: {
    bg: 'bg-orange-500/10 text-orange-600 dark:text-orange-400',
    text: 'text-orange-600 dark:text-orange-400',
    bar: 'bg-gradient-to-r from-orange-500/40 to-orange-400',
    bullet: 'bg-orange-500 dark:bg-orange-400',
  },
  [ActivityType.LANTERN]: {
    bg: 'bg-yellow-500/10 text-amber-600 dark:text-yellow-400',
    text: 'text-amber-600 dark:text-yellow-400',
    bar: 'bg-gradient-to-r from-yellow-500/40 to-yellow-400',
    bullet: 'bg-amber-500 dark:bg-yellow-400',
  },
  [ActivityType.BUBBLE]: {
    bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400',
    text: 'text-emerald-600 dark:text-emerald-400',
    bar: 'bg-gradient-to-r from-emerald-500/40 to-emerald-400',
    bullet: 'bg-emerald-500 dark:bg-emerald-400',
  },
  [ActivityType.CHALLENGE]: {
    bg: 'bg-rose-500/10 text-rose-600 dark:text-rose-400',
    text: 'text-rose-600 dark:text-rose-400',
    bar: 'bg-gradient-to-r from-rose-500/40 to-rose-400',
    bullet: 'bg-rose-500 dark:bg-rose-400',
  },
  [ActivityType.ZEN_GARDEN]: {
    bg: 'bg-cyan-500/10 text-cyan-600 dark:text-cyan-400',
    text: 'text-cyan-600 dark:text-cyan-400',
    bar: 'bg-gradient-to-r from-cyan-500/40 to-cyan-400',
    bullet: 'bg-cyan-500 dark:bg-cyan-400',
  },
  [ActivityType.MANDALA]: {
    bg: 'bg-violeta/10 text-violeta dark:text-violeta-claro',
    text: 'text-violeta dark:text-violeta-claro',
    bar: 'bg-gradient-to-r from-violeta/40 to-violeta-claro',
    bullet: 'bg-violeta dark:bg-violeta-claro',
  },
}

export interface RegisterActivitySessionRequest {
  activityType: ActivityType
  contextId?: string
}

export const registerActivitySession = (
  data: RegisterActivitySessionRequest,
  options?: RequestOptions,
) => api.post<null>('/activities/sessions', data, options)
