import { useState, useEffect } from 'react'
import { chatbotApi, ActivityResponse } from '../../api/chatbot'
import { Activity } from '../../components/backoffice/ActivityCard'

const VISUAL: Record<string, { emoji: string; barColor: string; iconBg: string }> = {
  'Reventar burbujas':     { emoji: '🫧', barColor: 'bg-violeta',   iconBg: 'bg-violet-100' },
  'Respiraciones guiadas': { emoji: '🌬️', barColor: 'bg-teal-400',  iconBg: 'bg-teal-100'   },
  'Diario emocional':      { emoji: '📓', barColor: 'bg-violeta',   iconBg: 'bg-slate-100'  },
  'Nubes':                 { emoji: '☁️', barColor: 'bg-blue-400',  iconBg: 'bg-blue-100'   },
}

const FALLBACK = { emoji: '✨', barColor: 'bg-violeta', iconBg: 'bg-violet-100' }

function toActivity(r: ActivityResponse): Activity {
  const v = VISUAL[r.name] ?? FALLBACK
  return { name: r.name, pct: r.pct, ...v }
}

export function useActivities() {
  const [activities, setActivities] = useState<Activity[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getActivities()
      .then(data => setActivities(data.map(toActivity)))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return { activities, loading }
}
