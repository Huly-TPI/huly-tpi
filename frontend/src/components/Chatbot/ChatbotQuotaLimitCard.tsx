import { Sparkles, CreditCard, ArrowRight } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import Button from '../Buttons/Button/Button'

interface ChatbotQuotaLimitCardProps {
  message: string
  onClose: () => void
}

export default function ChatbotQuotaLimitCard({ message, onClose }: ChatbotQuotaLimitCardProps) {
  const navigate = useNavigate()

  // Detect if it is the free plan limit
  const isFreePlan = message.includes('plan gratuito')

  return (
    <div className="w-full min-w-0 rounded-xl border border-violeta/30 bg-[var(--surface-accent)] p-3 sm:p-4 flex flex-col gap-3">
      <div>
        <p className="text-xs font-bold uppercase tracking-wide text-violeta flex items-center gap-1.5">
          <Sparkles className="h-3.5 w-3.5 text-violeta animate-pulse" />
          Límite Diario Alcanzado
        </p>
        <p className="mt-1 break-words text-xs leading-relaxed text-[var(--text-secondary)] sm:text-sm">
          {message}
        </p>
      </div>

      <div className="w-full">
        <Button
          type="button"
          onClick={() => {
            onClose()
            navigate('/shop')
          }}
          variant="primary"
          size="sm"
          fullWidth
          className="flex items-center justify-center gap-2"
        >
          {isFreePlan ? (
            <>
              <CreditCard className="h-4 w-4 shrink-0" />
              Adquirir Suscripción
            </>
          ) : (
            <>
              <ArrowRight className="h-4 w-4 shrink-0" />
              Ver Planes
            </>
          )}
        </Button>
      </div>
    </div>
  )
}
