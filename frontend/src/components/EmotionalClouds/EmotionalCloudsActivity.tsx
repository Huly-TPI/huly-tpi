import {useState, useCallback, useRef} from 'react'
import EmotionalCloud from './EmotionalCloud'
import type { Cloud, EmotionalCloudsProps } from './types'
import './EmotionalClouds.css'

const CLOUD_TOPS = ['20%', '28%', '18%', '38%', '25%', '70%','40%', '32%', '55%','60%']

const EmotionalCloudsActivity = ({ onThoughtSubmit, maxClouds = 10 }: EmotionalCloudsProps) => {
  const [clouds, setClouds] = useState<Cloud[]>([])
  const [input, setInput] = useState('')
  const cloudIdRef = useRef(0)
  const cloudCounterRef = useRef(0)

  const handleAddCloud = useCallback(() => {
    if (input.trim().length === 0) return

    const idx = cloudCounterRef.current % CLOUD_TOPS.length
    cloudCounterRef.current++

    const newCloud: Cloud = {
      id: `cloud-${cloudIdRef.current++}`,
      text: input.trim(),
      top: CLOUD_TOPS[idx],
      duration: '20s',
    }

    setClouds(prev => {
      const updated = [...prev, newCloud]
      return updated.slice(-maxClouds)
    })

    onThoughtSubmit?.(input.trim())
    setInput('')
  }, [input, onThoughtSubmit, maxClouds])

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
        <header className="w-full text-center flex-shrink-0 animate-fade-in pt-5 px-4">
          <h1 className="font-sans text-3xl md:text-4xl font-bold text-[#8869AC] tracking-wide mb-1 drop-shadow-sm">
            Nubes que pasan
          </h1>
          <p className="text-[#8869AC] text-sm md:text-base mx-auto leading-relaxed opacity-70">
            Deja ir tus pensamientos como nubes
          </p>
        </header>
        {clouds.map(cloud => (
          <EmotionalCloud
            key={cloud.id}
            cloud={cloud}
            onRemove={() => handleRemoveCloud(cloud.id)}
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

