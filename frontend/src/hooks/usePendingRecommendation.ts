import { useState, useEffect, useCallback, useMemo } from 'react'
import { pendingApi, type PendingRecommendationResponse } from '../api/pending'

export function usePendingRecommendation() {
  const [recommendation, setRecommendation] = useState<PendingRecommendationResponse | null>(null)
  const [loading, setLoading] = useState(false)

  const fetchRecommendation = useCallback(async () => {
    setLoading(true)
    try {
      const res = await pendingApi.getTodayRecommendation()
      setRecommendation(res)
    } catch {
      setRecommendation(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void fetchRecommendation()
  }, [fetchRecommendation])

  const accept = useCallback(async () => {
    if (!recommendation) return
    const updated = await pendingApi.respondToRecommendation(recommendation.recommendationId, 'ACCEPTED')
    setRecommendation(updated)
  }, [recommendation])

  const reject = useCallback(async () => {
    if (!recommendation) return
    const updated = await pendingApi.respondToRecommendation(recommendation.recommendationId, 'REJECTED')
    setRecommendation(updated)
  }, [recommendation])

  const requestOnDemand = useCallback(async (): Promise<boolean> => {
    const res = await pendingApi.generateRecommendation()
    setRecommendation(res)
    return res !== null
  }, [])

  const hasUnrespondedRecommendation = useMemo(
    () => recommendation !== null && recommendation.decision === 'PENDING',
    [recommendation],
  )

  const recommendedTaskIds = useMemo(
    () =>
      recommendation && recommendation.decision === 'ACCEPTED'
        ? new Set(recommendation.recommendedTaskIds)
        : new Set<number>(),
    [recommendation],
  )

  return {
    recommendation,
    loading,
    hasUnrespondedRecommendation,
    recommendedTaskIds,
    fetchRecommendation,
    accept,
    reject,
    requestOnDemand,
  }
}
