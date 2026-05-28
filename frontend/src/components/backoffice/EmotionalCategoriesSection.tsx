import { EmotionCard, EmotionCategory } from './EmotionCard'
import { SectionCard } from './SectionCard'

const EMOTIONAL_CATEGORIES: EmotionCategory[] = [
  { emoji: '💨', name: 'Estrés',   flows: 12, detect: 94, severity: 'ALTA',  iconBg: 'bg-[#FEE2E2]', barColor: 'bg-[#EF4444]', badgeColor: 'text-[#EF4444]' },
  { emoji: '⚡', name: 'Ansiedad', flows: 8,  detect: 88, severity: 'MEDIA', iconBg: 'bg-[#FFFBEB]', barColor: 'bg-[#F59E0B]', badgeColor: 'text-[#F59E0B]' },
  { emoji: '🌧️', name: 'Tristeza', flows: 15, detect: 91, severity: 'MEDIA', iconBg: 'bg-[#E0F2FE]', barColor: 'bg-[#3B82F6]', badgeColor: 'text-[#3B82F6]' },
  { emoji: '👻', name: 'Miedo',    flows: 5,  detect: 76, severity: 'BAJA',  iconBg: 'bg-[#FFF0F5]', barColor: 'bg-[#FF69B4]', badgeColor: 'text-[#FF69B4]' },
  { emoji: '🔥', name: 'Enojo',    flows: 9,  detect: 82, severity: 'MEDIA', iconBg: 'bg-[#FEF2F2]', barColor: 'bg-[#EF4444]', badgeColor: 'text-[#EF4444]' },
  { emoji: '🌙', name: 'Soledad',  flows: 4,  detect: 65, severity: 'BAJA',  iconBg: 'bg-[#EEEBE1]', barColor: 'bg-[#BBACA1]', badgeColor: 'text-[#BBACA1]' },
]

export function EmotionalCategoriesSection() {
  return (
    <SectionCard bg="bg-[#EDF2ED]" className="shadow-none">
      <div className="mb-5 flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="text-lg">😊</span>
          <h2 className="text-[15px] font-bold text-[#2D3748]">Panel de categorías emocionales</h2>
        </div>
        <button className="text-sm font-semibold text-[#8869AC] hover:underline">
          Gestionar todas
        </button>
      </div>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-6">
        {EMOTIONAL_CATEGORIES.map(cat => (
          <EmotionCard key={cat.name} category={cat} />
        ))}
      </div>
    </SectionCard>
  )
}
