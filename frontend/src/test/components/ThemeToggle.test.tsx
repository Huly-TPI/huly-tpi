import { beforeEach, describe, it, expect, vi } from 'vitest'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ThemeToggle from '../../components/ThemeToggle/ThemeToggle'
import { ThemeProvider } from '../../context/theme'
import { updateThemePreference } from '../../api/auth'

vi.mock('../../api/auth', () => ({
  updateThemePreference: vi.fn(() => Promise.resolve()),
}))

describe('ThemeToggle', () => {
  beforeEach(() => {
    vi.clearAllMocks()
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
    expect(screen.getByRole('button', { name: 'Cambiar a modo noche' })).toBeInTheDocument()
  })

  it('toma el tema que llega desde el perfil del usuario', async () => {
    renderToggle()
    act(() => {
      window.dispatchEvent(new CustomEvent('auth:user-loaded', {
        detail: {
          id: 1,
          name: 'Mili',
          email: 'mili@huly.com',
          role: 'USER',
          themePreference: 'DARK',
        },
      }))
    })

    await screen.findByRole('button', { name: 'Cambiar a modo dia' })
  })

  it('alterna el tema sin persistir cuando no hay usuario', async () => {
    const user = userEvent.setup()
    renderToggle()

    await user.click(screen.getByRole('button', { name: 'Cambiar a modo noche' }))

    expect(screen.getByRole('button', { name: 'Cambiar a modo dia' })).toBeInTheDocument()
    expect(document.documentElement.dataset.theme).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
    expect(updateThemePreference).not.toHaveBeenCalled()
  })

  it('persiste el tema al cambiarlo cuando hay usuario', async () => {
    const user = userEvent.setup()
    renderToggle()
    act(() => {
      window.dispatchEvent(new CustomEvent('auth:user-loaded', {
        detail: {
          id: 1,
          name: 'Mili',
          email: 'mili@huly.com',
          role: 'USER',
          themePreference: 'LIGHT',
        },
      }))
    })

    await user.click(screen.getByRole('button', { name: 'Cambiar a modo noche' }))

    expect(updateThemePreference).toHaveBeenCalledWith('DARK')
  })
})
