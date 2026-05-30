import { useState, useCallback, useRef, useEffect } from 'react'
import { chatbotApi, RiskWordResponse, RiskSeverity } from '../../api/chatbot'

const MOBILE_PAGE_SIZE = 20

export function useRiskWords() {
  const [words, setWords] = useState<RiskWordResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const pageSizeRef = useRef(MOBILE_PAGE_SIZE)

  const loadPage = useCallback(async (p = 0) => {
    setLoading(true)
    try {
      const res = await chatbotApi.listRiskWords({ page: p, size: pageSizeRef.current })
      setWords(res.content)
      setPage(res.page_number)
      setTotalPages(res.total_pages)
      setTotalElements(res.total_elements)
    } catch {
      setError('No se pudieron cargar las palabras de riesgo')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadPage(0) }, [loadPage])

  const handleAdd = useCallback(async (word: string, severity: RiskSeverity) => {
    try {
      await chatbotApi.createRiskWord({ word, severity })
      await loadPage(0)
    } catch (e) {
      const raw = e instanceof Error ? e.message : ''
      setError(raw.includes('409') ? 'Esta palabra ya existe en la lista' : 'No se pudo guardar la palabra')
    }
  }, [loadPage])

  const handleDelete = useCallback(async (id: number) => {
    setLoading(true)
    try {
      await chatbotApi.deleteRiskWord(id)
      const nextPage = words.length === 1 && page > 0 ? page - 1 : page
      await loadPage(nextPage)
    } catch {
      setError('No se pudo eliminar la palabra')
      setLoading(false)
    }
  }, [words.length, page, loadPage])

  const handlePageSizeChange = useCallback((size: number) => {
    if (size !== pageSizeRef.current) {
      pageSizeRef.current = size
      loadPage(0)
    }
  }, [loadPage])

  return {
    words, loading, page, totalPages, totalElements,
    error, clearError: () => setError(null),
    loadPage, handleAdd, handleDelete, handlePageSizeChange,
  }
}
