import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useAuth } from '../../context/auth'
import Button from '../Buttons/Button/Button'
import BadgeModal from './BadgeModal.tsx'

export default function BadgeLauncher() { 
    const { isAuthenticated } = useAuth()
    const location = useLocation()
    const [isOpen, setIsOpen] = useState(false)
    const [isHomeOnboardingActive, setIsHomeOnboardingActive] = useState(document.body.getAttribute('data-home-onboarding-active') === 'true')
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

    return (<>
        <Button
            type="button"
            onClick={() => setIsOpen(true)}
            aria-label="Abrir insignias"
            variant="primary"
            size="sm"
               className="fixed bottom-5 left-5 z-40 !h-16 !w-16 !min-w-0 rounded-full !p-0 shadow-xl transition hover:scale-105"
        >
            Abrir insignias
        </Button>
        <BadgeModal isOpen={isOpen} onClose={() => setIsOpen(false)} />
    </>)
        }
    