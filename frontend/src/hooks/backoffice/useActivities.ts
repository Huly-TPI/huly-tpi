import { useEffect, useState } from 'react'
import { Cloud, NotebookText, Sparkles, Wind, type LucideIcon } from 'lucide-react'
import { chatbotApi, ActivityResponse } from '../../api/chatbot'
import { Activity } from '../../components/backoffice/ActivityCard'

const VISUAL: Record<string, { Icon: LucideIcon; barColor: string; iconBg: string; iconColor: string }> = {
  'Reventar burbujas': {
    Icon: Sparkles,
    barColor: 'bg-violeta',
    iconBg: 'bg-violet-100',
    iconColor: 'text-violeta',
  },
  'Respiraciones guiadas': {
    Icon: Wind,
    barColor: 'bg-teal-400',
    iconBg: 'bg-teal-100',
    iconColor: 'text-teal-500',
  },
  'Diario emocional': {
    Icon: NotebookText,
    barColor: 'bg-violeta',
    iconBg: 'bg-slate-100',
    iconColor: 'text-slate-500',
  },
  Nubes: {
    Icon: Cloud,
    barColor: 'bg-blue-400',
    iconBg: 'bg-blue-100',
    iconColor: 'text-blue-500',
  },
}

const FALLBACK = {
  Icon: Sparkles,
  barColor: 'bg-violeta',
  iconBg: 'bg-violet-100',
  iconColor: 'text-violeta',
}

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
