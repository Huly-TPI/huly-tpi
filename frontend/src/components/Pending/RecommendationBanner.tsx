import { useState } from 'react'
import { Check, X } from 'lucide-react'

export interface RecommendationBannerProps {
  taskTitles: string[]
  onAccept: () => Promise<void>
  onReject: () => Promise<void>
}

export default function RecommendationBanner({ taskTitles, onAccept, onReject }: RecommendationBannerProps) {
  const [loading, setLoading] = useState<'accept' | 'reject' | null>(null)

  const handleAccept = async () => {
    setLoading('accept')
    try {
      await onAccept()
    } finally {
      setLoading(null)
    }
  }

  const handleReject = async () => {
    setLoading('reject')
    try {
      await onReject()
    } finally {
      setLoading(null)
    }
  }

  const isBusy = loading !== null

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-[1000] flex items-center justify-center bg-black/[0.42] p-4 backdrop-blur-[3px]"
    >
      <div className="w-[320px] rounded-lg bg-[#fff8e7] p-6 shadow-[0_8px_24px_rgba(0,0,0,0.28)]">
        <p className="mb-1 text-[0.7rem] font-bold uppercase tracking-[0.07em] text-[rgba(92,61,30,0.5)]">
          Recomendación para hoy
        </p>
        <p className="mb-3 text-sm text-[#3b2510]">
          Encontramos esta combinación para ayudarte a tener un día más equilibrado:
        </p>

        <ul className="mb-4 flex flex-col gap-1.5">
          {taskTitles.map((title, index) => (
            <li
              key={index}
              className="rounded-md bg-[rgba(92,61,30,0.08)] px-3 py-2 text-sm font-medium text-[#3b2510]"
            >
              {title}
            </li>
          ))}
        </ul>

        <div className="flex justify-center gap-4">
          <button
            type="button"
            onClick={handleReject}
            disabled={isBusy}
            aria-label="Rechazar recomendación"
            className="flex h-12 w-12 items-center justify-center rounded-full border border-anaranjado text-anaranjado transition-colors hover:bg-anaranjado hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
          >
            <X className="h-5 w-5" />
          </button>
          <button
            type="button"
            onClick={handleAccept}
            disabled={isBusy}
            aria-label="Aceptar recomendación"
            className="flex h-12 w-12 items-center justify-center rounded-full bg-bosque text-white transition-colors hover:shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.12)] disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Check className="h-5 w-5" />
          </button>
        </div>
      </div>
    </div>
  )
}
