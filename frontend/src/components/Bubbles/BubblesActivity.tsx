import { useState, useRef } from 'react'
import Bubble from './Bubble'
import type { Bubble as BubbleType, EmotionalBubblesActivityProps } from './types'
import './BubblesActivity.css'

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


const generateBubble = (id: string, delay: number): BubbleType => ({
  id,
  color: BUBBLE_COLORS[Math.floor(Math.random() * BUBBLE_COLORS.length)],
  size: 70 + Math.floor(Math.random() * 100),       
  driftX: 20 + Math.floor(Math.random() * 75),      
  driftY: -50 + Math.floor(Math.random() * 75),   
  animationDuration: 14 + Math.random() * 12,  
  animationDelay: delay,
})

const INITIAL_BUBBLES: BubbleType[] = Array.from(
  { length: 8 },
  (_, i) => generateBubble(`b-${i}`, i * 2.5)
)

const BubblesActivity = ({ onBack }: EmotionalBubblesActivityProps) => {
    const [started, setStarted] = useState(false)
    const [bubbles, setBubbles] = useState<BubbleType[]>(INITIAL_BUBBLES)
    const [poppingIds, setPoppingIds] = useState<Set<string>>(new Set())
    const [poppingPositions, setPoppingPositions] = useState<Map<string, { x: number; y: number }>>(new Map())
    const nextIdRef = useRef(INITIAL_BUBBLES.length)

    const handleBubbleClick = (bubble: BubbleType, position: { x: number; y: number }) => {
        if (poppingIds.has(bubble.id)) return
        setPoppingPositions(prev => new Map(prev).set(bubble.id, position))
        setPoppingIds(prev => new Set([...prev, bubble.id]))
        }

        const handlePopEnd = (id: string) => {
        setBubbles(prev => prev.filter(b => b.id !== id))
        setPoppingIds(prev => { const next = new Set(prev); next.delete(id); return next })
        setPoppingPositions(prev => { const next = new Map(prev); next.delete(id); return next })
         setTimeout(() => {
            nextIdRef.current++
            setBubbles(prev => [...prev, generateBubble(`b-respawn-${nextIdRef.current}`, 0)])
            }, 2000)
        }

    if(!started) {
        return (
            
                <div className="bubbles-activity">
                <div className="bubbles-scene">
                    <button
                        onClick={onBack}
                        className="fixed top-20 left-6 bg-white/80 backdrop-blur-sm rounded-full px-4 py-2 text-violeta text-sm flex items-center gap-2 shadow-sm hover:bg-white transition-colors"
                        type="button"
                        >
                        ← Volver
                        </button>
                    <div className="flex items-center justify-center w-full h-full">
                    <div className="bg-white backdrop-blur-sm rounded-2xl p-10 shadow-md w-[480px] max-w-[90vw] text-center">
                        <h2 className="text-3xl font-bold text-gray-800 mb-3">Burbujas de colores</h2>
                        <p className="text-base text-gray-500 mb-8 leading-relaxed">
                            Explorá cómo te sentís. Tocá las burbujas que van apareciendo y hacelas explotar.
                        </p>
                        <button
                            onClick={() => setStarted(true)}
                            className="w-full py-4 rounded-full bg-violeta text-white font-medium text-lg hover:opacity-90 transition-opacity"
                        >
                            Comenzar
                        </button>
                        </div>
                    </div>
                </div>
                </div>
            )
    }

    return(
        <div className="bubbles-activity">
            <div className="bubbles-scene" data-testid="bubbles-container">
                 <button
                        onClick={() => setStarted(false)}
                        className="fixed top-20 left-6 bg-white/80 backdrop-blur-sm rounded-full px-4 py-2 text-violeta text-sm flex items-center gap-2 shadow-sm hover:bg-white transition-colors"
                        type="button">
                        ← Volver
                    </button>
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