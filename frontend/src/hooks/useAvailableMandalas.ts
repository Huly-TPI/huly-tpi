import { useCallback, useEffect, useState } from 'react'
import { mandalasApi, type MandalaAccessType, type MandalaResponse, type MandalaUnlockSource } from '../api/mandalas'
import { mandalaAssetByKey } from '../components/Mandalas/mandalaAssets'
import type { MandalaCatalogItem } from '../components/Mandalas/mandalaTypes'
import { useAuth } from '../context/auth'

function toAccessStatus(source: MandalaUnlockSource): MandalaCatalogItem['accessStatus'] {
  return source === 'SUBSCRIPTION' ? 'included' : 'available'
}

function toUnlockSource(source: MandalaUnlockSource): MandalaCatalogItem['unlockSource'] {
  if (source === 'FREE') return 'free'
  if (source === 'PURCHASED') return 'purchased'
  return 'premiumPlan'
}

function toAccessType(type: MandalaAccessType): MandalaCatalogItem['accessType'] {
  if (type === 'FREE') return 'free'
  if (type === 'PURCHASABLE') return 'purchasable'
  return 'subscription'
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
    accessStatus: mandala.unlockSource ? toAccessStatus(mandala.unlockSource) : 'included',
    unlockSource: mandala.unlockSource ? toUnlockSource(mandala.unlockSource) : 'premiumPlan',
    accessType: toAccessType(mandala.accessType),
    isLocked: mandala.isLocked,
  }
}

export function useAvailableMandalas(pageSize = 6) {
  const { loading: authLoading } = useAuth()
  const [mandalas, setMandalas] = useState<MandalaCatalogItem[]>([])
  const [page, setPage] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [first, setFirst] = useState(true)
  const [last, setLast] = useState(true)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchPage = useCallback((nextPage: number) => {
    const safePage = Math.max(0, nextPage)
    setError(null)
    setLoading(true)
    return mandalasApi.getAvailable({ page: safePage, size: pageSize })
      .then(response => {
        setMandalas(response.content.map(toCatalogItem).filter((item): item is MandalaCatalogItem => item !== null))
        setPage(response.pageNumber)
        setTotalElements(response.totalElements)
        setTotalPages(response.totalPages)
        setFirst(response.first)
        setLast(response.last)
      })
      .catch(() => setError('No se pudieron cargar las mandalas disponibles.'))
      .finally(() => setLoading(false))
  }, [pageSize])

  const refetch = useCallback(() => fetchPage(page), [fetchPage, page])

  const goToPage = useCallback((nextPage: number) => {
    setPage(Math.max(0, nextPage))
  }, [])

  useEffect(() => {
    if (authLoading) return
    void fetchPage(page)
  }, [authLoading, fetchPage, page])

  return {
    mandalas,
    loading: loading || authLoading,
    error,
    refetch,
    page,
    pageSize,
    totalElements,
    totalPages,
    first,
    last,
    setPage: goToPage,
  }
}
