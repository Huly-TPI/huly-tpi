import { useActivities } from '../../hooks/backoffice/useActivities'
import { ActivityCard } from './ActivityCard'
import { SectionCard, CardHeader } from './SectionCard'
import { Skeleton } from './Skeleton'
import Button from '../Buttons/Button/Button'

export function ActivitiesSection() {
  const { activities, loading } = useActivities()

  return (
    <SectionCard>
      <CardHeader
        title="Biblioteca de Actividades"
        action={<Button variant="tertiary" size="sm">Editar todo</Button>}
      />
      {loading ? (
        <div className="grid grid-cols-2 gap-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="flex items-center gap-3 rounded-xl border border-gray-100 dark:border-gray-800/60 p-3">
              <Skeleton className="h-9 w-9 shrink-0 rounded-xl" />
              <div className="min-w-0 flex-1 space-y-1.5">
                <Skeleton className="h-3 w-4/5" />
                <Skeleton className="h-2 w-full rounded-full" />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3">
          {activities.map(act => <ActivityCard key={act.name} activity={act} />)}
        </div>
      )}
    </SectionCard>
  )
}
