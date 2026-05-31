import { useEffect, useState } from 'react'
import chatbotImage from '../../assets/chatbot/huly-chatbot.webp'
import ChatbotModal from './ChatbotModal'
import Button from '../Buttons/Button/Button'

export default function ChatbotLauncher() {
  const [isOpen, setIsOpen] = useState(false)
  const [hasSession, setHasSession] = useState(false)

  useEffect(() => {
    const syncSession = () => {
      setHasSession(Boolean(localStorage.getItem('token')))
    }

    syncSession()
    window.addEventListener('storage', syncSession)
    window.addEventListener('focus', syncSession)

    return () => {
      window.removeEventListener('storage', syncSession)
      window.removeEventListener('focus', syncSession)
    }
  }, [])

  if (!hasSession) return null

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
