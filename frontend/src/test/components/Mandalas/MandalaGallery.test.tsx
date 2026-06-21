import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import MandalaGallery from '../../../components/Mandalas/MandalaGallery'
import { mandalaCatalog } from '../../../components/Mandalas/mandalaCatalog'

describe('MandalaGallery', () => {
  it('renderiza cinco mandalas disponibles sin duplicar assets', () => {
    render(<MandalaGallery mandalas={mandalaCatalog} onSelectMandala={vi.fn()} />)

    expect(screen.getByRole('heading', { name: /elegí un mandala/i })).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(5)
    expect(screen.getAllByText('Gratis')).toHaveLength(5)
    expect(new Set(mandalaCatalog.map(mandala => mandala.src))).toHaveProperty('size', 5)
  })

  it('permite seleccionar cualquiera de las mandalas del catalogo', async () => {
    const user = userEvent.setup()
    const onSelectMandala = vi.fn()

    render(<MandalaGallery mandalas={mandalaCatalog} onSelectMandala={onSelectMandala} />)

    const paintButtons = screen.getAllByRole('button', { name: 'Pintar' })
    expect(paintButtons).toHaveLength(5)
    paintButtons.forEach(button => expect(button).toBeEnabled())

    await user.click(paintButtons[0])
    expect(onSelectMandala).toHaveBeenCalledWith(mandalaCatalog[0])
  })
})
