import { useEffect, useState } from 'react'
import { adminActivitiesApi, AdminActivityPopularityResponse } from '../../api/adminActivities'

export function useActivities() {
  const [popularity, setPopularity] = useState<AdminActivityPopularityResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    adminActivitiesApi.getPopularity()
      .then(data => setPopularity(data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return { popularity, loading }
}
