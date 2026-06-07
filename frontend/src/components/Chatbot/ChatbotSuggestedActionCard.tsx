import { useNavigate } from 'react-router-dom'
import Button from '../Buttons/Button/Button'

interface ChatbotSuggestedActionCardProps {
  title: string
  description: string
  actionUrl: string
  onClose: () => void
  decision?: 'accepted' | 'rejected'
  isLoading?: boolean
  error?: string
  onAccept: () => void | Promise<void>
  onReject: () => void | Promise<void>
}

export default function ChatbotSuggestedActionCard({
  title,
  description,
  actionUrl,
  onClose,
  decision,
  isLoading = false,
  error,
  onAccept,
  onReject,
}: ChatbotSuggestedActionCardProps) {
  const navigate = useNavigate()

  return (
    <div className="rounded-xl border border-violeta/30 bg-[var(--surface-accent)] p-3">
      <p className="text-xs font-bold uppercase tracking-wide text-violeta">Actividad sugerida</p>
      <p className="mt-1 text-sm font-semibold text-[var(--text-primary)]">{title}</p>
      <p className="mt-1 text-xs text-[var(--text-secondary)]">{description}</p>

      {!decision && (
        <div className="mt-2 flex gap-2">
          <Button
            type="button"
            onClick={onAccept}
            variant="primary"
            size="sm"
            isLoading={isLoading}
            loadingLabel="Guardando..."
            disabled={isLoading}
          >
            Aceptar
          </Button>
          <Button
            type="button"
            onClick={onReject}
            variant="secondary"
            size="sm"
            disabled={isLoading}
          >
            Rechazar
          </Button>
        </div>
      )}

      {decision === 'accepted' && (
        <div className="mt-2 flex flex-col gap-2">
          <p className="text-xs font-semibold text-bosque dark:text-menta">Actividad aceptada.</p>
          <Button
            type="button"
            onClick={() => {
              onClose()
              navigate(actionUrl)
            }}
            variant="primary"
            size="sm"
          >
            Ir a la actividad
          </Button>
        </div>
      )}

      {decision === 'rejected' && (
        <p className="mt-2 text-xs font-semibold text-bosque dark:text-menta">Actividad rechazada. Seguimos por chat.</p>
      )}

      {!!error && <p className="mt-2 text-xs font-semibold text-red-500">{error}</p>}
    </div>
  )
}
