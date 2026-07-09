import { beforeEach, describe, it, expect, vi } from 'vitest'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ThemeToggle from '../../components/ThemeToggle/ThemeToggle'
import { ThemeProvider } from '../../context/theme'
import { updateThemePreference } from '../../api/auth'
import { clickButton, clearAllMocks } from '../testHelpers'


vi.mock('../../api/auth', () => ({
  updateThemePreference: vi.fn(() => Promise.resolve()),
}))

describe('ThemeToggle', () => {
  beforeEach(() => {
    clearAllMocks()
    window.localStorage.clear()
    delete document.documentElement.dataset.theme
    document.documentElement.classList.remove('dark')
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {},
        removeListener: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => false,
      }),
    })
  })

  const renderToggle = () => {
    return render(
      <ThemeProvider>
        <ThemeToggle />
      </ThemeProvider>
    )
  }

  it('arranca con el tema del sistema cuando no hay usuario', () => {
    renderToggle()
    verifyButtonPresent('Cambiar a modo noche')
  })

  it('toma el tema que llega desde el perfil del usuario', () => {
    renderToggle()
    dispatchUserLoaded('DARK')
    return verifyButtonAsync('Cambiar a modo dia')
  })

  it('alterna y persiste el tema local cuando no hay usuario', () => {
    setupUser()
    renderToggle()
    return clickBtn('Cambiar a modo noche').then(() => {
      verifyButtonPresent('Cambiar a modo dia')
      verifyHtmlDatasetTheme('dark')
      verifyHtmlClassContains('dark', true)
      verifyLocalStorageTheme('dark')
      verifyUpdateThemePreferenceNotCalled()
    })
  })

  it('recupera el tema persistido al montar sin usuario', () => {
    setupLocalStorageTheme('dark')
    renderToggle()
    verifyButtonPresent('Cambiar a modo dia')
    verifyHtmlDatasetTheme('dark')
  })

  it('mantiene el tema elegido al limpiar la sesión', () => {
    setupUser()
    renderToggle()
    dispatchUserLoaded('LIGHT')
    return clickBtn('Cambiar a modo noche').then(() => {
      verifyLocalStorageTheme('dark')
      dispatchUserCleared()
      verifyButtonPresent('Cambiar a modo dia')
      verifyHtmlDatasetTheme('dark')
    })
  })

  it('persiste el tema al cambiarlo cuando hay usuario', () => {
    setupUser()
    renderToggle()
    dispatchUserLoaded('LIGHT')
    return clickBtn('Cambiar a modo noche').then(() => {
      verifyUpdateThemePreferenceCalledWith('DARK')
    })
  })
  let user: any

  /* helpers */

  const verifyButtonPresent = (name: string) => {
    expect(screen.getByRole('button', { name })).toBeInTheDocument()
  }

  const dispatchUserLoaded = (themePreference: string) => {
    act(() => {
      window.dispatchEvent(new CustomEvent('auth:user-loaded', {
        detail: {
          id: 1,
          name: 'Mili',
          email: 'mili@huly.com',
          role: 'USER',
          themePreference,
        },
      }))
    })
  }

  const verifyButtonAsync = (name: string) => {
    return screen.findByRole('button', { name }).then(() => {})
  }

  const setupUser = () => {
    user = userEvent.setup()
  }

  const clickBtn = (name: string) => {
    return clickButton(user, name)
  }

  const verifyHtmlDatasetTheme = (theme: string) => {
    expect(document.documentElement.dataset.theme).toBe(theme)
  }

  const verifyHtmlClassContains = (className: string, value: boolean) => {
    expect(document.documentElement.classList.contains(className)).toBe(value)
  }

  const verifyLocalStorageTheme = (theme: string) => {
    expect(window.localStorage.getItem('huly:scene-theme')).toBe(theme)
  }

  const setupLocalStorageTheme = (theme: string) => {
    window.localStorage.setItem('huly:scene-theme', theme)
  }

  const verifyUpdateThemePreferenceNotCalled = () => {
    expect(updateThemePreference).not.toHaveBeenCalled()
  }

  const verifyUpdateThemePreferenceCalledWith = (theme: string) => {
    expect(updateThemePreference).toHaveBeenCalledWith(theme)
  }

  const dispatchUserCleared = () => {
    act(() => {
      window.dispatchEvent(new CustomEvent('auth:user-cleared'))
    })
  }
})
