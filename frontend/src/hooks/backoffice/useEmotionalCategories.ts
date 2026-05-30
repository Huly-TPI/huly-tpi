import { useState, useEffect } from 'react'
import { chatbotApi, EmotionalCategoryResponse } from '../../api/chatbot'
import { EmotionCategory } from '../../components/backoffice/EmotionCard'

const VISUAL: Record<string, { emoji: string; color: string }> = {
  // Emociones básicas
  'Alegría':        { emoji: '😊', color: '#FBBF24' },
  'Tristeza':       { emoji: '🌧️', color: '#3B82F6' },
  'Enojo':          { emoji: '🔥', color: '#EF4444' },
  'Miedo':          { emoji: '👻', color: '#FF69B4' },
  'Sorpresa':       { emoji: '😲', color: '#F97316' },
  'Asco':           { emoji: '🤢', color: '#22C55E' },

  // Estados ansiosos y de estrés
  'Ansiedad':       { emoji: '⚡', color: '#F59E0B' },
  'Estrés':         { emoji: '💨', color: '#EF4444' },
  'Agobio':         { emoji: '🌊', color: '#06B6D4' },
  'Pánico':         { emoji: '😱', color: '#DC2626' },

  // Estados depresivos
  'Desesperanza':   { emoji: '🌑', color: '#475569' },
  'Vacío':          { emoji: '🫙', color: '#94A3B8' },
  'Soledad':        { emoji: '🌙', color: '#BBACA1' },
  'Duelo':          { emoji: '🕊️', color: '#64748B' },

  // Emociones sociales
  'Vergüenza':      { emoji: '😳', color: '#F43F5E' },
  'Culpa':          { emoji: '😔', color: '#78716C' },
  'Frustración':    { emoji: '😤', color: '#F97316' },
  'Irritabilidad':  { emoji: '😠', color: '#EF4444' },

  // Estados positivos / neutros
  'Calma':          { emoji: '🍃', color: '#10B981' },
  'Amor':           { emoji: '❤️', color: '#EC4899' },
  'Gratitud':       { emoji: '🌸', color: '#F472B6' },
  'Motivación':     { emoji: '🚀', color: '#8B5CF6' },

  // Estados disociativos o planos
  'Agotamiento':    { emoji: '😴', color: '#6B7280' },
  'Entumecimiento': { emoji: '🧊', color: '#93C5FD' },
  'Confusión':      { emoji: '🌀', color: '#A78BFA' },
  'Neutral':        { emoji: '😐', color: '#A0AEC0' },
}

function toEmotionCategory(r: EmotionalCategoryResponse): EmotionCategory {
  const v = VISUAL[r.name] ?? { emoji: '❓', color: '#A0AEC0' }
  return {
    emoji:      v.emoji,
    name:       r.name,
    flows:      r.flows,
    detect:     r.detect,
    severity:   r.severity,
    iconBg:     v.color + '33',
    barColor:   v.color,
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
