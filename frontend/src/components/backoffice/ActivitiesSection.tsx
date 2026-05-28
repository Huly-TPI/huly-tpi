import { ActivityCard, Activity } from './ActivityCard'
import { SectionCard, CardHeader } from './SectionCard'

// TODO: replace with real activities endpoint
const ACTIVITIES: Activity[] = [
  { emoji: '🫧', name: 'Reventar burbujas',    pct: 92, barColor: 'bg-violeta',    iconBg: 'bg-violet-100' },
  { emoji: '🌬️', name: 'Respiraciones guiadas', pct: 85, barColor: 'bg-teal-400',   iconBg: 'bg-teal-100'   },
  { emoji: '📓', name: 'Diario emocional',      pct: 78, barColor: 'bg-violeta',    iconBg: 'bg-slate-100'  },
  { emoji: '☁️', name: 'Nubes',                 pct: 64, barColor: 'bg-blue-400',   iconBg: 'bg-blue-100'   },
]

export function ActivitiesSection() {
  return (
    <SectionCard>
      <CardHeader
        title="Biblioteca de Actividades"
        action={<button className="text-xs font-semibold text-violeta hover:underline">Editar todo</button>}
      />
      <div className="grid grid-cols-2 gap-3">
        {ACTIVITIES.map(act => <ActivityCard key={act.name} activity={act} />)}
      </div>
    </SectionCard>
  )
}
