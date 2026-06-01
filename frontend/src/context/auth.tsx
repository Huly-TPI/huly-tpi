import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  getMe,
  login as loginRequest,
  logout as logoutRequest,
  type LoginRequest,
  type UserProfile,
} from '../api/auth'
import { clearToken, getToken, setToken } from '../api/client'

interface AuthContextValue {
  user: UserProfile | null
  loading: boolean
  isAuthenticated: boolean
  login: (credentials: LoginRequest) => Promise<void>
  loginWithToken: (accessToken: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)

  const loginWithToken = useCallback(async (accessToken: string) => {
    setToken(accessToken)
    const profile = await getMe()
    setUser(profile)
  }, [])

  const login = useCallback(
    async (credentials: LoginRequest) => {
      const res = await loginRequest(credentials)
      if (!res?.accessToken) {
        throw new Error('No se recibió el token de acceso')
      }
      await loginWithToken(res.accessToken)
    },
    [loginWithToken],
  )

  const logout = useCallback(async () => {
    try {
      await logoutRequest()
    } finally {
      clearToken()
      setUser(null)
    }
  }, [])

  useEffect(() => {
    const rehydrate = async () => {
      if (!getToken()) {
        setLoading(false)
        return
      }
      try {
        const profile = await getMe()
        setUser(profile)
      } catch {
        clearToken()
        setUser(null)
      } finally {
        setLoading(false)
      }
    }
    void rehydrate()
  }, [])

  useEffect(() => {
    const handleExpired = () => setUser(null)
    window.addEventListener('auth:expired', handleExpired)
    return () => window.removeEventListener('auth:expired', handleExpired)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      isAuthenticated: user !== null,
      login,
      loginWithToken,
      logout,
    }),
    [user, loading, login, loginWithToken, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}