import { useWellbeing } from '../../hooks/backoffice/useWellbeing'
import { SectionCard, CardHeader } from './SectionCard'
import { WellbeingChart } from './WellbeingChart'
import { Skeleton } from './Skeleton'

export function WellbeingSection() {
  const { data, loading } = useWellbeing()

  return (
    <SectionCard className="h-full flex flex-col">
      <CardHeader title="Evolución del bienestar general" />
      {loading ? (
        <div className="space-y-3 flex-1 flex flex-col justify-center">
          <div className="flex justify-between">
            {Array.from({ length: 7 }).map((_, i) => <Skeleton key={i} className="h-2 w-6" />)}
          </div>
          <Skeleton className="h-[110px] w-full rounded-xl" />
        </div>
      ) : data ? (
        <div className="flex-1 flex items-center justify-center">
          <WellbeingChart points={data.points} labels={data.labels} />
        </div>
      ) : null}
    </SectionCard>
  )
}
