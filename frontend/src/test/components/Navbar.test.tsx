import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Navbar from '../../components/Navbar'


describe('Navbar', () => {
  const renderWithRouter = () => {
    return render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    )
  }

  it('renderiza el logo de Huly', () => {
    renderWithRouter()
    expect(screen.getByText('Huly')).toBeInTheDocument()
  })

  it('renderiza todos los links de navegación', () => {
    renderWithRouter()
    expect(screen.getByText('Inicio')).toBeInTheDocument()
    expect(screen.getByText('Diario emocional')).toBeInTheDocument()
    expect(screen.getByText('Minijuegos')).toBeInTheDocument()
  })

  it('cada link tiene el href correcto', () => {
    renderWithRouter()
    expect(screen.getByText('Diario emocional').closest('a')).toHaveAttribute('href', '/diary')
    expect(screen.getByText('Minijuegos').closest('a')).toHaveAttribute('href', '/minigames')
  })
})