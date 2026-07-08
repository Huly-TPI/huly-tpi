import { 
  Wind, 
  BookOpen, 
  Send, 
  CirclePlay, 
  Activity, 
  Shell, 
  Brush 
} from 'lucide-react'
import { ActivityType } from '../../../api/activities'

export const ACTIVITY_ICONS: Record<ActivityType, React.ComponentType<any>> = {
  [ActivityType.BREATHING]: Wind,
  [ActivityType.DIARY]: BookOpen,
  [ActivityType.LANTERN]: Send,
  [ActivityType.BUBBLE]: CirclePlay,
  [ActivityType.CHALLENGE]: Activity,
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
    bg: 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-400',
    text: 'text-indigo-600 dark:text-indigo-400',
    bar: 'bg-gradient-to-r from-indigo-500/40 to-indigo-400',
    bullet: 'bg-indigo-500 dark:bg-indigo-400',
  },
  [ActivityType.LANTERN]: {
    bg: 'bg-orange-500/10 text-orange-600 dark:text-orange-400',
    text: 'text-orange-600 dark:text-orange-400',
    bar: 'bg-gradient-to-r from-orange-500/40 to-orange-400',
    bullet: 'bg-orange-500 dark:bg-orange-400',
  },
  [ActivityType.BUBBLE]: {
    bg: 'bg-pink-500/10 text-pink-600 dark:text-pink-400',
    text: 'text-pink-600 dark:text-pink-400',
    bar: 'bg-gradient-to-r from-pink-500/40 to-pink-400',
    bullet: 'bg-pink-500 dark:bg-pink-400',
  },
  [ActivityType.CHALLENGE]: {
    bg: 'bg-red-500/10 text-red-600 dark:text-red-400',
    text: 'text-red-600 dark:text-red-400',
    bar: 'bg-gradient-to-r from-red-500/40 to-red-400',
    bullet: 'bg-red-500 dark:bg-red-400',
  },
  [ActivityType.ZEN_GARDEN]: {
    bg: 'bg-yellow-500/10 text-yellow-600 dark:text-yellow-400',
    text: 'text-yellow-600 dark:text-yellow-400',
    bar: 'bg-gradient-to-r from-yellow-500/40 to-yellow-400',
    bullet: 'bg-yellow-500 dark:bg-yellow-400',
  },
  [ActivityType.MANDALA]: {
    bg: 'bg-purple-500/10 text-purple-600 dark:text-purple-400',
    text: 'text-purple-600 dark:text-purple-400',
    bar: 'bg-gradient-to-r from-purple-500/40 to-purple-400',
    bullet: 'bg-purple-500 dark:bg-purple-400',
  },
}
