import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../api/apiError'
import { mandalasApi } from '../api/mandalas'

export function useMandalaProgress(mandalaId: string) {
  const [paintBlob, setPaintBlob] = useState<Blob | null>(null)
  const [sessionRegistered, setSessionRegistered] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setPaintBlob(null)
    setSessionRegistered(false)

    Promise.all([
      mandalasApi.getProgress(mandalaId)
        .then(blob => {
          if (!cancelled) setPaintBlob(blob)
        })
        .catch(error => {
          if (error instanceof ApiError && error.status === 404) {
            if (!cancelled) setPaintBlob(null)
            return
          }
          console.error('No se pudo cargar el progreso del mandala', error)
          if (!cancelled) setPaintBlob(null)
        }),
      mandalasApi.getSessionStatus(mandalaId)
        .then(response => {
          if (!cancelled) setSessionRegistered(response.sessionRegistered)
        })
        .catch(error => {
          console.error('No se pudo cargar el estado de sesion del mandala', error)
          if (!cancelled) setSessionRegistered(false)
        }),
    ])
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [mandalaId])

  const saveProgress = useCallback(
    async (blob: Blob) => {
      try {
        await mandalasApi.saveProgress(mandalaId, blob)
      } catch (error) {
        console.error('No se pudo guardar el progreso del mandala', error)
      }
    },
    [mandalaId],
  )

  const clearProgress = useCallback(async () => {
    setPaintBlob(null)
    setSessionRegistered(false)
    try {
      await mandalasApi.clearProgress(mandalaId)
    } catch (error) {
      console.error('No se pudo borrar el progreso del mandala', error)
    }
  }, [mandalaId])

  return {
    clearProgress,
    loading,
    paintBlob,
    saveProgress,
    sessionRegistered,
  }
}
