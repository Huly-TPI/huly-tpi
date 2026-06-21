import { useCallback, useEffect, useState } from 'react'
import {
  clearMandalaProgress,
  loadMandalaProgress,
  saveMandalaProgress,
} from '../components/Mandalas/mandalaProgressStorage'

export function useMandalaProgress(mandalaId: string) {
  const [paintBlob, setPaintBlob] = useState<Blob | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setPaintBlob(null)

    loadMandalaProgress(mandalaId)
      .then(blob => {
        if (!cancelled) setPaintBlob(blob)
      })
      .catch(error => {
        console.error('No se pudo cargar el progreso del mandala', error)
        if (!cancelled) setPaintBlob(null)
      })
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
        await saveMandalaProgress(mandalaId, blob)
      } catch (error) {
        console.error('No se pudo guardar el progreso del mandala', error)
      }
    },
    [mandalaId],
  )

  const clearProgress = useCallback(async () => {
    setPaintBlob(null)
    try {
      await clearMandalaProgress(mandalaId)
    } catch (error) {
      console.error('No se pudo borrar el progreso del mandala', error)
    }
  }, [mandalaId])

  return {
    clearProgress,
    loading,
    paintBlob,
    saveProgress,
  }
}
