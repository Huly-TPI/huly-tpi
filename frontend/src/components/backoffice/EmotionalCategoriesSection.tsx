import { useEmotionalCategories } from '../../hooks/backoffice/useEmotionalCategories'
import { EmotionCard } from './EmotionCard'
import { SectionCard } from './SectionCard'
import { Skeleton } from './Skeleton'
import purpleSmile from '../../assets/backoffice/purpleSmile.webp'
import Button from '../Buttons/Button/Button'

export function EmotionalCategoriesSection() {
  const { categories, loading } = useEmotionalCategories()

  return (
    <SectionCard bg="bg-[#EDF2ED] dark:bg-[#09111f]" padding="p-0" className="shadow-none">
      <div className="mb-4 flex items-center justify-between gap-2 px-4 pt-4 lg:px-5 lg:pt-5">
        <div className="flex items-center gap-2">
          <img src={purpleSmile} alt="" className="w-6 h-6 object-contain -translate-y-0.0" />
          <h2 className="text-[20px] font-bold text-[#2D3748] dark:text-gray-100">Panel de categorías emocionales</h2>
        </div>
        <Button variant="tertiary" size="sm">
          Gestionar todas
        </Button>
      </div>

      {loading ? (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-6">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="flex flex-col rounded-2xl bg-white dark:bg-[#172033] p-5 shadow-sm">
              <Skeleton className="h-14 w-14 rounded-2xl" />
              <Skeleton className="mt-4 h-4 w-3/4" />
              <Skeleton className="mt-1 h-3 w-full" />
              <Skeleton className="mt-4 h-[3px] w-full rounded-full" />
              <div className="mt-3 flex items-center justify-between">
                <Skeleton className="h-3 w-10" />
                <Skeleton className="h-4 w-4 rounded" />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-6">
          {categories.map(cat => <EmotionCard key={cat.name} category={cat} />)}
        </div>
      )}
    </SectionCard>
  )
}
