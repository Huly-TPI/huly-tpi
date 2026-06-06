import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import chatbotImage from '../../assets/chatbot/huly-chatbot.webp'
import ChatbotModal from './ChatbotModal'
import Button from '../Buttons/Button/Button'
import { useAuth } from '../../context/auth'

export default function ChatbotLauncher() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()
  const [isOpen, setIsOpen] = useState(false)
  const [isHomeOnboardingActive, setIsHomeOnboardingActive] = useState(
    document.body.getAttribute('data-home-onboarding-active') === 'true',
  )
  const isEmotionalOnboardingRoute = location.pathname === '/onboarding'

  useEffect(() => {
    const syncHomeOnboardingState = () => {
      setIsHomeOnboardingActive(document.body.getAttribute('data-home-onboarding-active') === 'true')
    }

    window.addEventListener('home-onboarding-visibility-change', syncHomeOnboardingState)
    return () => {
      window.removeEventListener('home-onboarding-visibility-change', syncHomeOnboardingState)
    }
  }, [])

  if (!isAuthenticated || isEmotionalOnboardingRoute || isHomeOnboardingActive) return null

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
