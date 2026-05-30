import type { Bubble as BubbleType } from './types'

interface BubbleProps {
    bubble: BubbleType
    onClick: (bubble: BubbleType, position: { x: number; y: number }) => void
    popping?: boolean
    poppingPosition?: { x: number; y: number }
    onPopEnd?: (id: string) => void
}

const Bubble = ({ bubble, onClick, popping = false, poppingPosition, onPopEnd }: BubbleProps) => {
  const style: React.CSSProperties = {
    width: `${bubble.size}px`,
    height: `${bubble.size}px`,
    background: bubble.color,
    '--float-duration': `${bubble.animationDuration}s`,
    '--float-delay': `${bubble.animationDelay}s`,
    '--drift-x': `${bubble.driftX}vw`,
    '--drift-y': `${bubble.driftY}vh`,
    ...(popping && poppingPosition ? {
      position: 'fixed',
      left: poppingPosition.x,
      top: poppingPosition.y,
    } : {}),
  } as React.CSSProperties

   const handleClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (popping) return
    const rect = e.currentTarget.getBoundingClientRect()
    onClick(bubble, { x: rect.left, y: rect.top })
  }

  return (
    <div
      className={`bubble${popping ? ' bubble--popping' : ''}`}
      data-testid={`bubble-${bubble.id}`}
      style={style}
      onClick={handleClick}
     onAnimationEnd={(e) => {
      if (popping && e.animationName === 'bubble-pop') {
        onPopEnd?.(bubble.id)
      }
    }}
    >
      <div className="bubble__shine" />
    </div>
  )
}

export default Bubble