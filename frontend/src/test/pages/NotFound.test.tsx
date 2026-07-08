import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'
import NotFound from '../../pages/NotFound/NotFound'

describe('NotFound', () => {
  let activeContainer: HTMLElement

  beforeEach(() => {
    window.localStorage.clear()
  })

  describe('fondo', () => {
    it('activa los fondos en modo día', () => {
      setupTheme('light')
      renderNotFoundPage()
      verifyActiveBackgroundsCountGreaterThan(0)
    })

    it('activa los fondos en modo noche', () => {
      setupTheme('dark')
      renderNotFoundPage()
      verifyActiveBackgroundsCountGreaterThan(0)
    })

    it('no activa fondos oscuros en modo día', () => {
      setupTheme('light')
      renderNotFoundPage()
      verifyActiveBackgroundsLessThanTotal()
    })

    it('no activa fondos claros en modo noche', () => {
      setupTheme('dark')
      renderNotFoundPage()
      verifyActiveBackgroundsLessThanTotal()
    })
  })

  describe('navegación', () => {
    it('el link Volver al jardín apunta al inicio', () => {
      renderNotFoundPage()
      verifyGardenLinkPointsTo('/')
    })
  })

  /* helpers */

  const renderNotFoundPage = () => {
    const { container } = render(
      <ThemeProvider>
        <MemoryRouter>
          <NotFound />
        </MemoryRouter>
      </ThemeProvider>
    )
    activeContainer = container
  }

  const setupTheme = (theme: 'light' | 'dark') => {
    window.localStorage.setItem('huly:scene-theme', theme)
  }

  const getActiveBackgroundsCount = () => {
    return activeContainer.querySelectorAll('.not-found__bg--active').length
  }

  const verifyActiveBackgroundsCountGreaterThan = (val: number) => {
    expect(getActiveBackgroundsCount()).toBeGreaterThan(val)
  }

  const verifyActiveBackgroundsLessThanTotal = () => {
    const allBgs = activeContainer.querySelectorAll('.not-found__bg').length
    const active = getActiveBackgroundsCount()
    expect(active).toBeLessThan(allBgs)
  }

  const verifyGardenLinkPointsTo = (path: string) => {
    expect(screen.getByRole('link', { name: 'Volver al jardín' })).toHaveAttribute('href', path)
  }
})
