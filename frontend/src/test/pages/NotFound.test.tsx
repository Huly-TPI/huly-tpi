import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'
import NotFound from '../../pages/NotFound/NotFound'


function renderNotFound() {
  return render(
    <ThemeProvider>
      <MemoryRouter>
        <NotFound />
      </MemoryRouter>
    </ThemeProvider>
  )
}


const getActiveBackgrounds = (container: HTMLElement) =>
  container.querySelectorAll('.not-found__bg--active')

const getGardenLink = () => screen.getByRole('link', { name: 'Volver al jardín' })


describe('NotFound', () => {
  beforeEach(() => {
    window.localStorage.clear()
  })

  describe('fondo', () => {
    it('activa los fondos en modo día', () => {
      window.localStorage.setItem('huly:scene-theme', 'light')
      const { container } = renderNotFound()
      expect(getActiveBackgrounds(container).length).toBeGreaterThan(0)
    })

    it('activa los fondos en modo noche', () => {
      window.localStorage.setItem('huly:scene-theme', 'dark')
      const { container } = renderNotFound()
      expect(getActiveBackgrounds(container).length).toBeGreaterThan(0)
    })

    it('no activa fondos oscuros en modo día', () => {
      window.localStorage.setItem('huly:scene-theme', 'light')
      const { container } = renderNotFound()
      const allBgs = container.querySelectorAll('.not-found__bg')
      const active = getActiveBackgrounds(container)
      expect(active.length).toBeLessThan(allBgs.length)
    })

    it('no activa fondos claros en modo noche', () => {
      window.localStorage.setItem('huly:scene-theme', 'dark')
      const { container } = renderNotFound()
      const allBgs = container.querySelectorAll('.not-found__bg')
      const active = getActiveBackgrounds(container)
      expect(active.length).toBeLessThan(allBgs.length)
    })
  })

  describe('navegación', () => {
    it('el link Volver al jardín apunta al inicio', () => {
      renderNotFound()
      expect(getGardenLink()).toHaveAttribute('href', '/')
    })
  })
})
