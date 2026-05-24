import { beforeEach, describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ThemeToggle from '../../components/ThemeToggle/ThemeToggle'
import { ThemeProvider } from '../../context/theme'

describe('ThemeToggle', () => {
  beforeEach(() => {
    window.localStorage.clear()
    delete document.documentElement.dataset.theme
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

  it('arranca en modo claro por defecto si no hay preferencia guardada', () => {
    renderToggle()
    expect(screen.getByRole('button', { name: 'Cambiar a modo noche' })).toBeInTheDocument()
  })

  it('toma modo guardado en localStorage', () => {
    window.localStorage.setItem('huly:scene-theme', 'dark')
    renderToggle()

    expect(screen.getByRole('button', { name: 'Cambiar a modo dia' })).toBeInTheDocument()
  })

  it('alterna y persiste el tema al hacer click', async () => {
    const user = userEvent.setup()
    renderToggle()

    await user.click(screen.getByRole('button', { name: 'Cambiar a modo noche' }))

    expect(screen.getByRole('button', { name: 'Cambiar a modo dia' })).toBeInTheDocument()
    expect(window.localStorage.getItem('huly:scene-theme')).toBe('dark')
    expect(document.documentElement.dataset.theme).toBe('dark')
  })
})
