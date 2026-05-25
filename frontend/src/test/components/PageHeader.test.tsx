import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import PageHeader from '../../components/PageHeader'


describe('PageHeader', () => {
  it('renderiza el título y el emoji juntos', () => {
    render(<PageHeader title="Bienvenido" />)
    expect(screen.getByRole('heading')).toHaveTextContent('Bienvenido')
  })

  it('renderiza el subtítulo cuando se pasa', () => {
    render(<PageHeader title="Jardín" subtitle="Tu espacio personal" />)
    expect(screen.getByText('Tu espacio personal')).toBeInTheDocument()
  })

  it('no renderiza el subtítulo cuando no se pasa', () => {
    const { container } = render(<PageHeader title="Diario" />)
    const paragraphs = container.querySelectorAll('p')
    expect(paragraphs.length).toBe(0)
  })
})