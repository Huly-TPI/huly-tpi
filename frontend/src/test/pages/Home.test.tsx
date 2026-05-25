import { beforeEach, describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Home from '../../pages/Home/Home'
import { ThemeProvider } from '../../context/theme'

describe('Home', () => {
  beforeEach(() => {
    window.localStorage.clear()
    window.localStorage.setItem('huly:scene-theme', 'light')
    window.localStorage.setItem('huly:home-onboarding-completed', 'true')
  })

  const renderWithRouter = () => {
    return render(
      <ThemeProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </ThemeProvider>
    )
  }

  it('renderiza el fondo del jardin', () => {
    const { container } = renderWithRouter()
    const activeLightBackgrounds = container.querySelectorAll(
      '.garden-scene__background--light.garden-scene__background--active',
    )

    expect(activeLightBackgrounds.length).toBeGreaterThan(0)
  })

  it('activa el fondo oscuro al alternar a modo noche', async () => {
    const user = userEvent.setup()
    const { container } = renderWithRouter()

    await user.click(screen.getByRole('button', { name: 'Cambiar a modo noche' }))

    const activeDarkBackgrounds = container.querySelectorAll(
      '.garden-scene__background--dark.garden-scene__background--active',
    )
    const activeLightBackgrounds = container.querySelectorAll(
      '.garden-scene__background--light.garden-scene__background--active',
    )

    expect(activeDarkBackgrounds.length).toBeGreaterThan(0)
    expect(activeLightBackgrounds.length).toBe(0)
  })

  it('renderiza hotspots navegables principales', () => {
    renderWithRouter()
    expect(screen.getByLabelText('Perfil').closest('a')).toHaveAttribute('href', '/profile')
    expect(screen.getByLabelText('Diario').closest('a')).toHaveAttribute('href', '/diary')
    expect(screen.getByLabelText('Minijuegos').closest('a')).toHaveAttribute('href', '/minigames')
    expect(screen.getByLabelText('Pendientes').closest('a')).toHaveAttribute('href', '/pending')
    expect(screen.getByLabelText('Retos').closest('a')).toHaveAttribute('href', '/challenges')
  })

  it('renderiza acceso a respiraciones guiadas desde las nubes', () => {
    renderWithRouter()
    const guidedBreathingLinks = screen.getAllByLabelText('Respiraciones guiadas')
    expect(guidedBreathingLinks.length).toBeGreaterThan(0)
    expect(guidedBreathingLinks[0].closest('a')).toHaveAttribute('href', '/guided-breathing')
  })

  it('redirige a diario al hacer click en el hotspot Diario', async () => {
    const user = userEvent.setup()

    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/']}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/diary" element={<h1>Vista Diario</h1>} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    )

    await user.click(screen.getByLabelText('Diario'))
    expect(screen.getByRole('heading', { name: 'Vista Diario' })).toBeInTheDocument()
  })

  it('permite alternar entre modo noche y modo dia', async () => {
    const user = userEvent.setup()
    renderWithRouter()

    const toggleButton = screen.getByRole('button', { name: 'Cambiar a modo noche' })
    await user.click(toggleButton)

    expect(screen.getByRole('button', { name: 'Cambiar a modo dia' })).toBeInTheDocument()
  })

  it('restringe accesos estimulantes en modo noche', async () => {
    const user = userEvent.setup()
    renderWithRouter()

    await user.click(screen.getByRole('button', { name: 'Cambiar a modo noche' }))

    const minijuegosButton = screen.getByRole('button', { name: 'Minijuegos' })
    const retosButton = screen.getByRole('button', { name: 'Retos' })
    const diarioButton = screen.getByRole('button', { name: 'Diario' })

    expect(minijuegosButton).toBeDisabled()
    expect(retosButton).toBeDisabled()
    expect(diarioButton).toBeDisabled()

    expect(screen.queryByRole('link', { name: 'Minijuegos' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Retos' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Diario' })).not.toBeInTheDocument()
  })

  it('renderiza el banco con la clase de espejo mobile', () => {
    renderWithRouter()

    const benchImage = screen.getByAltText('Banco con cuaderno en el jardin')
    expect(benchImage).toHaveClass('scene-element__image--mirror-mobile')
  })

  it('muestra el onboarding de bienvenida en el primer ingreso', () => {
    window.localStorage.removeItem('huly:home-onboarding-completed')

    renderWithRouter()

    expect(screen.getByLabelText('Tutorial de bienvenida')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Comenzar con el tutorial' })).toBeInTheDocument()
  })

  it('avanza el onboarding y lo marca como completado al finalizar', async () => {
    const user = userEvent.setup()
    window.localStorage.removeItem('huly:home-onboarding-completed')

    renderWithRouter()

    await user.click(screen.getByRole('button', { name: 'Comenzar con el tutorial' }))
    expect(screen.getByText('Casa')).toBeInTheDocument()

    for (let index = 0; index < 5; index += 1) {
      await user.click(screen.getByRole('button', { name: 'Siguiente' }))
    }

    await user.click(screen.getByRole('button', { name: 'Finalizar tutorial' }))

    expect(window.localStorage.getItem('huly:home-onboarding-completed')).toBe('true')
    expect(screen.queryByLabelText(/tutorial/i)).not.toBeInTheDocument()
  })
})
