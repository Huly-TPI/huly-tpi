import { useEffect, useRef, useState } from 'react'

export interface LanternData {
    id: string
    text: string
    /** horizontal starting position as a css value, e.g. "30%" */
    left: string
    /** rise duration in seconds */
    duration: number
    /** visual size variant for variety */
    size: 'sm' | 'md' | 'lg'
}

interface LanternProps {
    lantern: LanternData
    onRemove: () => void
    onSelect?: () => void
}

const SIZE_PX: Record<LanternData['size'], number> = {
    sm: 85,
    md: 110,
    lg: 135,
}

/** Inline SVG lantern — warm amber sky-lantern shape */
const LanternSVG = ({ size }: { size: number }) => (
    <svg
        className="lantern-svg"
        style={{ '--lantern-size': `${size}px` } as React.CSSProperties}
        width={size}
        height={size * 1.45}
        viewBox="0 0 110 160"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
    >
        {/* outer glow */}
        <ellipse cx="55" cy="72" rx="44" ry="56" fill="rgba(255,180,30,0.18)" />

        {/* main body */}
        <path
            d="M22 55 Q18 100 30 125 Q42 148 55 150 Q68 148 80 125 Q92 100 88 55 Q78 30 55 28 Q32 30 22 55Z"
            fill="url(#bodyGrad)"
            stroke="#c87820"
            strokeWidth="1.2"
        />

        {/* paper texture highlight */}
        <path
            d="M35 48 Q30 75 38 105 Q44 125 55 132"
            stroke="rgba(255,255,200,0.45)"
            strokeWidth="6"
            strokeLinecap="round"
            fill="none"
        />

        {/* top rim */}
        <ellipse cx="55" cy="36" rx="28" ry="8" fill="#d4861a" stroke="#a85e0a" strokeWidth="1" />

        {/* bottom ring / opening */}
        <ellipse cx="55" cy="147" rx="20" ry="5.5" fill="#c87820" stroke="#a85e0a" strokeWidth="1" />

        {/* vertical ribs */}
        {[-18, -8, 0, 8, 18].map((offset, i) => (
            <path
                key={i}
                d={`M${55 + offset} 44 Q${55 + offset * 1.15} 95 ${55 + offset * 0.8} 142`}
                stroke="rgba(120,60,0,0.18)"
                strokeWidth="0.8"
                fill="none"
            />
        ))}

        {/* decorative flower */}
        <g transform="translate(55,115)">
            {[0, 60, 120, 180, 240, 300].map((deg, i) => (
                <ellipse
                    key={i}
                    cx={Math.cos((deg * Math.PI) / 180) * 5.5}
                    cy={Math.sin((deg * Math.PI) / 180) * 5.5}
                    rx="3.5"
                    ry="2.2"
                    transform={`rotate(${deg})`}
                    fill="#c86020"
                    opacity="0.55"
                />
            ))}
            <circle cx="0" cy="0" r="2.5" fill="#ff9933" opacity="0.8" />
        </g>

        {/* inner glow / flame light */}
        <ellipse
            className="lantern-flame-glow"
            cx="55"
            cy="110"
            rx="28"
            ry="32"
            fill="url(#innerGlow)"
        />

        {/* flame at bottom opening */}
        <g transform="translate(55,148)" className="lantern-flame-glow">
            <ellipse cx="0" cy="-3" rx="6" ry="9" fill="url(#flameGrad)" />
            <ellipse cx="0" cy="-6" rx="3" ry="5" fill="#fff7a0" opacity="0.8" />
        </g>

        <defs>
            <radialGradient id="bodyGrad" cx="40%" cy="35%" r="70%">
                <stop offset="0%" stopColor="#ffd87a" />
                <stop offset="45%" stopColor="#f0a030" />
                <stop offset="100%" stopColor="#c06010" />
            </radialGradient>
            <radialGradient id="innerGlow" cx="50%" cy="60%" r="55%">
                <stop offset="0%" stopColor="#ffe066" stopOpacity="0.7" />
                <stop offset="100%" stopColor="#ff8800" stopOpacity="0" />
            </radialGradient>
            <linearGradient id="flameGrad" x1="0%" y1="100%" x2="0%" y2="0%">
                <stop offset="0%" stopColor="#ff6600" />
                <stop offset="50%" stopColor="#ffcc00" />
                <stop offset="100%" stopColor="#fff7a0" />
            </linearGradient>
        </defs>
    </svg>
)

const Lantern = ({ lantern, onRemove, onSelect }: LanternProps) => {
    const [releasing, setReleasing] = useState(false)
    const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
    const sizePx = SIZE_PX[lantern.size]

    useEffect(() => {
        timerRef.current = setTimeout(onRemove, lantern.duration * 1000)
        return () => { if (timerRef.current) clearTimeout(timerRef.current) }
    }, [lantern.duration, onRemove])

    const handleClick = () => {
        if (releasing) return
        setReleasing(true)
        if (timerRef.current) clearTimeout(timerRef.current)
        timerRef.current = setTimeout(onRemove, 2200)
        onSelect?.()
    }

    return (
        <div
            className={`lantern-item${releasing ? ' lantern-item--releasing' : ''}`}
            style={{
                left: lantern.left,
                '--rise-duration': `${lantern.duration}s`,
                '--lantern-size': `${sizePx}px`,
            } as React.CSSProperties}
            onClick={handleClick}
            role="button"
            aria-label={`Pensamiento: ${lantern.text}. Toca para soltar.`}
            data-testid={`lantern-${lantern.id}`}
        >
            <LanternSVG size={sizePx} />
            <div className="lantern-text-wrapper">
                <p className="lantern-text">{lantern.text}</p>
            </div>
        </div>
    )
}

export default Lantern
