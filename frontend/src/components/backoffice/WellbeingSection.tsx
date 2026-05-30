import { useWellbeing } from '../../hooks/backoffice/useWellbeing'
import { SectionCard, CardHeader } from './SectionCard'
import { WellbeingChart } from './WellbeingChart'
import { Skeleton } from './Skeleton'

export function WellbeingSection() {
  const { data, loading } = useWellbeing()

  return (
    <SectionCard>
      <CardHeader title="Evolución del bienestar general" />
      {loading ? (
        <div className="space-y-3">
          <div className="flex justify-between">
            {Array.from({ length: 7 }).map((_, i) => <Skeleton key={i} className="h-2 w-6" />)}
          </div>
          <Skeleton className="h-[110px] w-full rounded-xl" />
        </div>
      ) : data ? (
        <WellbeingChart points={data.points} labels={data.labels} />
      ) : null}
    </SectionCard>
  )
}
