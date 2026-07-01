import { useCallback, useEffect, useRef } from 'react'
import { ActivityType, registerActivitySession } from '../api/activities'

interface ActivitySessionTrackerOptions {
  minDurationSeconds?: number
  autoStart?: boolean
  contextId?: string
}

export function useActivitySessionTracker(
  activityType: ActivityType,
  trackerOptions?: ActivitySessionTrackerOptions,
) {
  const startedAtRef = useRef<number | null>(null)
  const isConditionMetRef = useRef(false)
  const isSavedRef = useRef(false)
  
  const startSession = useCallback(() => {
    if (startedAtRef.current === null) {
      startedAtRef.current = Date.now()
    }
  }, [])

  const markConditionMet = useCallback(() => {
    isConditionMetRef.current = true
  }, [])

  const stopSession = useCallback(() => {
    startedAtRef.current = null
    isConditionMetRef.current = false
    isSavedRef.current = false
  }, [])

  const saveSession = useCallback(
    async (options?: { keepalive?: boolean; force?: boolean }) => {
      if (!startedAtRef.current || isSavedRef.current) return

      if (!options?.force) {
        if (!isConditionMetRef.current) return
        if (trackerOptions?.minDurationSeconds) {
          const elapsed = (Date.now() - startedAtRef.current) / 1000
          if (elapsed < trackerOptions.minDurationSeconds) return
        }
      }

      isSavedRef.current = true
      try {
        await registerActivitySession(
          {
            activityType,
            contextId: trackerOptions?.contextId,
          },
          options,
        )
      } catch (error) {
        if (!options?.keepalive) isSavedRef.current = false
      }
    },
    [activityType, trackerOptions?.contextId, trackerOptions?.minDurationSeconds],
  )

  useEffect(() => {
    if (trackerOptions?.autoStart) {
      startSession()
    }
  }, [trackerOptions?.autoStart, startSession])

  useEffect(() => {
    const handlePageHide = () => {
      void saveSession({ keepalive: true })
    }
    window.addEventListener('pagehide', handlePageHide)
    return () => {
      window.removeEventListener('pagehide', handlePageHide)
      void saveSession({ keepalive: true })
    }
  }, [saveSession])

  return {
    startSession,
    markConditionMet,
    stopSession,
    saveSession,
  }
}
