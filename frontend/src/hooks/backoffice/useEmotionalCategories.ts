import { useEffect, useState } from 'react'
import {
  CircleHelp,
  CloudRain,
  Flame,
  Frown,
  Ghost,
  Moon,
  Smile,
  Wind,
  Zap,
  type LucideIcon,
} from 'lucide-react'
import { chatbotApi, EmotionalCategoryResponse } from '../../api/chatbot'
import { EmotionCategory } from '../../components/backoffice/EmotionCard'

const VISUAL: Record<string, { Icon: LucideIcon; color: string }> = {
  'Alegría': { Icon: Smile, color: '#FBBF24' },
  Tristeza: { Icon: CloudRain, color: '#3B82F6' },
  Enojo: { Icon: Flame, color: '#EF4444' },
  Miedo: { Icon: Ghost, color: '#FF69B4' },
  Sorpresa: { Icon: CircleHelp, color: '#F97316' },
  Asco: { Icon: Frown, color: '#22C55E' },
  Ansiedad: { Icon: Zap, color: '#F59E0B' },
  'Estrés': { Icon: Wind, color: '#EF4444' },
  Agobio: { Icon: Wind, color: '#06B6D4' },
  Abrumación: { Icon: Wind, color: '#06B6D4' },
  'Pánico': { Icon: CircleHelp, color: '#DC2626' },
  Desesperanza: { Icon: Moon, color: '#475569' },
  Vacío: { Icon: CircleHelp, color: '#94A3B8' },
  Soledad: { Icon: Moon, color: '#BBACA1' },
  Duelo: { Icon: CloudRain, color: '#64748B' },
  Vergüenza: { Icon: Frown, color: '#F43F5E' },
  Culpa: { Icon: Frown, color: '#78716C' },
  Frustración: { Icon: Flame, color: '#F97316' },
  Irritabilidad: { Icon: Flame, color: '#EF4444' },
  Calma: { Icon: Wind, color: '#10B981' },
  Amor: { Icon: Smile, color: '#EC4899' },
  Gratitud: { Icon: Smile, color: '#F472B6' },
  Motivación: { Icon: Zap, color: '#8B5CF6' },
  Agotamiento: { Icon: Moon, color: '#6B7280' },
  Entumecimiento: { Icon: CloudRain, color: '#93C5FD' },
  Adormecimiento: { Icon: CloudRain, color: '#93C5FD' },
  Confusión: { Icon: CircleHelp, color: '#A78BFA' },
  Neutral: { Icon: Smile, color: '#A0AEC0' },
}

function toEmotionCategory(r: EmotionalCategoryResponse): EmotionCategory {
  const v = VISUAL[r.name] ?? { Icon: CircleHelp, color: '#A0AEC0' }
  return {
    Icon: v.Icon,
    name: r.name,
    detections: r.detections,
    detect: r.detect,
    severity: r.severity,
    iconBg: `${v.color}33`,
    barColor: v.color,
    badgeColor: v.color,
  }
}

export function useEmotionalCategories() {
  const [categories, setCategories] = useState<EmotionCategory[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getEmotionalCategories()
      .then(data => setCategories(data.map(toEmotionCategory)))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return { categories, loading }
}
