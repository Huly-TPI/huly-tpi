import { useState, useEffect, useRef } from 'react'
import { RiskWordResponse, RiskSeverity } from '../../api/chatbot'
import { SectionCard, CardHeader } from './SectionCard'
import { RiskWordItem } from './RiskWordItem'
import { Skeleton } from './Skeleton'
import Button from '../Buttons/Button/Button'

const ITEM_H = 50

export interface RiskDetectionSectionProps {
  words: RiskWordResponse[]
  loading: boolean
  page: number
  totalPages: number
  totalElements: number
  onPrev: () => void
  onNext: () => void
  onAdd: (word: string, severity: RiskSeverity) => Promise<void>
  onDelete: (id: number) => Promise<void>
  onPageSizeChange: (size: number) => void
}

export function RiskDetectionSection({
  words, loading, page, totalPages, totalElements,
  onPrev, onNext, onAdd, onDelete, onPageSizeChange,
}: RiskDetectionSectionProps) {
  const [showForm, setShowForm] = useState(false)
  const [newWord, setNewWord] = useState('')
  const [newSeverity, setNewSeverity] = useState<RiskSeverity>('HIGH')
  const [saving, setSaving] = useState(false)
  const listSlotRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const el = listSlotRef.current
    if (!el) return
    const ro = new ResizeObserver(() => {
      if (window.innerWidth < 1024) return
      const size = Math.max(1, Math.floor((el.clientHeight + 8) / ITEM_H))
      onPageSizeChange(size)
    })
    ro.observe(el)
    return () => ro.disconnect()
  }, [onPageSizeChange])

  async function handleAdd() {
    const trimmed = newWord.trim()
    if (!trimmed) return
    setSaving(true)
    try {
      await onAdd(trimmed, newSeverity)
      setNewWord('')
      setShowForm(false)
    } finally {
      setSaving(false)
    }
  }

  const hasPrev = page > 0
  const hasNext = page < totalPages - 1

  return (
    <SectionCard className="flex flex-col lg:h-full">
      <CardHeader
        title="Detección de Riesgo"
        action={totalElements > 0 ? <span className="text-xs text-[#A0AEC0]">{totalElements} frases</span> : undefined}
      />

      <div ref={listSlotRef} className="flex-1 min-h-0">
        {loading ? (
          <ul className="flex flex-col gap-2">
            {Array.from({ length: 5 }).map((_, i) => (
              <li key={i} className="flex items-center justify-between gap-3 rounded-xl bg-white px-4 py-3">
                <Skeleton className="h-4 flex-1" />
                <Skeleton className="h-5 w-[62px] shrink-0 rounded-full" />
                <Skeleton className="h-3.5 w-3.5 shrink-0" />
              </li>
            ))}
          </ul>
        ) : (
          <ul className="flex flex-col gap-2">
            {words.map(w => <RiskWordItem key={w.id} word={w} onDelete={onDelete} />)}
            {words.length === 0 && (
              <p className="py-4 text-center text-xs text-[#A0AEC0]">No hay frases de riesgo cargadas.</p>
            )}
          </ul>
        )}
      </div>

      {totalElements > 0 && (
        <div className="mt-3 flex items-center justify-between">
          <button
            onClick={onPrev}
            disabled={!hasPrev || loading}
            className="flex h-8 w-8 items-center justify-center rounded-full border border-[#EDF2ED] bg-white text-[#4A5568] shadow-sm transition-colors hover:border-[#8869AC] hover:text-[#8869AC] disabled:opacity-30 disabled:cursor-not-allowed"
            aria-label="Página anterior"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <span className="text-xs font-semibold text-[#A0AEC0]">{page + 1} / {Math.max(1, totalPages)}</span>
          <button
            onClick={onNext}
            disabled={!hasNext || loading}
            className="flex h-8 w-8 items-center justify-center rounded-full border border-[#EDF2ED] bg-white text-[#4A5568] shadow-sm transition-colors hover:border-[#8869AC] hover:text-[#8869AC] disabled:opacity-30 disabled:cursor-not-allowed"
            aria-label="Página siguiente"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      )}

      {showForm && (
        <div className="mt-3 flex flex-col gap-2 rounded-xl border border-[#D1CAEF] bg-[#D1CAEF]/20 p-3">
          <input
            autoFocus
            type="text"
            placeholder="Escribir frase clave..."
            value={newWord}
            onChange={e => setNewWord(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAdd()}
            className="rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:border-[#8869AC]"
          />
          <select
            value={newSeverity}
            onChange={e => setNewSeverity(e.target.value as RiskSeverity)}
            className="rounded-xl border border-gray-200 bg-white px-3 py-2 text-xs text-[#4A5568] focus:outline-none focus:border-[#8869AC]"
          >
            <option value="HIGH">CRÍTICO (Alto)</option>
            <option value="MEDIUM">ALTO (Medio)</option>
            <option value="LOW">INFO (Bajo)</option>
          </select>
          <div className="flex gap-2">
            <Button
              variant="primary"
              size="sm"
              onClick={handleAdd}
              disabled={saving || !newWord.trim()}
              isLoading={saving}
              loadingLabel="Guardando..."
              className="flex-1"
            >
              Guardar
            </Button>
            <Button
              variant="secondary"
              size="sm"
              onClick={() => { setShowForm(false); setNewWord('') }}
            >
              Cancelar
            </Button>
          </div>
        </div>
      )}

      {!showForm && (
        <Button
          variant="secondary"
          fullWidth
          onClick={() => setShowForm(true)}
          className="mt-3"
        >
          + Nueva frase clave
        </Button>
      )}
    </SectionCard>
  )
}
