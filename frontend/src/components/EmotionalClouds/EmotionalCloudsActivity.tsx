import {useState, useCallback, useRef} from 'react'
import EmotionalCloud from './EmotionalCloud'
import type { Cloud, EmotionalCloudsProps } from './types'
import './EmotionalClouds.css'

const CLOUD_TOPS = ['20%', '28%', '18%', '38%', '25%', '70%','40%', '32%', '55%','60%']

const Seagull = ({ style }: { style: React.CSSProperties }) => (
    <svg
        className="sky-seagull"
        style={style}
        viewBox="0 0 40 14"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
    >
      <path d="M0 7 Q10 0 20 7 Q30 0 40 7" stroke="#7a9ab8" strokeWidth="1.8" strokeLinecap="round" fill="none" />
    </svg>
)

const Sun = () => (
    <svg
        className="sky-sun"
        viewBox="0 0 80 80"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
    >
      {Array.from({ length: 8 }).map((_, i) => {
        const angle = (i * 45 * Math.PI) / 180
        const x1 = 40 + Math.cos(angle) * 26
        const y1 = 40 + Math.sin(angle) * 26
        const x2 = 40 + Math.cos(angle) * 36
        const y2 = 40 + Math.sin(angle) * 36
        return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#f9c12e" strokeWidth="2.5" strokeLinecap="round" />
      })}
      <circle cx="40" cy="40" r="22" fill="#fde68a" opacity="0.35" />
      <circle cx="40" cy="40" r="17" fill="url(#sunGrad)" />
      <defs>
        <radialGradient id="sunGrad" cx="38%" cy="35%" r="60%">
          <stop offset="0%" stopColor="#fff176" />
          <stop offset="60%" stopColor="#f9c12e" />
          <stop offset="100%" stopColor="#e8a510" />
        </radialGradient>
      </defs>
    </svg>
)

const SEAGULLS = [
  { top: '12%', left: '15%', width: 36},
  { top: '18%', left: '22%', width: 24},
  { top: '8%',  left: '30%', width: 18},
  { top: '22%', left: '55%', width: 28},
  { top: '14%', left: '62%', width: 20},
]

const EmotionalCloudsActivity = ({ onThoughtAdded, onFinish, maxClouds = 10 }: EmotionalCloudsProps) => {
  const [clouds, setClouds] = useState<Cloud[]>([])
  const [input, setInput] = useState('')
  const cloudIdRef = useRef(0)
  const cloudCounterRef = useRef(0)
  const allThoughtsRef = useRef<string[]>([])

  const handleAddCloud = useCallback(() => {
    if (input.trim().length === 0) return

    const text = input.trim()
    allThoughtsRef.current = [...allThoughtsRef.current, text]
    onThoughtAdded?.(text)

    const idx = cloudCounterRef.current % CLOUD_TOPS.length
    cloudCounterRef.current++

    const newCloud: Cloud = {
      id: `cloud-${cloudIdRef.current++}`,
      text,
      top: CLOUD_TOPS[idx],
      duration: '20s',
    }

    setClouds(prev => {
      const updated = [...prev, newCloud]
      return updated.slice(-maxClouds)
    })

    setInput('')
  }, [input, maxClouds])

  const handleRemoveCloud = useCallback((cloudId: string) => {
    setClouds(prev => prev.filter(c => c.id !== cloudId))
  }, [])

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleAddCloud()
    }
  }

  return (
    <div className="emotional-clouds-activity">
      <div className="emotional-clouds-container">
        <Sun />

        {SEAGULLS.map((s, i) => (
            <Seagull
                key={i}
                style={{
                  position: 'absolute',
                  top: s.top,
                  left: s.left,
                  width: s.width,
                } as React.CSSProperties}
            />
        ))}

        <header className="w-full text-center flex-shrink-0 animate-fade-in pt-5 px-4">
          <h1 className="font-sans text-3xl md:text-4xl font-bold text-[#8869AC] tracking-wide mb-1 drop-shadow-sm">
            Nubes que pasan
          </h1>
          <p className="text-[#8869AC] text-2xl mx-auto leading-relaxed opacity-70">
            Deja ir tus pensamientos como nubes
          </p>
        </header>
        {clouds.map(cloud => (
          <EmotionalCloud
            key={cloud.id}
            cloud={cloud}
            onRemove={() => handleRemoveCloud(cloud.id)}
            onSelect={() => onFinish?.([cloud.text])}
          />
        ))}
      </div>

      <div className="emotional-clouds-input-section">
        <label htmlFor="emotional-input" className="emotional-clouds-label">
          Escribe un pensamiento y dejalo ir
        </label>
        <div className="emotional-clouds-input-wrapper">
          <input
            id="emotional-input"
            type="text"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="¿Que te está dando vueltas en la cabeza ahora?"
            className="emotional-clouds-input"
            maxLength={100}
            aria-label="Ingresa un pensamiento o sentimiento"
            data-testid="emotional-input"
          />
          <button
            onClick={handleAddCloud}
            disabled={input.trim().length === 0}
            className="emotional-clouds-button"
            type="button"
            data-testid="submit-button"
          >
            Soltar
          </button>
        </div>
        <div className="emotional-clouds-counter" data-testid="char-counter">
          {input.length}/100
        </div>
      </div>
    </div>
  )
}

export default EmotionalCloudsActivity

