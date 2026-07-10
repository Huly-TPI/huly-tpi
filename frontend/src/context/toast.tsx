import React, { createContext, useContext, useState, useCallback } from 'react'
import { Toast, ToastType } from '../components/backoffice/Toast'

interface ToastItem {
  id: string
  message: string
  type: ToastType
  duration?: number
}

interface ToastContextType {
  showToast: (message: string, type?: ToastType, duration?: number) => void
  hideToast: (id: string) => void
  setToastsRaised: (raised: boolean) => void
}

const ToastContext = createContext<ToastContextType | undefined>(undefined)

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])
  const [raised, setRaised] = useState(false)

  const hideToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const showToast = useCallback((message: string, type: ToastType = 'error', duration = 6000) => {
    const id = Math.random().toString(36).substring(2, 9)
    setToasts((prev) => [...prev, { id, message, type, duration }])
  }, [])

  const setToastsRaised = useCallback((value: boolean) => {
    setRaised(value)
  }, [])

  return (
    <ToastContext.Provider value={{ showToast, hideToast, setToastsRaised }}>
      {children}
      {/* Toast container for stacked notifications */}
      <div
        className={`fixed right-5 z-[9999] flex flex-col gap-3 transition-[bottom] duration-300 ${
          raised ? 'bottom-52' : 'bottom-5'
        }`}
      >
        {toasts.map((t) => (
          <Toast
            key={t.id}
            message={t.message}
            type={t.type}
            duration={t.duration}
            isPortal={false}
            onClose={() => hideToast(t.id)}
          />
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider')
  }
  return context
}