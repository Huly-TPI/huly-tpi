import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from './auth'
import AuthGateModal from '../components/AuthGateModal/AuthGateModal'

interface AuthGateContextValue {
  requireAuth: (action: () => void) => void
}

const AuthGateContext = createContext<AuthGateContextValue | undefined>(undefined)

interface AuthGateProviderProps {
  children: ReactNode
}

export function AuthGateProvider({ children }: AuthGateProviderProps) {
  const { isAuthenticated, loading } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  const requireAuth = useCallback(
    (action: () => void) => {
      if (loading) 
        return
      
      if (isAuthenticated)
        action()
      else 
        setOpen(true)
      
    },
    [isAuthenticated, loading],
  )

  const closeModal = useCallback(() => setOpen(false), [])

  const goToLogin = useCallback(() => {
    setOpen(false)
    navigate('/login')
  }, [navigate])

  const goToRegister = useCallback(() => {
    setOpen(false)
    navigate('/register')
  }, [navigate])

  const value = useMemo<AuthGateContextValue>(
    () => ({ requireAuth }),
    [requireAuth],
  )

  return (
    <AuthGateContext.Provider value={value}>
      {children}
      <AuthGateModal
        open={open}
        onClose={closeModal}
        onLogin={goToLogin}
        onRegister={goToRegister}
      />
    </AuthGateContext.Provider>
  )
}

export function useAuthGate() {
  const context = useContext(AuthGateContext)
  if (!context) {
    throw new Error('useAuthGate must be used within an AuthGateProvider')
  }
  return context
}