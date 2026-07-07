import { useState } from 'react'
import { Sparkles } from 'lucide-react'
import Tooltip from './Tooltip'

export interface RecommendationTriggerButtonProps {
  onClick: () => Promise<void>
}

export default function RecommendationTriggerButton({ onClick }: RecommendationTriggerButtonProps) {
  const [loading, setLoading] = useState(false)

  const handleClick = async () => {
    setLoading(true)
    try {
      await onClick()
    } finally {
      setLoading(false)
    }
  }

  return (
    <Tooltip label="Generar combinación recomendada" wrap align="start">
      <button
        type="button"
        onClick={handleClick}
        disabled={loading}
        aria-label="Generar combinación recomendada"
        className="flex h-10 w-10 items-center justify-center rounded-full bg-violeta text-white shadow-md transition-transform hover:scale-105 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <Sparkles className={`h-5 w-5 ${loading ? 'animate-pulse' : ''}`} />
      </button>
    </Tooltip>
  )
}
