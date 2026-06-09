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
        <button
            type="button"
            onClick={() => setIsOpen(true)}
            aria-label="Abrir insignias"
            className="fixed bottom-5 left-5 z-40 h-20 w-20 transition hover:scale-105 drop-shadow-xl"
        >
            <img src="/badges/badge_launcher.webp" alt="Abrir insignias" className="h-full w-full object-contain" />
        </button>
        <BadgeModal isOpen={isOpen} onClose={() => setIsOpen(false)} />
    </>)
}
