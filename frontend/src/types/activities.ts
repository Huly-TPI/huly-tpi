import { ActivityType } from '../api/activities'

export interface ActivityMetadata {
  title: string
  color: string
}

export const ACTIVITY_METADATA: Record<ActivityType, ActivityMetadata> = {
  [ActivityType.BREATHING]: { title: 'Respiraciones Guiadas', color: 'from-emerald-400 to-teal-500' },
  [ActivityType.DIARY]: { title: 'Diario Emocional', color: 'from-purple-400 to-pink-500' },
  [ActivityType.LANTERN]: { title: 'Farolitos que vuelan', color: 'from-blue-400 to-indigo-500' },
  [ActivityType.BUBBLE]: { title: 'Reventar Burbujas', color: 'from-cyan-400 to-sky-500' },
  [ActivityType.CHALLENGE]: { title: 'Retos Diarios', color: 'from-amber-400 to-orange-500' },
  [ActivityType.ZEN_GARDEN]: { title: 'Jardin Zen de Arena', color: 'from-stone-400 to-yellow-600' },
  [ActivityType.MANDALA]: { title: 'Mandalas', color: 'from-rose-400 to-fuchsia-500' },
  [ActivityType.STONES]: { title: 'Piedras del Lago', color: 'from-sky-400 to-blue-500' },
  [ActivityType.PENDING]: { title: 'Pendientes', color: 'from-slate-400 to-indigo-600' },
}
