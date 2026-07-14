import { useState, useRef } from 'react'
import Bubble from './Bubble'
import type { Bubble as BubbleType } from './types'
import './BubblesActivity.css'
import bgImage from '../../assets/bubbles/background/bubbles-minigame-background.webp'
import fishImage from '../../assets/bubbles/bubble-minigame-fish.webp'
import Button from '../Buttons/Button/Button'
import BackButton from '../Buttons/BackButton/BackButton'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'
import { ActivityType } from '../../api/activities'
import { useBubbleAudio } from '../../hooks/useBubbleAudio'


const BUBBLE_COLORS = [
  'rgba(144, 210, 170, 0.55)',
  'rgba(230, 180, 200, 0.55)',
  'rgba(200, 190, 230, 0.50)',
  'rgba(160, 210, 235, 0.55)',
  'rgba(245, 210, 160, 0.50)',
  'rgba(180, 230, 210, 0.55)',
  'rgba(220, 175, 220, 0.50)',
  'rgba(170, 220, 245, 0.55)',
]

const isMobile = window.innerWidth < 640

const generateBubble = (id: string, delay: number): BubbleType => ({
  id,
  color: BUBBLE_COLORS[Math.floor(Math.random() * BUBBLE_COLORS.length)],
  size: (isMobile ? 50 : 70) + Math.floor(Math.random() * (isMobile ? 50 : 100)),
  driftX: 20 + Math.floor(Math.random() * 75),
  driftY: -50 + Math.floor(Math.random() * 75),
  animationDuration: 20 + Math.random() * 15,
  animationDelay: delay,
})

const INITIAL_BUBBLES: BubbleType[] = Array.from(
  { length: 8 },
  (_, i) => generateBubble(`b-${i}`, i * 4),
)

const BubblesActivity = () => {
  const [started, setStarted] = useState(false)
  const [bubbles, setBubbles] = useState<BubbleType[]>(INITIAL_BUBBLES)
  const [poppingIds, setPoppingIds] = useState<Set<string>>(new Set())
  const [poppingPositions, setPoppingPositions] = useState<Map<string, { x: number; y: number }>>(new Map())
  const nextIdRef = useRef(INITIAL_BUBBLES.length)
  const { playPop } = useBubbleAudio()

  const { startSession, markConditionMet, saveSession } = useActivitySessionTracker(ActivityType.BUBBLE, {
    autoStart: false
  })

  const handleBubbleClick = (bubble: BubbleType, position: { x: number; y: number }) => {
    if (poppingIds.has(bubble.id)) return
    playPop()
    markConditionMet()
    setPoppingPositions(prev => new Map(prev).set(bubble.id, position))
    setPoppingIds(prev => new Set([...prev, bubble.id]))
  }

  const handlePopEnd = (id: string) => {
    setBubbles(prev => prev.filter(b => b.id !== id))
    setPoppingIds(prev => {
      const next = new Set(prev)
      next.delete(id)
      return next
    })
    setPoppingPositions(prev => {
      const next = new Map(prev)
      next.delete(id)
      return next
    })
    setTimeout(() => {
      nextIdRef.current++
      setBubbles(prev => [...prev, generateBubble(`b-respawn-${nextIdRef.current}`, 0)])
    }, 2000)
  }

  if (!started) {
    return (
      <div className="bubbles-activity">
        <div className="bubbles-scene">
          <img src={bgImage} className="bubbles-scene-bg absolute inset-0 w-full h-full object-cover object-bottom" alt="" draggable={false} />
          <div className="absolute inset-0 flex items-center justify-center bg-black/[0.15] dark:bg-black/45">
            <div className="bg-white dark:bg-[#172033]/90 dark:border dark:border-slate-800/50 backdrop-blur-sm rounded-2xl p-10 shadow-md w-[480px] max-w-[90vw] text-center">
              <h2 className="text-3xl font-bold text-gray-800 dark:text-slate-100 mb-3">Burbujas de colores</h2>
              <p className="text-base text-gray-500 dark:text-slate-400 mb-8 leading-relaxed">
                Explorá cómo te sentís. Tocá las burbujas que van apareciendo y hacelas explotar.
              </p>
              <Button variant="primary" fullWidth onClick={() => {
                setStarted(true)
                startSession()
              }}>
                Comenzar
              </Button>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="bubbles-activity">
      <div className="bubbles-scene" data-testid="bubbles-container">
        <img src={bgImage} className="bubbles-scene-bg absolute inset-0 w-full h-full object-cover object-bottom" alt="" draggable={false} />
        <img
          src={fishImage}
          className="absolute fish"
          style={{ bottom: '35%', left: '8%', width: '28vw', minWidth: '120px', maxWidth: '400px' }}
          alt=""
          draggable={false}
        />
        <BackButton to="/minigames" onBeforeNavigate={saveSession} />
        {bubbles.map(bubble => (
          <Bubble
            key={bubble.id}
            bubble={bubble}
            onClick={handleBubbleClick}
            popping={poppingIds.has(bubble.id)}
            poppingPosition={poppingPositions.get(bubble.id)}
            onPopEnd={handlePopEnd}
          />
        ))}
      </div>
    </div>
  )
}

export default BubblesActivity