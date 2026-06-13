import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'

import background from '../../assets/lanterns/ligth-theme/background-lanterns.webp'
import darkBackground from '../../assets/lanterns/dark-theme/background/night-background.webp'
import lanternImage from '../../assets/lanterns/dark-theme/lantern.webp'
import darkLanternImage from '../../assets/lanterns/dark-theme/lantern.webp'
import paperImage from '../../assets/lanterns/paper.webp'
import darkPaperImage from '../../assets/lanterns/paper.webp'

import { useTheme } from '../../context/theme'
import './Lanterns.css'

interface Lantern {
  id: string
  text: string
}

const MAX_LANTERNS = 5
const MAX_TEXT_LENGTH = 120

const BG_POSITIONS = [
  { top: '5%', left: '60%', size: 'w-[22%] md:w-[14%]', floatClass: 'lantern-float-1' },
  { top: '2%', left: '78%', size: 'w-[18%] md:w-[12%]', floatClass: 'lantern-float-2' },
  { top: '35%', left: '72%', size: 'w-[20%] md:w-[13%]', floatClass: 'lantern-float-3' },
  { top: '47%', left: '85%', size: 'w-[16%] md:w-[10%]', floatClass: 'lantern-float-4' },
] as const

export default function LanternsActivity() {
  const navigate = useNavigate()
  const { theme } = useTheme()
  const isDark = theme === 'dark'

  const [lanterns, setLanterns] = useState<Lantern[]>([])
  const [inputText, setInputText] = useState('')
  const [animatingId, setAnimatingId] = useState<string | null>(null)

  const mainLantern = lanterns[0] ?? null
  const bgLanterns = lanterns.slice(1)

  const currentLanternImage = isDark ? darkLanternImage : lanternImage
  const currentPaperImage = isDark ? darkPaperImage : paperImage

  const handleRelease = useCallback(() => {
    const trimmed = inputText.trim()

    if (!trimmed) return

    const newLantern: Lantern = {
      id: crypto.randomUUID(),
      text: trimmed,
    }

    setAnimatingId(newLantern.id)
    setLanterns(prev => [newLantern, ...prev].slice(0, MAX_LANTERNS))
    setInputText('')

    setTimeout(() => setAnimatingId(null), 800)
  }, [inputText])

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleRelease()
    }
  }

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

      {/* Faroles pequeños */}
      {bgLanterns.map((lantern, index) => {
        const pos = BG_POSITIONS[index]
        if (!pos) return null

        return (
          <div
            key={lantern.id}
            className={`lantern-bg absolute ${pos.floatClass} ${pos.size}`}
            style={{
              top: pos.top,
              left: pos.left,
            }}
          >
            <img
              src={currentLanternImage}
              alt=""
              className={`h-auto w-full opacity-80 ${!isDark ? 'lantern-day' : 'lantern-night'
                }`}
              draggable={false}
            />
          </div>
        )
      })}

      {/* Farol principal */}
      {mainLantern && (
        <div
          className={`absolute left-[45%] top-[28%] z-10 w-[55%] -translate-x-1/2 md:w-[30%] ${animatingId === mainLantern.id
            ? 'lantern-main-enter'
            : 'lantern-main'
            }`}
        >
          <div className="relative">
            <img
              src={currentLanternImage}
              alt="Farolito"
              className={`h-auto w-full ${!isDark ? 'lantern-day' : 'lantern-night'
                }`}
              draggable={false}
            />

            <div className="lantern-text absolute inset-0 flex items-center justify-center px-[28%] pb-[22%] pt-[15%]">
              <p className="text-center font-nunito text-[10px] font-semibold leading-tight text-[#5a3e28] md:text-xs overflow-hidden line-clamp-4">
                {mainLantern.text}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Papel inferior */}
      <div className="relative z-20 mt-auto flex justify-center px-4 pb-0 mb-[-110px]">
        <div className="paper-panel">
          <img src={currentPaperImage} alt="" className="paper-bg-img" draggable={false} />
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