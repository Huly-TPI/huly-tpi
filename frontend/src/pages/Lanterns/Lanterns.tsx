import { useState, useCallback, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

import background from '../../assets/lanterns/ligth-theme/background-lanterns.webp'
import darkBackground from '../../assets/lanterns/dark-theme/background/night-background.webp'
import lanternImage from '../../assets/lanterns/ligth-theme/Lantern-Ligth.webp'
import darkLanternImage from '../../assets/lanterns/dark-theme/lantern-dark.webp'
import paperImage from '../../assets/lanterns/paper.webp'

import { useTheme } from '../../context/theme'
import { useAuthGate } from '../../context/authGate'
import { lanternsApi } from '../../api/lanterns'
import { ActivityType } from '../../api/activities'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'
import Button from '../../components/Buttons/Button/Button'
import BackButton from '../../components/Buttons/BackButton/BackButton'
import './Lanterns.css'

interface Lantern {
  id: number
  text: string
  workedOn: boolean
}

interface RecommendationResponse {
  activity_type: string
  action_id: string
  title: string
  description: string
  redirect_url: string
}

const MAX_LANTERNS = 5
const MAX_TEXT_LENGTH = 20

const BG_POSITIONS = [
  { top: '5%', left: '60%', size: 'w-[14%]', floatClass: 'lantern-float-1' },
  { top: '2%', left: '78%', size: 'w-[12%]', floatClass: 'lantern-float-2' },
  { top: '35%', left: '72%', size: 'w-[13%]', floatClass: 'lantern-float-3' },
  { top: '47%', left: '85%', size: 'w-[10%]', floatClass: 'lantern-float-4' },
] as const

const ACTIVITY_LABELS: Record<string, string> = {
  diary: 'al diario',
  lanterns: 'a los faroles',
  breathing: 'a respiración guiada',
  bubbles: 'a las burbujas',
  challenge: 'al reto diario',
  zen_garden: 'al jardín zen',
  mandala: 'a los mandalas',
  stones: 'a las piedras del lago',
  pending: 'a tus pendientes',
}

export default function LanternsActivity() {
  const navigate = useNavigate()
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const { requireAuth } = useAuthGate()

  const { startSession, markConditionMet, saveSession, stopSession } = useActivitySessionTracker(
    ActivityType.LANTERN,
    { autoStart: true }
  )

  const [lanterns, setLanterns] = useState<Lantern[]>([])
  const [inputText, setInputText] = useState('')
  const [animatingId, setAnimatingId] = useState<number | null>(null)
  const [selectedIndex, setSelectedIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [recommendation, setRecommendation] = useState<RecommendationResponse | null>(null)

  const currentLanternImage = isDark ? darkLanternImage : lanternImage

  useEffect(() => {
    lanternsApi.list()
      .then(thoughts => {
        const mapped = thoughts.slice(0, MAX_LANTERNS)
        setLanterns(mapped)
        setSelectedIndex(0)
      })
      .catch(() => {})
  }, [])

  const selectedLantern = lanterns[selectedIndex] ?? null

  const goToPrev = useCallback(() => {
    setSelectedIndex(prev => (prev > 0 ? prev - 1 : lanterns.length - 1))
  }, [lanterns.length])

  const goToNext = useCallback(() => {
    setSelectedIndex(prev => (prev < lanterns.length - 1 ? prev + 1 : 0))
  }, [lanterns.length])

  const handleRelease = useCallback(() => {
    const trimmed = inputText.trim()
    if (!trimmed || lanterns.length >= MAX_LANTERNS) return
    requireAuth(async () => {
      setInputText('')
      try {
        const created = await lanternsApi.create(trimmed)
        setAnimatingId(created.id)
        setLanterns(prev => [created, ...prev].slice(0, MAX_LANTERNS))
        setSelectedIndex(0)
        markConditionMet()
        setTimeout(() => setAnimatingId(null), 800)
      } catch {
        setInputText(trimmed)
      }
    })
  }, [inputText, lanterns.length, requireAuth, markConditionMet])

  const removeLantern = useCallback((idToRemove: number) => {
    setLanterns(prev => prev.filter(l => l.id !== idToRemove))
    setSelectedIndex(prev => {
      const newLength = lanterns.length - 1
      if (newLength <= 0) return 0
      return prev >= newLength ? newLength - 1 : prev
    })
  }, [lanterns.length])

  const handleLiberar = useCallback(() => {
    if (!selectedLantern) return
    const id = selectedLantern.id
    removeLantern(id)
    markConditionMet()
    lanternsApi.updateStatus(id, 'CANCELLED').catch(() => {})
  }, [selectedLantern, removeLantern, markConditionMet])

  const handleCompletar = useCallback(() => {
    if (!selectedLantern) return
    const id = selectedLantern.id
    removeLantern(id)
    markConditionMet()
    void saveSession()
    stopSession()
    startSession()
    lanternsApi.updateStatus(id, 'COMPLETED').catch(() => {})
  }, [selectedLantern, removeLantern, markConditionMet, saveSession, stopSession, startSession])

  const handleTrabajar = useCallback(async () => {
    if (!selectedLantern) return
    setLoading(true)
    try {
      // Simular retraso de procesamiento para la demo de tesis (0.5s)
      await new Promise(resolve => setTimeout(resolve, 500))

      const rec: RecommendationResponse = {
        activity_type: 'breathing',
        action_id: '1',
        title: 'Sesión de Respiración Guiada',
        description: 'Basado en tu farolito, notamos cierta sobrecarga de pensamientos. Te recomendamos hacer una pausa y realizar un ejercicio de respiración guiada para volver al presente.',
        redirect_url: '/guided-breathing',
      }

      setRecommendation(rec)
      if (!selectedLantern.workedOn) {
        lanternsApi.markWorkedOn(selectedLantern.id).catch(() => {})
        setLanterns(prev => prev.map(l => l.id === selectedLantern.id ? { ...l, workedOn: true } : l))
      }
    } catch (error) {
      console.error('Error al obtener recomendación:', error)
    } finally {
      setLoading(false)
    }
  }, [selectedLantern])

  const handleNavigate = useCallback(() => {
    if (!recommendation) return
    navigate(recommendation.redirect_url)
    setRecommendation(null)
  }, [navigate, recommendation])

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleRelease()
    }
  }

  const bgLanterns = lanterns.filter((_, i) => i !== selectedIndex)

  return (
    <div
      className={`lanterns-page relative flex h-full flex-col overflow-hidden ${isDark ? 'lanterns-page--dark' : ''}`}
      style={{ backgroundImage: `url(${isDark ? darkBackground : background})` }}
    >
      <BackButton to="/" onBeforeNavigate={saveSession} />

      {/* Header */}
      <div className="relative z-10 flex flex-col gap-2 px-4 pt-12 pb-2 md:gap-2 md:p-8">
        <header className="lantern-header text-center md:text-left md:pl-24">
          <h1 className="lantern-title">Farolitos que vuelan</h1>
          <p className="lantern-subtitle">
            Escribe tus pensamientos y déjalos ir al cielo.
          </p>
        </header>
      </div>

      {/* ── Mobile: carrusel de un farol ── */}
      {selectedLantern && (
        <div className="lantern-carousel relative z-[25] flex flex-1 items-center justify-center px-4 md:hidden">
          <div className="flex w-full items-center gap-2">
            {lanterns.length > 1 && (
              <button
                type="button"
                onClick={goToPrev}
                className="lantern-carousel-arrow"
                aria-label="Farol anterior"
              >
                ‹
              </button>
            )}

            <div
              className={`flex flex-1 flex-col items-center ${
                animatingId === selectedLantern.id ? 'lantern-main-enter-mobile' : 'lantern-main-mobile'
              }`}
            >
              <div className="relative w-full max-w-[320px]">
                <img
                  src={currentLanternImage}
                  alt="Farolito con tu pensamiento"
                  className={`h-auto w-full ${!isDark ? 'lantern-day' : ''}`}
                  draggable={false}
                />
                <div className="lantern-note absolute left-[40%] right-[32%] top-[18%] bottom-[42%] flex flex-col items-center justify-center">
                  <p className="overflow-hidden text-center font-nunito text-sm font-semibold leading-tight line-clamp-3">
                    {selectedLantern.text}
                  </p>
                </div>
              </div>

              {/* Dots */}
              {lanterns.length > 1 && (
                <div className="mt-1.5 flex gap-1.5">
                  {lanterns.map((l, i) => (
                    <button
                      key={l.id}
                      type="button"
                      onClick={() => setSelectedIndex(i)}
                      className={`lantern-dot ${i === selectedIndex ? 'lantern-dot--active' : ''}`}
                      aria-label={`Farol ${i + 1}`}
                    />
                  ))}
                </div>
              )}

              {/* Acciones */}
              <div className="mt-3 flex gap-3">
                <button type="button" onClick={handleLiberar} className="lantern-action-btn lantern-action-btn--soltar">
                  Liberar
                </button>
                <button type="button" onClick={handleTrabajar} className="lantern-action-btn lantern-action-btn--trabajar">
                  Trabajar
                </button>
                {selectedLantern.workedOn && (
                  <button type="button" onClick={handleCompletar} className="lantern-action-btn lantern-action-btn--completar">
                    Completar
                  </button>
                )}
              </div>
            </div>

            {lanterns.length > 1 && (
              <button
                type="button"
                onClick={goToNext}
                className="lantern-carousel-arrow"
                aria-label="Farol siguiente"
              >
                ›
              </button>
            )}
          </div>
        </div>
      )}

    
      {/* ── Desktop: layout original ── */}
      {selectedLantern && (
        <div
          className={`absolute z-[25] hidden w-[30%] -translate-x-1/2 flex-col items-center transition-all duration-700 ease-in-out md:flex ${
            animatingId === selectedLantern.id ? 'lantern-main-enter' : 'lantern-main'
          }`}
          style={{ top: '20%', left: '55%' }}
        >
          <div className="relative w-full">
            <img
              src={currentLanternImage}
              alt="Farolito con tu pensamiento"
              className={`h-auto w-full ${!isDark ? 'lantern-day' : ''}`}
              draggable={false}
            />
            <div className="lantern-note absolute left-[40%] right-[32%] top-[18%] bottom-[42%] flex flex-col items-center justify-center">
              <p className="overflow-hidden text-center font-nunito text-[14px] font-semibold leading-tight line-clamp-3">
                {selectedLantern.text}
              </p>
            </div>
          </div>

          <div className="mt-2 flex gap-3">
            <button type="button" onClick={handleLiberar} className="lantern-action-btn lantern-action-btn--soltar">
              Liberar
            </button>
            <button type="button" onClick={handleTrabajar} className="lantern-action-btn lantern-action-btn--trabajar">
              Trabajar
            </button>
            {selectedLantern.workedOn && (
              <button type="button" onClick={handleCompletar} className="lantern-action-btn lantern-action-btn--completar">
                Completar
              </button>
            )}
          </div>
        </div>
      )}

      {/* Faroles de fondo — solo desktop */}
      {bgLanterns.map((lantern, index) => {
        const bgPos = BG_POSITIONS[index]
        if (!bgPos) return null

        return (
          <button
            key={lantern.id}
            type="button"
            onClick={() => setSelectedIndex(lanterns.findIndex(l => l.id === lantern.id))}
            className={`lantern-item absolute z-[15] hidden border-0 bg-transparent p-0 md:block ${bgPos.size} ${bgPos.floatClass}`}
            style={{ top: bgPos.top, left: bgPos.left }}
          >
            <div className="relative">
              <img
                src={currentLanternImage}
                alt=""
                className={`h-auto w-full opacity-80 transition-opacity hover:opacity-100 ${!isDark ? 'lantern-day' : ''}`}
                draggable={false}
              />
              <div className="lantern-note absolute left-[40%] right-[32%] top-[18%] bottom-[42%] flex flex-col items-center justify-center">
                <p className="text-center font-nunito text-[6px] font-semibold leading-tight overflow-hidden line-clamp-3">
                  {lantern.text}
                </p>
              </div>
            </div>
          </button>
        )
      })}

      {/* Loading overlay */}
      {loading && (
        <div className="absolute inset-0 z-30 flex items-center justify-center bg-white/60 dark:bg-black/45">
          <p className="text-lg font-semibold text-[#8869AC] dark:text-slate-100 md:text-xl">Analizando tu pensamiento...</p>
        </div>
      )}

      {/* Modal de recomendación */}
      {recommendation && !loading && (
        <div className="absolute inset-0 z-30 flex items-center justify-center bg-black/[0.15] dark:bg-black/45">
          <div className="mx-4 w-full max-w-sm rounded-2xl bg-white dark:bg-[#172033] dark:border dark:border-slate-800/50 p-5 text-center shadow-xl sm:p-8">
            <h2 className="mb-3 text-xl font-bold text-[#8869AC] dark:text-slate-100 md:text-2xl">
              {recommendation.title}
            </h2>
            <p className="mb-5 text-sm font-medium leading-relaxed text-gray-600 dark:text-slate-400 md:mb-6 md:text-base">
              {recommendation.description}
            </p>
            <Button variant="primary" fullWidth onClick={handleNavigate}>
              Ir {ACTIVITY_LABELS[recommendation.activity_type] ?? 'a la actividad'}
            </Button>
            <Button
              variant="secondary"
              fullWidth
              onClick={() => setRecommendation(null)}
              className="mt-3"
            >
              Quedarme aquí
            </Button>
          </div>
        </div>
      )}

      {/* Papel inferior */}
      <div className="paper-wrapper relative z-20 mt-auto flex justify-start md:justify-center px-3 md:px-4">
        <div className="paper-panel">
          <img src={paperImage} alt="" className="paper-bg-img" draggable={false} />

          <div className="paper-input-row">
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder={lanterns.length >= MAX_LANTERNS ? 'Soltá o completá uno primero' : '¿Qué te da vueltas?'}
              maxLength={MAX_TEXT_LENGTH}
              className="paper-input"
              disabled={lanterns.length >= MAX_LANTERNS}
            />
            <button
              type="button"
              onClick={handleRelease}
              disabled={!inputText.trim() || lanterns.length >= MAX_LANTERNS}
              className="paper-button"
            >
              Soltar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}