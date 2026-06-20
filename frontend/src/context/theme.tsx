import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { SceneTheme } from '../components/Scene/SceneElement/SceneElement'
import { updateThemePreference, type UserProfile } from '../api/auth'

interface ThemeContextValue {
  theme: SceneTheme
  setTheme: (theme: SceneTheme) => void
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)

const THEME_STORAGE_KEY = 'huly:scene-theme'

const isSceneTheme = (value: string | null): value is SceneTheme =>
  value === 'light' || value === 'dark'

const getSystemTheme = (): SceneTheme => {
  if (typeof window.matchMedia !== 'function') return 'light'
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

const getStoredTheme = (): SceneTheme | null => {
  const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY)
  return isSceneTheme(storedTheme) ? storedTheme : null
}

const getInitialTheme = (): SceneTheme =>
  getStoredTheme() ?? getSystemTheme()

const toSceneTheme = (themePreference: UserProfile['themePreference']): SceneTheme =>
  themePreference === 'DARK' ? 'dark' : 'light'

const toThemePreference = (theme: SceneTheme): UserProfile['themePreference'] =>
  theme === 'dark' ? 'DARK' : 'LIGHT'

interface ThemeProviderProps {
  children: ReactNode
}

export function ThemeProvider({ children }: ThemeProviderProps) {
  const [theme, setThemeState] = useState<SceneTheme>(getInitialTheme)
  const [hasUserTheme, setHasUserTheme] = useState(false)

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    document.documentElement.classList.toggle('dark', theme === 'dark')
  }, [theme])

  useEffect(() => {
    const handleUserLoaded = (event: Event) => {
      const profile = (event as CustomEvent<UserProfile>).detail
      setHasUserTheme(true)
      const userTheme = toSceneTheme(profile.themePreference)
      setThemeState(userTheme)
      window.localStorage.setItem(THEME_STORAGE_KEY, userTheme)
    }

    const handleUserCleared = () => {
      setHasUserTheme(false)
      setThemeState(getInitialTheme())
    }

    window.addEventListener('auth:user-loaded', handleUserLoaded)
    window.addEventListener('auth:user-cleared', handleUserCleared)
    return () => {
      window.removeEventListener('auth:user-loaded', handleUserLoaded)
      window.removeEventListener('auth:user-cleared', handleUserCleared)
    }
  }, [])

  useEffect(() => {
    if (hasUserTheme) return
    if (typeof window.matchMedia !== 'function') return

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handleChange = () => {
      const storedTheme = getStoredTheme()
      setThemeState(storedTheme ?? getSystemTheme())
    }

    mediaQuery.addEventListener('change', handleChange)
    return () => mediaQuery.removeEventListener('change', handleChange)
  }, [hasUserTheme])

  const persistTheme = useCallback((nextTheme: SceneTheme) => {
    window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme)
    if (!hasUserTheme) return
    void updateThemePreference(toThemePreference(nextTheme))
  }, [hasUserTheme])

  const setTheme = useCallback((nextTheme: SceneTheme) => {
    setThemeState(nextTheme)
    persistTheme(nextTheme)
  }, [persistTheme])

  const toggleTheme = useCallback(() => {
    setThemeState(currentTheme => {
      const nextTheme = currentTheme === 'light' ? 'dark' : 'light'
      persistTheme(nextTheme)
      return nextTheme
    })
  }, [persistTheme])

  const value = useMemo<ThemeContextValue>(() => ({
    theme,
    setTheme,
    toggleTheme,
  }), [theme, setTheme, toggleTheme])

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
}

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider')
  }
  return context
}
