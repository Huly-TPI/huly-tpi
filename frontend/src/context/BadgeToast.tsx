import { createContext, useCallback, useContext, useState } from 'react'
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

  const showBadgeToast = useCallback((badge: BadgeResponse) => setBadge(badge), [])
  const dismiss = useCallback(() => setBadge(null), [])

  return (
    <BadgeToastContext.Provider value={{ showBadgeToast }}>
      {children}
      {badge && <BadgeUnlockToast badge={badge} onDismiss={dismiss} />}
    </BadgeToastContext.Provider>
  )
}

export const useBadgeToast = () => useContext(BadgeToastContext)