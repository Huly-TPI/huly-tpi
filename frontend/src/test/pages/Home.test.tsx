import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Home from '../../pages/Home/Home'

describe('Home', () => {
  const renderWithRouter = () => {
    return render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    )
  }

  it('renderiza el fondo del jardin', () => {
    renderWithRouter()
    expect(screen.getByAltText('Fondo diurno del jardín')).toBeInTheDocument()
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
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/diary" element={<h1>Vista Diario</h1>} />
        </Routes>
      </MemoryRouter>
    )

    await user.click(screen.getByLabelText('Diario'))
    expect(screen.getByRole('heading', { name: 'Vista Diario' })).toBeInTheDocument()
  })
})
