import { useState, useEffect } from 'react'
import { chatbotApi, EmotionalCategoryResponse } from '../../api/chatbot'
import { EmotionCard, EmotionCategory } from './EmotionCard'
import { SectionCard } from './SectionCard'
import purpleSmile from '../../assets/backoffice/purpleSmile.webp'

const VISUAL: Record<string, { emoji: string; color: string }> = {
  'Estrés':   { emoji: '💨', color: '#EF4444' },
  'Ansiedad': { emoji: '⚡', color: '#F59E0B' },
  'Tristeza': { emoji: '🌧️', color: '#3B82F6' },
  'Miedo':    { emoji: '👻', color: '#FF69B4' },
  'Enojo':    { emoji: '🔥', color: '#EF4444' },
  'Soledad':  { emoji: '🌙', color: '#BBACA1' },
}

function toEmotionCategory(r: EmotionalCategoryResponse): EmotionCategory {
  const v = VISUAL[r.name] ?? { emoji: '❓', color: '#A0AEC0' }
  return {
    emoji:      v.emoji,
    name:       r.name,
    flows:      r.flows,
    detect:     r.detect,
    severity:   r.severity,
    iconBg:     v.color + '33',  // hex color at ~20% opacity
    barColor:   v.color,
    badgeColor: v.color,
  }
}

export function EmotionalCategoriesSection() {
  const [categories, setCategories] = useState<EmotionCategory[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getEmotionalCategories()
      .then(data => setCategories(data.map(toEmotionCategory)))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return (
    <SectionCard bg="bg-[#EDF2ED]" padding="p-0" className="shadow-none">
      <div className="mb-4 flex items-center justify-between gap-2 px-4 pt-4 lg:px-5 lg:pt-5">
        <div className="flex items-center gap-2">
          <img src={purpleSmile} alt="" className="w-6 h-6 object-contain -translate-y-0.0" />
          <h2 className="text-[20px] font-bold text-[#2D3748]">Panel de categorías emocionales</h2>
        </div>
        <button className="text-sm font-semibold text-[#8869AC] hover:underline">
          Gestionar todas
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-8">
          <div className="h-6 w-6 animate-spin rounded-full border-2 border-[#8869AC] border-t-transparent" />
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-6">
          {categories.map(cat => <EmotionCard key={cat.name} category={cat} />)}
        </div>
      )}
    </SectionCard>
  )
}
