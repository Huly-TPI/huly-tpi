import { ActivityType } from '../api/activities'

export interface ActivityMetadata {
  title: string
  color: string
}

export const ACTIVITY_METADATA: Record<ActivityType, ActivityMetadata> = {
  [ActivityType.RESPIRACION]: { title: 'Respiraciones Guiadas', color: 'from-emerald-400 to-teal-500' },
  [ActivityType.DIARIO]: { title: 'Diario Emocional', color: 'from-purple-400 to-pink-500' },
  [ActivityType.NUBE]: { title: 'Farolitos que vuelan', color: 'from-blue-400 to-indigo-500' },
  [ActivityType.BURBUJA]: { title: 'Reventar Burbujas', color: 'from-cyan-400 to-sky-500' },
  [ActivityType.RETO]: { title: 'Retos Diarios', color: 'from-amber-400 to-orange-500' },
  [ActivityType.ARENA_ZEN]: { title: 'Jardin Zen de Arena', color: 'from-stone-400 to-yellow-600' },
  [ActivityType.MANDALA]: { title: 'Mandalas', color: 'from-rose-400 to-fuchsia-500' },
}
