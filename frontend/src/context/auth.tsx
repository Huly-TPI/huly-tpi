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
import { clearToken, setToken, tryRehydrateSession } from '../api/client'

interface AuthContextValue {
  user: UserProfile | null
  loading: boolean
  isAuthenticated: boolean
  login: (credentials: LoginRequest) => Promise<UserProfile>
  loginWithToken: (accessToken: string) => Promise<UserProfile>
  refreshUser: () => Promise<UserProfile>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    const profile = await getMe()
    setUser(profile)
    return profile
  }, [])

  const loginWithToken = useCallback(
    async (accessToken: string) => {
      setToken(accessToken)
      return refreshUser()
    },
    [refreshUser],
  )

  const login = useCallback(
    async (credentials: LoginRequest) => {
      const res = await loginRequest(credentials)
      if (!res?.accessToken) {
        throw new Error('No se recibió el token de acceso')
      }
      return loginWithToken(res.accessToken)
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
      let token: string | null = null

      try {
        token = await tryRehydrateSession()
      } catch {
        clearToken()
        setUser(null)
        setLoading(false)
        return
      }

      if (!token) {
        setLoading(false)
        return
      }

      try {
        await refreshUser()
      } catch {
        // El access token sigue siendo válido, no limpiamos la sesión.
        // La próxima request que necesite el user volverá a intentar getMe.
      } finally {
        setLoading(false)
      }
    }
    void rehydrate()
  }, [refreshUser])

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
      refreshUser,
      logout,
    }),
    [user, loading, login, loginWithToken, refreshUser, logout],
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