import { useState, useEffect } from 'react'
import type { Cloud } from './types'
import './EmotionalClouds.css'

interface CloudProps {
  cloud: Cloud
  onRemove: () => void
  onSelect?: () => void
}

const EmotionalCloud = ({ cloud, onRemove, onSelect }: CloudProps) => {
  const [isLeaving, setIsLeaving] = useState(false)

  useEffect(() => {
    const driftMs = parseFloat(cloud.duration ?? '20') * 1000
    const timer = setTimeout(onRemove, driftMs)
    return () => clearTimeout(timer)
  }, [cloud.duration, onRemove])

  const handleClick = () => {
    if (isLeaving) return
    setIsLeaving(true)
    setTimeout(onRemove, 3000)
    onSelect?.()
  }

  return (
      <div
          className={`emotional-cloud ${isLeaving ? 'emotional-cloud--leaving' : ''}`}
          style={{
            '--cloud-top': cloud.top ?? '20%',
            '--drift-duration': cloud.duration ?? '20s',
            '--drift-delay': '0s',
          } as React.CSSProperties}
          onClick={handleClick}
          role="status"
          aria-live="polite"
          data-testid={`cloud-${cloud.id}`}
      >
        <span className="emotional-cloud__text">{cloud.text}</span>
      </div>
  )
}

export default EmotionalCloud

