import { createContext, useContext, useState } from 'react'
import type { ReactNode } from 'react'

interface SubscriptionModalContextType {
  subscriptionOpen: boolean
  openSubscriptionModal: () => void
  closeSubscriptionModal: () => void
}

const SubscriptionModalContext = createContext<SubscriptionModalContextType | null>(null)

export function SubscriptionModalProvider({ children }: { children: ReactNode }) {
  const [subscriptionOpen, setSubscriptionOpen] = useState(false)

  return (
    <SubscriptionModalContext.Provider value={{
      subscriptionOpen,
      openSubscriptionModal: () => setSubscriptionOpen(true),
      closeSubscriptionModal: () => setSubscriptionOpen(false),
    }}>
      {children}
    </SubscriptionModalContext.Provider>
  )
}

export function useSubscriptionModal() {
  const ctx = useContext(SubscriptionModalContext)
  if (!ctx) throw new Error('useSubscriptionModal must be used within SubscriptionModalProvider')
  return ctx
}
