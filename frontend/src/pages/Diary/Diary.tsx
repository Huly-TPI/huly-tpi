import { useEffect, useState } from 'react'
import './Diary.css'
import { journalApi, type JournalEntryResponse, type Mood } from '../../api/journal.ts'
import { useAuth } from '../../context/auth.tsx'
import { useToast } from '../../context/toast.tsx'
import cloudImg from '../../assets/garden/light-theme/cloud.webp'
import dayBackground from '../../assets/shared/day-background.webp'
import darkCloudImg from '../../assets/garden/dark-theme/cloud.webp'
import nightBackground from '../../assets/shared/dark-background.webp'
import diaryBg from '../../assets/diary/diary-background.png'
import mobileDiaryBg from '../../assets/diary/mobile-diary-background.png'
import Button from '../../components/Buttons/Button/Button.tsx'
import BackButton from '../../components/Buttons/BackButton/BackButton.tsx'
import DiaryConsentModal from '../../components/DiaryConsentModal.tsx'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground.tsx'
import { useTheme } from '../../context/theme.tsx'
import { useAuthGate } from '../../context/authGate.tsx'
import { ActivityType } from '../../api/activities'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'

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

function formatDateMobile(date: Date): string {
  return date.toLocaleDateString('es-AR', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
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

function formatDateShortMobile(isoString: string): string {
  const date = new Date(isoString)
  return date.toLocaleDateString('es-AR', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  })
}

export default function Diary() {
  const { user } = useAuth()
  const { theme } = useTheme()
  const { showToast } = useToast()
  const isDark = theme === 'dark'
  const { requireAuth } = useAuthGate()
  const cardText = isDark ? '#dbe7f5' : '#3d2b1a'
  const lineColor = isDark ? 'rgba(148, 163, 184, 0.18)' : 'rgba(120, 90, 50, 0.12)'
  const accentBg   = isDark ? 'rgba(92, 120, 172, 0.20)' : 'rgba(209, 202, 239, 0.28)'
  const successBg  = isDark ? 'rgba(109, 138, 152, 0.20)' : 'rgba(171, 203, 167, 0.30)'
  const warningBg  = isDark ? 'rgba(132, 124, 110, 0.20)' : 'rgba(244, 211, 138, 0.32)'
  const moodIdle   = isDark ? '#4a3d6e' : '#e8e0f5'
  const moodSelected = isDark ? '#5f4a8a' : '#D1CAEF'
  const moodRing   = isDark ? '#a7f3d0' : '#8869AC'

  const lineBackground = {
    backgroundImage: `repeating-linear-gradient(to bottom, transparent 0px, transparent 31px, ${lineColor} 31px, ${lineColor} 32px)`,
    backgroundSize: '100% 32px',
    backgroundPositionY: '16px',
  }

  const pagerButtonStyle = isDark
    ? { color: '#cbd5e1', backgroundColor: 'rgba(0,0,0,0.18)' }
    : { color: '#7a5c38', backgroundColor: 'transparent' }

  const saveButtonClassName = isDark
    ? '!w-auto !min-w-0 !border-[#5c4a86] !bg-[#5f4a8a] hover:!shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.18)]'
    : '!w-auto !min-w-0'

  const [entries, setEntries] = useState<JournalEntryResponse[]>([])
  const [loadingEntries, setLoadingEntries] = useState(true)

  const [pageIndex, setPageIndex] = useState(0)

  const [adentro, setAdentro] = useState('')
  const [pensamiento, setPensamiento] = useState('')
  const [bien, setBien] = useState('')
  const [manana, setManana] = useState('')
  const [selectedMood, setSelectedMood] = useState<Mood | null>(null)
  const [saving, setSaving] = useState(false)
  const [showConsentModal, setShowConsentModal] = useState(false)
  const [isMobile, setIsMobile] = useState(
    () => typeof window !== 'undefined' && typeof window.matchMedia === 'function' && window.matchMedia('(max-width: 767px)').matches
  )

  const { startSession, markConditionMet, saveSession, stopSession } = useActivitySessionTracker(
    ActivityType.DIARY
  )

  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return
    const mql = window.matchMedia('(max-width: 767px)')
    const onChange = (e: MediaQueryListEvent) => setIsMobile(e.matches)
    setIsMobile(mql.matches)
    mql.addEventListener('change', onChange)
    return () => mql.removeEventListener('change', onChange)
  }, [])

  const cardBg = isMobile ? mobileDiaryBg : diaryBg

  useEffect(() => {
    if (adentro.trim() || pensamiento.trim() || bien.trim() || manana.trim() || selectedMood !== null) {
      startSession()
      markConditionMet()
    }
  }, [adentro, pensamiento, bien, manana, selectedMood, startSession, markConditionMet])

  useEffect(() => {
    if (window.innerWidth < 768) return
    const main = document.querySelector('main') as HTMLElement | null
    if (main) {
      main.style.overflow = 'hidden'
      return () => { main.style.overflow = '' }
    }
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
  const displayDateMobile = isNewEntry
    ? formatDateMobile(new Date())
    : formatDateShortMobile(currentEntry!.createdAt)

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
    try {
      const contentJson = JSON.stringify({
        adentro: adentro.trim(),
        pensamiento: pensamiento.trim(),
        bien: bien.trim(),
        manana: manana.trim(),
      })
      const useTextForAI = user ? localStorage.getItem(getDiaryConsentKey(user.id)) === 'true' : false
      const newEntry = await journalApi.create({ content: contentJson, mood: selectedMood, useTextForAI })
      
      await saveSession()
      stopSession()

      setEntries((prev) => [newEntry, ...prev])
      setAdentro('')
      setPensamiento('')
      setBien('')
      setManana('')
      setSelectedMood(null)
      setPageIndex(0)
      showToast('Entrada al diario guardada correctamente.', 'success')
    } catch {
      showToast('No se pudo guardar la entrada. Intentá de nuevo.', 'error')
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
      className="flex flex-col items-center px-2 sm:px-4 pt-12 sm:pt-16 pb-4 relative md:h-full md:overflow-hidden"
    >
      <ThemeBackground
        lightSrc={dayBackground}
        darkSrc={nightBackground}
        lightAlt="Fondo del diario"
        darkAlt="Fondo nocturno del diario"
      />
      <img src={theme === 'dark' ? darkCloudImg : cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-80 hidden lg:block"  style={{ zIndex: 0, width: 380, top: '1%',  left: '-6%' }} />
      <img src={theme === 'dark' ? darkCloudImg : cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-60 hidden lg:block"  style={{ zIndex: 0, width: 300, top: '0%',  right: '3%' }} />
      <img src={theme === 'dark' ? darkCloudImg : cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-50 hidden lg:block"  style={{ zIndex: 0, width: 340, top: '15%', right: '-5%' }} />
      <img src={theme === 'dark' ? darkCloudImg : cloudImg} alt="" aria-hidden className="absolute pointer-events-none select-none opacity-40 hidden lg:block"  style={{ zIndex: 0, width: 320, top: '22%', left: '-4%' }} />
      <BackButton to="/"/>

      <div
        className="diary-card relative h-screen w-full max-w-4xl bg-no-repeat [background-size:100%_100%] bg-center rounded-[6px] overflow-hidden drop-shadow-[0_8px_24px_rgba(0,0,0,0.3)] flex flex-col md:max-h-[calc(100dvh-120px)]"
        style={{ backgroundImage: `url(${cardBg})`, color: cardText, zIndex: 1 }}
      >
        {isDark && (
          <div className="absolute inset-0 bg-[#1a1008]/[0.3] pointer-events-none" aria-hidden="true" />
        )}
        <div className="relative z-10 flex flex-col flex-1 min-h-0 pt-4 md:pt-6 px-[4%] md:px-[5%] pb-4 md:pb-6">
        <div
          className="flex items-center justify-between p-3 border-b gap-2 flex-shrink-0"
          style={{ borderColor: lineColor }}
        >
          <div className="flex items-center gap-2 min-w-0">
            <span className="font-bold text-green-800 text-lg tracking-tight flex-shrink-0 dark:text-menta">huly</span>
            <span className="sm:hidden text-xs capitalize" style={{ color: '#8869AC' }}>{displayDateMobile}</span>
            <span className="hidden sm:inline text-sm capitalize truncate" style={{ color: '#8869AC' }}>{displayDate}</span>
          </div>
          <div className="flex items-center gap-2 sm:gap-4 flex-shrink-0">
            <div className="flex items-center gap-1">
              <button
                onClick={goToPrev}
                disabled={pageIndex === 0}
                className="w-6 h-6 flex items-center justify-center rounded disabled:opacity-25 transition-colors"
                style={pagerButtonStyle}
              >
                ‹
              </button>
              <span className="text-xs sm:text-sm min-w-[28px] sm:min-w-[40px] text-center" style={{ color: isDark ? '#e2e8f0' : '#7a5c38' }}>
                {pageIndex + 1} / {totalPages}
              </span>
              <button
                onClick={goToNext}
                disabled={pageIndex >= totalPages - 1}
                className="w-6 h-6 flex items-center justify-center rounded disabled:opacity-25 transition-colors"
                style={pagerButtonStyle}
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
              loadingLabel="..."
              className={saveButtonClassName}
            >
              💾<span className="hidden sm:inline"> Guardar</span>
            </Button>
          </div>
        </div>

        <div className="diary-content flex flex-col md:flex-row flex-1 min-h-0 overflow-y-auto md:overflow-hidden" style={{ ...lineBackground }}>
          <div className="diary-left md:flex-1 md:min-h-0 px-2 py-5 flex flex-col">
            <p className="text-[10px] uppercase tracking-widest font-semibold mb-3 lg:text-[11px]" style={{ color: '#649959' }}>
              Hoy
            </p>

            <p className="text-xs font-bold uppercase tracking-wide mb-3 lg:text-[13px]" style={{ color: '#649959' }}>
              ¿Cómo me sentí hoy?
            </p>
            <div className="diary-mood-grid grid grid-cols-4 gap-x-2 gap-y-3 mb-5">
              {MOODS.map((mood) => {
                const isSelected = displayMood === mood.value
                return (
                  <button
                    key={mood.value}
                    type="button"
                    disabled={!isNewEntry}
                    onClick={() =>
                      requireAuth(() => setSelectedMood(selectedMood === mood.value ? null : mood.value))
                    }
                    className="flex flex-col items-center gap-1 group"
                  >
                    <div
                      className={`diary-mood-circle w-10 h-10 sm:w-12 sm:h-12 rounded-full flex items-center justify-center text-lg sm:text-xl transition-all group-hover:brightness-110 ${
                        isSelected ? 'ring-2 ring-offset-1' : ''
                      } ${!isNewEntry ? 'cursor-default' : 'cursor-pointer'}`}
                      style={{
                        backgroundColor: isSelected ? moodSelected : moodIdle,
                        outlineColor: isSelected ? moodRing : undefined,
                        boxShadow: isSelected ? `0 0 0 2px ${moodRing}` : undefined,
                      }}
                    >
                      {mood.emoji}
                    </div>
                    <span
                      className={`text-[11px] lg:text-[12px] ${isSelected ? 'font-semibold' : ''}`}
                      style={{ color: isSelected ? (isDark ? '#a7f3d0' : '#5f4a8a') : (isDark ? '#94a3b8' : '#7a5c38') }}
                    >
                      {mood.label}
                    </span>
                  </button>
                )
              })}
            </div>

            <p className="text-[10px] font-bold uppercase tracking-widest mb-1 lg:text-[11px]" style={{ color: '#8869AC' }}>
              Lo que pasa adentro
            </p>
            <textarea
              value={displayAdentro}
              onChange={(e) => isNewEntry && setAdentro(e.target.value)}
              readOnly={!isNewEntry || !user}
              onFocus={() => !user && requireAuth(() => {})}
              placeholder="Hoy me pasó..."
              className="flex-1 min-h-[120px] md:min-h-0 w-full md:w-[95%] resize-none border-none outline-none text-sm leading-8 rounded-xl px-3 py-2 placeholder:text-[rgba(136,105,172,0.7)]"
              style={{ backgroundColor: accentBg, color: cardText }}
            />



            <p className="text-[10px] italic mt-3" style={{ color: '#649959' }}>
              Tu espacio privado y seguro
            </p>
          </div>

          <div className="hidden md:block w-px my-4" style={{ backgroundColor: 'rgba(120, 90, 50, 0.25)' }} />
          <div className="block md:hidden h-px mx-4" style={{ backgroundColor: 'rgba(120, 90, 50, 0.25)' }} />

          <div className="diary-right md:flex-1 md:min-h-0 px-2 md:pl-6 md:pr-2 pt-5 pb-8 flex flex-col gap-5">
            <div className="flex-1 min-h-0 flex flex-col">
              <p className="text-[10px] font-bold uppercase tracking-widest mb-1 flex-shrink-0 lg:text-[11px]" style={{ color: '#8869AC' }}>
                ☁️ Un pensamiento que quiero soltar
              </p>
              <textarea
                value={displayPensamiento}
                onChange={(e) => isNewEntry && setPensamiento(e.target.value)}
                readOnly={!isNewEntry || !user}
                onFocus={() => !user && requireAuth(() => {})}
                placeholder="Lo que ya no quiero cargar..."
                className="flex-1 min-h-[90px] md:min-h-0 w-full resize-none border-none outline-none text-sm leading-8 rounded-xl px-3 py-2 placeholder:text-[rgba(136,105,172,0.7)]"
                style={{ backgroundColor: accentBg, color: cardText }}
              />
            </div>

            <div className="flex-1 min-h-0 flex flex-col">
              <p className="text-[10px] font-bold uppercase tracking-widest mb-1 flex-shrink-0 lg:text-[11px]" style={{ color: '#649959' }}>
                🌱 Algo que me salió bien hoy
              </p>
              <textarea
                value={displayBien}
                onChange={(e) => isNewEntry && setBien(e.target.value)}
                readOnly={!isNewEntry || !user}
                onFocus={() => !user && requireAuth(() => {})}
                placeholder="Hoy logré..."
                className="flex-1 min-h-[90px] md:min-h-0 w-full resize-none border-none outline-none text-sm leading-8 rounded-xl px-3 py-2 placeholder:text-[rgba(100,153,89,0.7)]"
                style={{ backgroundColor: successBg, color: cardText }}
              />
            </div>

            <div className="flex-1 min-h-0 flex flex-col">
              <p className="text-[10px] font-bold uppercase tracking-widest mb-1 flex-shrink-0 lg:text-[11px]" style={{ color: isDark ? '#F2C57C' : '#a07830' }}>
                ☀️ Lo que quiero para mañana
              </p>
              <textarea
                value={displayManana}
                onChange={(e) => isNewEntry && setManana(e.target.value)}
                readOnly={!isNewEntry || !user}
                onFocus={() => !user && requireAuth(() => {})}
                placeholder="Mañana quiero..."
                className="flex-1 min-h-[90px] md:min-h-0 w-full resize-none border-none outline-none text-sm leading-8 rounded-xl px-3 py-2 placeholder:text-[rgba(160,120,48,0.6)]"
                style={{ backgroundColor: warningBg, color: cardText }}
              />
            </div>
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
                  : 'w-2.5 h-2.5 bg-[var(--text-muted)] hover:brightness-110'
              }`}
            />
          ))}
        </div>
      )}
    </div>
    </>
  )
}
