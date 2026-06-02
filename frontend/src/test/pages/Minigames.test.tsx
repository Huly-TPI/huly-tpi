import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Minigames from '../../pages/Minigames/Minigames'

describe('Minigames', () => {
  const renderWithRouter = () => {
    return render(
      <MemoryRouter>
        <Minigames />
      </MemoryRouter>,
    )
  }


  it('renderiza los fondos de escena desktop y mobile', () => {
    renderWithRouter()
    expect(screen.getByAltText('Fondo del jardin de minijuegos')).toBeInTheDocument()
    expect(screen.getByAltText('Fondo del jardin de minijuegos para celular')).toBeInTheDocument()
  })

  it('renderiza los hotspots de cada minijuego con su ruta', () => {
    renderWithRouter()
    expect(screen.getByLabelText('Burbujas').closest('a')).toHaveAttribute('href', '/bubbles')
    expect(screen.getByLabelText('Piedras del lago').closest('a')).toHaveAttribute('href', '/stones')
    expect(screen.getByLabelText('Colorear mandalas').closest('a')).toHaveAttribute('href', '/mandalas')
    expect(screen.getByLabelText('Arena zen').closest('a')).toHaveAttribute('href', '/free-draw')
  })

  it('renderiza las nubes navegables hacia la actividad de nubes', () => {
    renderWithRouter()
    const cloudLinks = screen.getAllByLabelText('Nubes que pasan')
    expect(cloudLinks.length).toBe(3)
    expect(cloudLinks[0].closest('a')).toHaveAttribute('href', '/clouds')
  })

  it('redirige a burbujas al hacer click en el hotspot del pez', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter initialEntries={['/minigames']}>
        <Routes>
          <Route path="/minigames" element={<Minigames />} />
          <Route path="/bubbles" element={<h1>Vista Burbujas</h1>} />
        </Routes>
      </MemoryRouter>,
    )

    await user.click(screen.getByLabelText('Burbujas'))
    expect(screen.getByRole('heading', { name: 'Vista Burbujas' })).toBeInTheDocument()
  })
})