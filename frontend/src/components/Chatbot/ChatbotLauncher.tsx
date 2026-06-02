import { useState } from 'react'
import chatbotImage from '../../assets/chatbot/huly-chatbot.webp'
import ChatbotModal from './ChatbotModal'
import Button from '../Buttons/Button/Button'
import { useAuth } from '../../context/auth'

export default function ChatbotLauncher() {
  const { isAuthenticated } = useAuth()
  const [isOpen, setIsOpen] = useState(false)

  if (!isAuthenticated) return null

  return (
    <>
      <Button
        type="button"
        onClick={() => setIsOpen(true)}
        aria-label="Abrir chat de Huly"
        variant="primary"
        size="sm"
        className="fixed bottom-5 right-5 z-40 !h-16 !w-16 md:!h-16 md:!w-16 !min-w-0 rounded-full !p-0 shadow-xl transition hover:scale-105"
      >
        <img
          src={chatbotImage}
          alt="Huly chatbot"
          className="h-11 w-11 rounded-full object-cover md:h-11 md:w-11"
        />
      </Button>

      <ChatbotModal isOpen={isOpen} onClose={() => setIsOpen(false)} />
    </>
  )
}
