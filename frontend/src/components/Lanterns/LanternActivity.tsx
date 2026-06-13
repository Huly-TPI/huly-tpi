import { useCallback, useRef, useState } from 'react'
import Lantern, { type LanternData } from './Lantern'
import './LanternActivity.css'

/* ─────────────────────────────────────────────
   Props — same contract as EmotionalCloudsActivity
   so the parent CloudsActivity wrapper can swap it in
───────────────────────────────────────────── */
export interface LanternActivityProps {
    onThoughtAdded?: (thought: string) => void
    onFinish?: (thoughts: string[]) => void
    maxLanterns?: number
}

/* ── Horizontal starting positions (varied so lanterns don't stack) ── */
const LEFT_POSITIONS = [
    '8%', '18%', '30%', '42%', '55%', '65%', '74%', '82%', '90%', '12%',
]

const SIZES: LanternData['size'][] = ['sm', 'md', 'lg', 'md', 'sm', 'lg', 'md', 'sm', 'lg', 'md']
const DURATIONS = [22, 18, 26, 20, 24, 19, 28, 21, 25, 17]

/* ── Sky decorations ── */

const Sun = () => (
    <svg className="lantern-sun" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
        {Array.from({ length: 8 }).map((_, i) => {
            const angle = (i * 45 * Math.PI) / 180
            return (
                <line key={i}
                    x1={40 + Math.cos(angle) * 26} y1={40 + Math.sin(angle) * 26}
                    x2={40 + Math.cos(angle) * 36} y2={40 + Math.sin(angle) * 36}
                    stroke="#f9c12e" strokeWidth="2.5" strokeLinecap="round"
                />
            )
        })}
        <circle cx="40" cy="40" r="22" fill="#fde68a" opacity="0.35" />
        <circle cx="40" cy="40" r="17" fill="url(#sunGrad)" />
        <defs>
            <radialGradient id="sunGrad" cx="38%" cy="35%" r="60%">
                <stop offset="0%"   stopColor="#fff176" />
                <stop offset="60%"  stopColor="#f9c12e" />
                <stop offset="100%" stopColor="#e8a510" />
            </radialGradient>
        </defs>
    </svg>
)

interface CloudBlobProps {
    x: number; y: string; w: number; speed?: 'slow' | 'fast' | ''
}
const CloudBlob = ({ x, y, w, speed = '' }: CloudBlobProps) => (
    <svg
        className={`lantern-cloud ${speed ? `lantern-cloud--${speed}` : ''}`}
        style={{ left: x, top: y, width: w, position: 'absolute' } as React.CSSProperties}
        viewBox="0 0 120 50"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
    >
        <ellipse cx="60" cy="32" rx="52" ry="18" fill="white" opacity="0.88" />
        <ellipse cx="38" cy="26" rx="28" ry="20" fill="white" opacity="0.88" />
        <ellipse cx="78" cy="24" rx="24" ry="17" fill="white" opacity="0.88" />
        <ellipse cx="55" cy="20" rx="20" ry="16" fill="white" opacity="0.9" />
    </svg>
)

const Hills = () => (
    <svg className="lantern-hills" viewBox="0 0 1440 220" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
        {/* back hills — purple */}
        <path d="M0 160 Q120 80 280 130 Q440 170 580 100 Q720 40 880 110 Q1020 170 1160 90 Q1300 30 1440 120 L1440 220 L0 220Z"
            fill="#9b7ec8" opacity="0.55" />
        {/* mid hills — green */}
        <path d="M0 185 Q100 130 240 160 Q380 185 520 145 Q660 110 800 155 Q940 195 1080 145 Q1220 100 1440 165 L1440 220 L0 220Z"
            fill="#5aab5a" opacity="0.75" />
        {/* front foliage strip */}
        <path d="M0 200 Q200 175 400 195 Q600 210 800 188 Q1000 170 1200 198 Q1320 210 1440 195 L1440 220 L0 220Z"
            fill="#3d8f3d" />
        {/* leaf bumps front-left */}
        <ellipse cx="60"  cy="200" rx="55" ry="28" fill="#2e7a2e" />
        <ellipse cx="140" cy="205" rx="40" ry="22" fill="#3d8f3d" />
        <ellipse cx="220" cy="198" rx="50" ry="25" fill="#2e7a2e" />
        {/* leaf bumps front-right */}
        <ellipse cx="1280" cy="200" rx="55" ry="28" fill="#2e7a2e" />
        <ellipse cx="1360" cy="205" rx="45" ry="22" fill="#3d8f3d" />
        <ellipse cx="1420" cy="198" rx="40" ry="24" fill="#2e7a2e" />
    </svg>
)

/* ─────────────────────────────────────────────
   Main component
───────────────────────────────────────────── */
const LanternActivity = ({ onThoughtAdded, onFinish, maxLanterns = 10 }: LanternActivityProps) => {
    const [lanterns, setLanterns] = useState<LanternData[]>([])
    const [input, setInput] = useState('')
    const idRef = useRef(0)
    const counterRef = useRef(0)

    const handleAdd = useCallback(() => {
        const text = input.trim()
        if (!text) return

        onThoughtAdded?.(text)

        const idx = counterRef.current % LEFT_POSITIONS.length
        counterRef.current++

        const newLantern: LanternData = {
            id: `lantern-${idRef.current++}`,
            text,
            left: LEFT_POSITIONS[idx],
            duration: DURATIONS[idx],
            size: SIZES[idx],
        }

        setLanterns(prev => [...prev, newLantern].slice(-maxLanterns))
        setInput('')
    }, [input, maxLanterns, onThoughtAdded])

    const handleRemove = useCallback((id: string) => {
        setLanterns(prev => prev.filter(l => l.id !== id))
    }, [])

    const handleSelect = useCallback((text: string) => {
        onFinish?.([text])
    }, [onFinish])

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault()
            handleAdd()
        }
    }

    return (
        <div className="lantern-activity">

            {/* ── sky decorations ── */}
            <Sun />
            <CloudBlob x={60}  y="14%" w={110} speed="slow" />
            <CloudBlob x={260} y="22%" w={85}  speed="" />
            <CloudBlob x={520} y="10%" w={130} speed="slow" />
            <CloudBlob x={780} y="18%" w={95}  speed="fast" />
            <CloudBlob x={1050} y="12%" w={110} />
            <CloudBlob x={1280} y="20%" w={80}  speed="slow" />

            {/* ── header ── */}
            <header className="lantern-header">
                <h1 className="lantern-title">Farolitos que vuelan</h1>
                <p className="lantern-subtitle">Escribe tus pensamientos y déjalos ir al cielo</p>
            </header>

            {/* ── flying lanterns ── */}
            <div className="lantern-sky" aria-live="polite" aria-label="Cielo con farolitos">
                {lanterns.map(l => (
                    <Lantern
                        key={l.id}
                        lantern={l}
                        onRemove={() => handleRemove(l.id)}
                        onSelect={() => handleSelect(l.text)}
                    />
                ))}
            </div>

            {/* ── hills / foreground ── */}
            <Hills />

            {/* ── input bar ── */}
            <div className="lantern-input-section">
                <label htmlFor="lantern-input" className="lantern-input-label">
                    Escribe un pensamiento y déjalo ir ✨
                </label>
                <div className="lantern-input-row">
                    <input
                        id="lantern-input"
                        type="text"
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder="¿Qué te está dando vueltas en la cabeza ahora?"
                        className="lantern-input"
                        maxLength={100}
                        aria-label="Ingresa un pensamiento para soltar en un farolito"
                        data-testid="lantern-input"
                        autoComplete="off"
                    />
                    <button
                        type="button"
                        className="lantern-submit-btn"
                        onClick={handleAdd}
                        disabled={input.trim().length === 0}
                        data-testid="lantern-submit"
                    >
                        Soltar
                    </button>
                </div>
                <div className="lantern-char-counter" aria-live="polite" data-testid="char-counter">
                    {input.length}/100
                </div>
            </div>
        </div>
    )
}

export default LanternActivity
