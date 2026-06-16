import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'

import background from '../../assets/lanterns/ligth-theme/background-lanterns.webp'
import darkBackground from '../../assets/lanterns/dark-theme/background/night-background.webp'
import lanternImage from '../../assets/lanterns/ligth-theme/Lantern-Ligth.webp'
import darkLanternImage from '../../assets/lanterns/dark-theme/Lantern-Dark.webp'
import paperImage from '../../assets/lanterns/paper.webp'

import { useTheme } from '../../context/theme'
import { api } from '../../api/client'
import Button from '../../components/Buttons/Button/Button'
import './Lanterns.css'

interface Lantern {
  id: string
  text: string
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
  { top: '5%', left: '60%', size: 'w-[22%] md:w-[14%]', floatClass: 'lantern-float-1' },
  { top: '2%', left: '78%', size: 'w-[18%] md:w-[12%]', floatClass: 'lantern-float-2' },
  { top: '35%', left: '72%', size: 'w-[20%] md:w-[13%]', floatClass: 'lantern-float-3' },
  { top: '47%', left: '85%', size: 'w-[16%] md:w-[10%]', floatClass: 'lantern-float-4' },
] as const

const ACTIVITY_LABELS: Record<string, string> = {
  diary: 'al diario',
  clouds: 'a las nubes',
  breathing: 'a respiración guiada',
  bubbles: 'a las burbujas',
}

export default function LanternsActivity() {
  const navigate = useNavigate()
  const { theme } = useTheme()
  const isDark = theme === 'dark'

  const [lanterns, setLanterns] = useState<Lantern[]>([])
  const [inputText, setInputText] = useState('')
  const [animatingId, setAnimatingId] = useState<string | null>(null)
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [recommendation, setRecommendation] = useState<RecommendationResponse | null>(null)

  const currentLanternImage = isDark ? darkLanternImage : lanternImage

  const handleRelease = useCallback(() => {
    const trimmed = inputText.trim()
    if (!trimmed) return

    const newLantern: Lantern = {
      id: crypto.randomUUID(),
      text: trimmed,
    }

    setAnimatingId(newLantern.id)
    setSelectedId(newLantern.id)
    setLanterns(prev => [newLantern, ...prev].slice(0, MAX_LANTERNS))
    setInputText('')

    setTimeout(() => setAnimatingId(null), 800)
  }, [inputText])

  const handleSoltar = useCallback(() => {
    setLanterns(prev => {
      const next = prev.filter(l => l.id !== selectedId)
      setSelectedId(next[0]?.id ?? null)
      return next
    })
  }, [selectedId])

  const handleTrabajar = useCallback(async () => {
    const lantern = lanterns.find(l => l.id === selectedId)
    if (!lantern) return
    setLoading(true)
    try {
      const rec = await api.post<RecommendationResponse>('/clouds/recommendation', { thoughts: [lantern.text] })
      setRecommendation(rec)
    } catch (error) {
      console.error('Error al obtener recomendación:', error)
    } finally {
      setLoading(false)
    }
  }, [lanterns, selectedId])

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

  const selectedLantern = lanterns.find(l => l.id === selectedId)
  const bgLanterns = lanterns.filter(l => l.id !== selectedId)

  return (
    <div
      className={`lanterns-page relative flex h-full flex-col overflow-hidden ${isDark ? 'lanterns-page--dark' : ''}`}
      style={{ backgroundImage: `url(${isDark ? darkBackground : background})` }}
    >
      {/* Header */}
      <div className="relative z-10 flex flex-col gap-2 p-4 md:p-8">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex w-fit items-center gap-1 rounded-full bg-white/70 px-4 py-1.5 text-sm font-semibold text-[#4C7C64] backdrop-blur-sm transition-colors hover:bg-white/90"
        >
          ← Volver
        </button>

        <header className="lantern-header">
          <h1 className="lantern-title">Farolitos que vuelan</h1>
          <p className="lantern-subtitle">
            Escribe tus pensamientos y déjalos ir al cielo.
          </p>
        </header>
      </div>

      {/* Farol seleccionado con botones abajo */}
      {selectedLantern && (
        <div
          className={`absolute z-[25] flex w-[55%] -translate-x-1/2 flex-col items-center md:w-[30%] transition-all duration-700 ease-in-out ${
            animatingId === selectedLantern.id ? 'lantern-main-enter' : 'lantern-main'
          }`}
          style={{ top: '20%', left: '45%' }}
        >
          <div className="relative w-full">
            <img
              src={currentLanternImage}
              alt="Farolito con tu pensamiento"
              className={`h-auto w-full ${!isDark ? 'lantern-day' : ''}`}
              draggable={false}
            />
            <div className="lantern-note absolute left-[40%] right-[32%] top-[18%] bottom-[42%] flex flex-col items-center justify-center">
              <p className="text-center font-nunito text-[12px] font-semibold leading-tight md:text-[14px] overflow-hidden line-clamp-3">
                {selectedLantern.text}
              </p>
            </div>
          </div>

          <div className="mt-2 flex gap-3">
            <button
              type="button"
              onClick={handleSoltar}
              className="lantern-action-btn lantern-action-btn--soltar"
            >
              Soltar
            </button>
            <button
              type="button"
              onClick={handleTrabajar}
              className="lantern-action-btn lantern-action-btn--trabajar"
            >
              Trabajar
            </button>
          </div>
        </div>
      )}

      {/* Faroles de fondo — clickeables para traer al frente */}
      {bgLanterns.map((lantern, index) => {
        const bgPos = BG_POSITIONS[index]
        if (!bgPos) return null

        return (
          <button
            key={lantern.id}
            type="button"
            onClick={() => setSelectedId(lantern.id)}
            className={`lantern-item absolute z-[15] border-0 bg-transparent p-0 ${bgPos.size} ${bgPos.floatClass}`}
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
        <div className="absolute inset-0 z-30 flex items-center justify-center bg-white/60 backdrop-blur-sm">
          <p className="text-xl font-semibold text-[#8869AC]">Analizando tu pensamiento...</p>
        </div>
      )}

      {/* Modal de recomendación */}
      {recommendation && !loading && (
        <div className="absolute inset-0 z-30 flex items-center justify-center bg-white/70 backdrop-blur-sm">
          <div className="mx-4 w-full max-w-sm rounded-2xl bg-white p-6 text-center shadow-xl sm:p-8">
            <h2 className="mb-3 text-2xl font-bold text-[#8869AC]">
              {recommendation.title}
            </h2>
            <p className="mb-6 leading-relaxed text-gray-500">
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
      <div className="relative z-20 mt-auto flex justify-center px-4 pb-0 mb-[-110px]">
        <div className="paper-panel">
          <img src={paperImage} alt="" className="paper-bg-img" draggable={false} />
          <h3 className="paper-title">
            Escribe un pensamiento y déjalo ir
          </h3>
          <div className="paper-input-row">
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="¿Qué te está dando vueltas en la cabeza ahora?"
              maxLength={MAX_TEXT_LENGTH}
              className="paper-input"
            />
            <button
              type="button"
              onClick={handleRelease}
              disabled={!inputText.trim()}
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
