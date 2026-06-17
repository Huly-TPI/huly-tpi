import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { BadgeResponse } from '../api/badges'
import BadgeUnlockToast from '../components/Badges/BadgeUnlockToast'

interface BadgeToastContextType {
  showBadgeToast: (badge: BadgeResponse) => void
}

const BadgeToastContext = createContext<BadgeToastContextType>({
  showBadgeToast: () => {}
})

export function BadgeToastProvider({ children }: { children: React.ReactNode }) {
  const [badge, setBadge] = useState<BadgeResponse | null>(null)
  const [isHomeOnboardingActive, setIsHomeOnboardingActive] = useState(
    document.body.getAttribute('data-home-onboarding-active') === 'true',
  )

  const showBadgeToast = useCallback((badge: BadgeResponse) => setBadge(badge), [])
  const dismiss = useCallback(() => setBadge(null), [])

  useEffect(() => {
    const syncHomeOnboardingState = () => {
      setIsHomeOnboardingActive(document.body.getAttribute('data-home-onboarding-active') === 'true')
    }

    window.addEventListener('home-onboarding-visibility-change', syncHomeOnboardingState)
    return () => {
      window.removeEventListener('home-onboarding-visibility-change', syncHomeOnboardingState)
    }
  }, [])

  return (
    <BadgeToastContext.Provider value={{ showBadgeToast }}>
      {children}
      {badge && !isHomeOnboardingActive && <BadgeUnlockToast badge={badge} onDismiss={dismiss} />}
    </BadgeToastContext.Provider>
  )
}

export const useBadgeToast = () => useContext(BadgeToastContext)
