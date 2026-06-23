import { useCallback, useEffect, useState } from 'react'
import { mandalasApi, type MandalaResponse } from '../api/mandalas'
import { mandalaAssetByKey } from '../components/Mandalas/mandalaAssets'
import type { MandalaCatalogItem } from '../components/Mandalas/mandalaTypes'
import { useAuth } from '../context/auth'

function toAccessStatus(source: MandalaResponse['unlockSource']): MandalaCatalogItem['accessStatus'] {
  return source === 'SUBSCRIPTION' ? 'included' : 'available'
}

function toUnlockSource(source: MandalaResponse['unlockSource']): MandalaCatalogItem['unlockSource'] {
  if (source === 'FREE') return 'free'
  if (source === 'PURCHASED') return 'purchased'
  return 'premiumPlan'
}

function toCatalogItem(mandala: MandalaResponse): MandalaCatalogItem | null {
  const src = mandalaAssetByKey[mandala.assetKey]
  if (!src) {
    console.warn(`No existe asset local para la mandala ${mandala.assetKey}`)
    return null
  }
  return {
    id: mandala.id,
    title: mandala.title,
    description: mandala.description,
    src,
    accessStatus: toAccessStatus(mandala.unlockSource),
    unlockSource: toUnlockSource(mandala.unlockSource),
  }
}

export function useAvailableMandalas() {
  const { loading: authLoading } = useAuth()
  const [mandalas, setMandalas] = useState<MandalaCatalogItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const refetch = useCallback(() => {
    setError(null)
    setLoading(true)
    return mandalasApi.getAvailable()
      .then(response => setMandalas(response.map(toCatalogItem).filter((item): item is MandalaCatalogItem => item !== null)))
      .catch(() => setError('No se pudieron cargar las mandalas disponibles.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (authLoading) return
    void refetch()
  }, [authLoading, refetch])

  return { mandalas, loading: loading || authLoading, error, refetch }
}
