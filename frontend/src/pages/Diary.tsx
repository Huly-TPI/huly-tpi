import { useEffect, useState } from 'react'
import { journalApi, type JournalEntryResponse, type Mood } from '../api/journal'
import { useAuth } from '../context/auth'
import cloudImg from '../assets/garden/light-theme/cloud.webp'
import Button from '../components/Buttons/Button/Button'
import BackButton from '../components/Buttons/BackButton/BackButton'
import DiaryConsentModal from '../components/DiaryConsentModal'

function getDiaryConsentKey(userId: number): string {
  return `diaryTextConsent_${userId}`
}

const MOODS: { value: Mood; label: string; emoji: string }[] = [
  { value: 'HAPPY',     label: 'Feliz',      emoji: '😊' },
  { value: 'CALM',      label: 'Tranquilo',  emoji: '😌' },
  { value: 'MOTIVATED', label: 'Motivado',   emoji: '💪' },
  { value: 'NEUTRAL',   label: 'Neutro',     emoji: '😐' },
  { value: 'TIRED',     label: 'Cansado',    emoji: '😴' },
  { value: 'ANXIOUS',   label: 'Ansioso',    emoji: '😰' },
  { value: 'ANGRY',  label: 'Frustrado',  emoji: '😤' },
  { value: 'SAD',       label: 'Triste',     emoji: '😢' },
]

function formatDateLong(date: Date): string {
  return date.toLocaleDateString('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

function formatDateShort(isoString: string): string {
  const date = new Date(isoString)
  return date.toLocaleDateString('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

const LINE_BG = {
  backgroundImage:
    'repeating-linear-gradient(to bottom, transparent 0px, transparent 31px, #e5e7eb 31px, #e5e7eb 32px)',
  backgroundSize: '100% 32px',
  backgroundPositionY: '16px',
}

export default function Diary() {
  const { user } = useAuth()

  const [entries, setEntries] = useState<JournalEntryResponse[]>([])
  const [loadingEntries, setLoadingEntries] = useState(true)

  const [pageIndex, setPageIndex] = useState(0)

  const [adentro, setAdentro] = useState('')
  const [pensamiento, setPensamiento] = useState('')
  const [bien, setBien] = useState('')
  const [manana, setManana] = useState('')
  const [selectedMood, setSelectedMood] = useState<Mood | null>(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showConsentModal, setShowConsentModal] = useState(false)

  useEffect(() => {
    const main = document.querySelector('main') as HTMLElement | null
    if (main) main.style.overflow = 'hidden'
    return () => { if (main) main.style.overflow = '' }
  }, [])

  useEffect(() => {
    if (user) {
      setShowConsentModal(localStorage.getItem(getDiaryConsentKey(user.id)) === null)
    }
  }, [user])

  useEffect(() => {
    journalApi
      .list()
      .then(setEntries)
      .catch(() => {})
      .finally(() => setLoadingEntries(false))
  }, [])

  const totalPages = entries.length + 1
  const isNewEntry = pageIndex === 0
  const currentEntry: JournalEntryResponse | null =
    pageIndex > 0 ? entries[pageIndex - 1] : null

  function parseEntry(entry: JournalEntryResponse | null) {
    if (!entry) return { adentro: '', pensamiento: '', bien: '', manana: '' }
    try {
      const parsed = JSON.parse(entry.content)
      if (parsed && typeof parsed === 'object' && 'adentro' in parsed) {
        return {
          adentro: parsed.adentro ?? '',
          pensamiento: parsed.pensamiento ?? '',
          bien: parsed.bien ?? '',
          manana: parsed.manana ?? '',
        }
      }
    } catch {}
    return { adentro: entry.content, pensamiento: '', bien: '', manana: '' }
  }

  const parsed = parseEntry(currentEntry)

  const displayAdentro     = isNewEntry ? adentro     : parsed.adentro
  const displayPensamiento = isNewEntry ? pensamiento : parsed.pensamiento
  const displayBien        = isNewEntry ? bien        : parsed.bien
  const displayManana      = isNewEntry ? manana      : parsed.manana
  const displayMood: Mood | null = isNewEntry ? selectedMood : (currentEntry?.mood ?? null)
  const displayDate = isNewEntry
    ? formatDateLong(new Date())
    : formatDateShort(currentEntry!.createdAt)

  const hasContent = adentro.trim() || pensamiento.trim() || bien.trim() || manana.trim()

  const handleConsentAccept = () => {
    if (user) localStorage.setItem(getDiaryConsentKey(user.id), 'true')
    setShowConsentModal(false)
  }

  const handleConsentReject = () => {
    if (user) localStorage.setItem(getDiaryConsentKey(user.id), 'false')
    setShowConsentModal(false)
  }

  const handleSave = async () => {
    if (!hasContent) return
    setSaving(true)
    setError(null)
    try {
      const contentJson = JSON.stringify({
        adentro: adentro.trim(),
        pensamiento: pensamiento.trim(),
        bien: bien.trim(),
        manana: manana.trim(),
      })
      const useTextForAI = user ? localStorage.getItem(getDiaryConsentKey(user.id)) === 'true' : false
      const newEntry = await journalApi.create({ content: contentJson, mood: selectedMood, useTextForAI })
      setEntries((prev) => [newEntry, ...prev])
      setAdentro('')
      setPensamiento('')
      setBien('')
      setManana('')
      setSelectedMood(null)
      setPageIndex(0)
    } catch {
      setError('No se pudo guardar la entrada. Intentá de nuevo.')
    } finally {
      setSaving(false)
    }
  }

  const goToPrev = () => {
    if (pageIndex > 0) setPageIndex((p) => p - 1)
  }
  const goToNext = () => {
    if (pageIndex < totalPages - 1) setPageIndex((p) => p + 1)
  }

  const MAX_DOTS = 7
  const visibleDots = Math.min(totalPages, MAX_DOTS)

  return (
    <>
    {showConsentModal && (
      <DiaryConsentModal onAccept={handleConsentAccept} onReject={handleConsentReject} />
    )}
    <div
      className="h-screen flex flex-col items-center px-4 pt-16 pb-6 relative overflow-hidden"
      style={{
        background: 'linear-gradient(to bottom, #bde0f7 0%, #d4efc4 100%)',
      }}
    >
      <img src={cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-80 hidden lg:block"  style={{ zIndex: 0, width: 380, top: '1%',  left: '-6%' }} />
      <img src={cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-60 hidden lg:block"  style={{ zIndex: 0, width: 300, top: '0%',  right: '3%' }} />
      <img src={cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-50 hidden lg:block"  style={{ zIndex: 0, width: 340, top: '15%', right: '-5%' }} />
      <img src={cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-40 hidden lg:block"  style={{ zIndex: 0, width: 320, top: '22%', left: '-4%' }} />
      <BackButton />

      <div className="relative w-full max-w-4xl bg-white rounded-2xl shadow-2xl overflow-hidden border-2" style={{ borderColor: '#8869AC', zIndex: 1 }}>
        <div className="flex items-center justify-between px-5 py-3 border-b border-gray-200 bg-white">
          <div className="flex items-center gap-3">
            <span className="font-bold text-green-800 text-lg tracking-tight">huly</span>
            <span className="text-sm capitalize" style={{ color: '#D1CAEF' }}>{displayDate}</span>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1">
              <button
                onClick={goToPrev}
                disabled={pageIndex === 0}
                className="w-6 h-6 flex items-center justify-center rounded text-gray-400 hover:text-gray-700 disabled:opacity-25 transition-colors"
              >
                ‹
              </button>
              <span className="text-sm text-gray-500 min-w-[40px] text-center">
                {pageIndex + 1} / {totalPages}
              </span>
              <button
                onClick={goToNext}
                disabled={pageIndex >= totalPages - 1}
                className="w-6 h-6 flex items-center justify-center rounded text-gray-400 hover:text-gray-700 disabled:opacity-25 transition-colors"
              >
                ›
              </button>
            </div>
            <span className="text-xl">🎨</span>
            <Button
              variant="primary"
              size="sm"
              onClick={handleSave}
              disabled={!isNewEntry || !hasContent}
              isLoading={saving}
              loadingLabel="Guardando..."
            >
              💾 Guardar
            </Button>
          </div>
        </div>

        <div className="flex" style={{ minHeight: '520px', ...LINE_BG }}>
          <div className="flex-1 px-6 py-5 flex flex-col">
            <p className="text-[10px] uppercase tracking-widest font-semibold mb-3" style={{ color: '#649959' }}>
              Hoy
            </p>

            <p className="text-xs font-bold uppercase tracking-wide mb-3" style={{ color: '#649959' }}>
              ¿Cómo me sentí hoy?
            </p>
            <div className="grid grid-cols-4 gap-x-3 gap-y-3 mb-5">
              {MOODS.map((mood) => {
                const isSelected = displayMood === mood.value
                return (
                  <button
                    key={mood.value}
                    type="button"
                    disabled={!isNewEntry}
                    onClick={() =>
                      setSelectedMood(selectedMood === mood.value ? null : mood.value)
                    }
                    className="flex flex-col items-center gap-1 group"
                  >
                    <div
                      className={`w-12 h-12 rounded-full flex items-center justify-center text-xl transition-all ${
                        isSelected
                          ? 'bg-green-400 ring-2 ring-green-600 ring-offset-1'
                          : 'bg-green-200 group-hover:bg-green-300'
                      } ${!isNewEntry ? 'cursor-default' : 'cursor-pointer'}`}
                    >
                      {mood.emoji}
                    </div>
                    <span
                      className={`text-[11px] ${isSelected ? 'text-green-700 font-semibold' : 'text-gray-500'}`}
                    >
                      {mood.label}
                    </span>
                  </button>
                )
              })}
            </div>

            <p className="text-[10px] font-bold uppercase tracking-widest mb-1" style={{ color: '#8869AC' }}>
              Lo que pasa adentro
            </p>
            <textarea
              value={displayAdentro}
              onChange={(e) => isNewEntry && setAdentro(e.target.value)}
              readOnly={!isNewEntry}
              placeholder="Hoy me pasó..."
              className="flex-1 w-full resize-none border-none outline-none text-sm text-gray-700 leading-8 min-h-[180px] rounded-xl px-3 py-2 placeholder:text-[rgba(136,105,172,0.7)]"
              style={{ backgroundColor: 'rgba(209, 202, 239, 0.3)' }}
            />

            {error && <p className="text-red-500 text-xs mt-2">{error}</p>}

            <p className="text-[10px] italic mt-3" style={{ color: '#649959' }}>
              Tu espacio privado y seguro
            </p>
          </div>

          <div className="w-1 my-4" style={{ backgroundColor: '#8869AC' }} />

          <div className="flex-1 px-6 py-5 flex flex-col gap-5">
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest mb-1" style={{ color: '#8869AC' }}>
                ☁️ Un pensamiento que quiero soltar
              </p>
              <textarea
                value={displayPensamiento}
                onChange={(e) => isNewEntry && setPensamiento(e.target.value)}
                readOnly={!isNewEntry}
                placeholder="Lo que ya no quiero cargar..."
                rows={5}
                className="w-full resize-none border-none outline-none text-sm text-gray-700 leading-8 rounded-xl px-3 py-2 placeholder:text-[rgba(136,105,172,0.7)]"
                style={{ backgroundColor: 'rgba(209, 202, 239, 0.3)' }}
              />
            </div>

            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest mb-1" style={{ color: '#649959' }}>
                🌱 Algo que me salió bien hoy
              </p>
              <textarea
                value={displayBien}
                onChange={(e) => isNewEntry && setBien(e.target.value)}
                readOnly={!isNewEntry}
                placeholder="Hoy logré..."
                rows={4}
                className="w-full resize-none border-none outline-none text-sm text-gray-700 leading-8 rounded-xl px-3 py-2 placeholder:text-[rgba(100,153,89,0.7)]"
                style={{ backgroundColor: 'rgba(171, 203, 167, 0.3)' }}
              />
            </div>

            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest mb-1" style={{ color: '#F2C57C' }}>
                ☀️ Lo que quiero para mañana
              </p>
              <textarea
                value={displayManana}
                onChange={(e) => isNewEntry && setManana(e.target.value)}
                readOnly={!isNewEntry}
                placeholder="Mañana quiero..."
                rows={4}
                className="w-full resize-none border-none outline-none text-sm text-gray-700 leading-8 rounded-xl px-3 py-2 placeholder:text-[#F2C57C]"
                style={{ backgroundColor: 'rgba(244, 211, 138, 0.3)' }}
              />
            </div>
          </div>
        </div>
      </div>

      {!loadingEntries && (
        <div className="relative flex gap-2 mt-5" style={{ zIndex: 1 }}>
          {Array.from({ length: visibleDots }).map((_, i) => (
            <button
              key={i}
              onClick={() => setPageIndex(i)}
              className={`rounded-full transition-all ${
                pageIndex === i
                  ? 'w-3 h-3 bg-purple-600'
                  : 'w-2.5 h-2.5 bg-gray-400 hover:bg-gray-500'
              }`}
            />
          ))}
        </div>
      )}
    </div>
    </>
  )
}
