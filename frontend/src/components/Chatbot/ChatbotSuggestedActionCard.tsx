import { useNavigate } from 'react-router-dom'
import Button from '../Buttons/Button/Button'

interface ChatbotSuggestedActionCardProps {
  title: string
  description: string
  actionUrl: string
  onClose: () => void
  decision?: 'accepted' | 'rejected'
  onReject: () => void | Promise<void>
}

export default function ChatbotSuggestedActionCard({
  title,
  description,
  actionUrl,
  onClose,
  decision,
  onReject,
}: ChatbotSuggestedActionCardProps) {
  const navigate = useNavigate()

  return (
    <div className="rounded-xl border border-violeta/20 bg-violeta-claro/30 p-3">
      <p className="text-xs font-bold uppercase tracking-wide text-violeta">Actividad sugerida</p>
      <p className="mt-1 text-sm font-semibold text-gray-800">{title}</p>
      <p className="mt-1 text-xs text-gray-600">{description}</p>
      <div className="mt-2 flex gap-2">
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
        {!decision && (
          <Button type="button" onClick={onReject} variant="secondary" size="sm">
            No ir por ahora
          </Button>
        )}
      </div>
      {decision === 'rejected' && <p className="mt-2 text-xs font-semibold text-bosque">Seguimos por chat.</p>}
    </div>
  )
}

