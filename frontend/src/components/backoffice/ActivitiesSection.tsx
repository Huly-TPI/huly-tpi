import { useState, useEffect } from 'react'
import { chatbotApi, ActivityResponse } from '../../api/chatbot'
import { ActivityCard, Activity } from './ActivityCard'
import { SectionCard, CardHeader } from './SectionCard'

const VISUAL: Record<string, { emoji: string; barColor: string; iconBg: string }> = {
  'Reventar burbujas':    { emoji: '🫧', barColor: 'bg-violeta',   iconBg: 'bg-violet-100' },
  'Respiraciones guiadas':{ emoji: '🌬️', barColor: 'bg-teal-400',  iconBg: 'bg-teal-100'   },
  'Diario emocional':     { emoji: '📓', barColor: 'bg-violeta',   iconBg: 'bg-slate-100'  },
  'Nubes':                { emoji: '☁️', barColor: 'bg-blue-400',  iconBg: 'bg-blue-100'   },
}

const FALLBACK = { emoji: '✨', barColor: 'bg-violeta', iconBg: 'bg-violet-100' }

function toActivity(r: ActivityResponse): Activity {
  const v = VISUAL[r.name] ?? FALLBACK
  return { name: r.name, pct: r.pct, ...v }
}

export function ActivitiesSection() {
  const [activities, setActivities] = useState<Activity[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getActivities()
      .then(data => setActivities(data.map(toActivity)))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return (
    <SectionCard>
      <CardHeader
        title="Biblioteca de Actividades"
        action={<button className="text-xs font-semibold text-violeta hover:underline">Editar todo</button>}
      />
      {loading ? (
        <div className="flex items-center justify-center py-6">
          <div className="h-5 w-5 animate-spin rounded-full border-2 border-[#8869AC] border-t-transparent" />
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3">
          {activities.map(act => <ActivityCard key={act.name} activity={act} />)}
        </div>
      )}
    </SectionCard>
  )
}
