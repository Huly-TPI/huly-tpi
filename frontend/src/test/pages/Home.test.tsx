import { beforeEach, describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Home from '../../pages/Home/Home'
import { ThemeProvider } from '../../context/theme'

describe('Home', () => {
  beforeEach(() => {
    window.localStorage.clear()
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
    renderWithRouter()
    expect(screen.getByAltText('Fondo del jardin')).toBeInTheDocument()
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

    expect(screen.getByRole('button', { name: 'Minijuegos' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retos' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Diario' })).toBeInTheDocument()
  })
})
