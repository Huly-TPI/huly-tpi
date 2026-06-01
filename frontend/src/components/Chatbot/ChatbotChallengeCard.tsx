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
    <div className="rounded-xl border border-menta bg-fondo p-3">
      <p className="text-xs font-bold uppercase tracking-wide text-bosque">Reto propuesto</p>
      <p className="mt-1 text-sm font-semibold text-gray-800">{title}</p>
      <p className="mt-1 text-xs text-gray-600">{description}</p>

      {!decision && (
        <div className="mt-2 flex gap-2">
          <Button type="button" variant="primary" size="sm" onClick={onAccept}>
            Aceptar
          </Button>
          <Button type="button" variant="secondary" size="sm" onClick={onReject}>
            Rechazar
          </Button>
        </div>
      )}

      {decision && (
        <p className="mt-2 text-xs font-semibold text-bosque">
          {decision === 'accepted' ? 'Reto aceptado.' : 'Reto rechazado.'}
        </p>
      )}
    </div>
  )
}

