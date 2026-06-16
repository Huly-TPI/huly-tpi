import Button from '../Buttons/Button/Button'

interface ChatbotChallengeCardProps {
  title: string
  description: string
  decision?: 'accepted' | 'rejected'
  onAccept: () => void | Promise<void>
  onReject: () => void | Promise<void>
}

export default function ChatbotChallengeCard({
  title,
  description,
  decision,
  onAccept,
  onReject,
}: ChatbotChallengeCardProps) {
  return (
    <div className="w-full min-w-0 rounded-xl border border-bosque/45 bg-bosque/10 p-3 sm:p-4">
      <p className="text-xs font-bold uppercase tracking-wide text-bosque">Reto propuesto</p>
      <p className="mt-1 break-words text-sm font-semibold text-[var(--text-primary)] sm:text-[0.95rem]">
        {title}
      </p>
      <p className="mt-1 break-words text-xs leading-relaxed text-[var(--text-secondary)] sm:text-sm">
        {description}
      </p>

      {!decision && (
        <div className="mt-3 flex flex-col gap-2 sm:flex-row">
          <Button type="button" variant="success" size="sm" onClick={onAccept} fullWidth>
            Aceptar
          </Button>
          <Button type="button" variant="successSecondary" size="sm" onClick={onReject} fullWidth>
            Rechazar
          </Button>
        </div>
      )}

      {decision && (
        <p className="mt-2 text-xs font-semibold text-bosque dark:text-menta">
          {decision === 'accepted' ? 'Reto aceptado.' : 'Reto rechazado.'}
        </p>
      )}
    </div>
  )
}

