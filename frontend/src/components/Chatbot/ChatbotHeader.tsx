import Button from '../Buttons/Button/Button'
import hulyChatbotImage from '../../assets/chatbot/huly-chatbot.webp'

interface ChatbotHeaderProps {
  onClose: () => void
}

export default function ChatbotHeader({ onClose }: ChatbotHeaderProps) {
  return (
    <header className="flex items-center justify-between border-b border-[var(--border-soft)] px-6 py-4">
      <div className="flex items-center gap-2">
        <img src={hulyChatbotImage} alt="" aria-hidden className="h-9 w-9 rounded-full object-cover" />
        <h2 className="text-3xl font-extrabold text-violeta">Huly</h2>
      </div>
      <Button
        type="button"
        onClick={onClose}
        variant="secondary"
        size="sm"
        className="!w-auto !min-w-0"
      >
        Cerrar
      </Button>
    </header>
  )
}
