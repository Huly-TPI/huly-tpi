import { useEmotionalCategories } from '../../hooks/backoffice/useEmotionalCategories'
import { EmotionCard } from './EmotionCard'
import { SectionCard, CardHeader } from './SectionCard'
import { Skeleton } from './Skeleton'

export function EmotionalCategoriesSection() {
  const { categories, loading } = useEmotionalCategories()

  return (
    <SectionCard className="h-full flex flex-col">
      <CardHeader title="Panel de categorías emocionales" />

      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 flex-1">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="flex flex-col rounded-2xl bg-[#EDF2ED] dark:bg-[#09111f] p-4 shadow-sm">
              <Skeleton className="h-11 w-11 rounded-xl" />
              <Skeleton className="mt-3 h-4 w-3/4" />
              <Skeleton className="mt-1 h-3 w-full" />
              <Skeleton className="mt-1 h-3 w-full" />
              <Skeleton className="mt-3 h-[3px] w-full rounded-full" />
              <div className="mt-2 flex items-center justify-between">
                <Skeleton className="h-3 w-10" />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 flex-1">
          {categories.map(cat => (
            <EmotionCard key={cat.name} category={cat} />
          ))}
        </div>
      )}
    </SectionCard>
  )
}
