import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import MandalaGallery from '../../../components/Mandalas/MandalaGallery'
import { mandalaAssetByKey } from '../../../components/Mandalas/mandalaAssets'

describe('MandalaGallery', () => {
  const availableMandalas = [
    {
      id: 'mandala-01',
      title: 'Mandala 01',
      description: 'Trazos circulares para pintar con calma.',
      src: mandalaAssetByKey['mandala-01'],
      accessStatus: 'available' as const,
      unlockSource: 'free' as const,
    },
    {
      id: 'mandala-13',
      title: 'Mandala 13',
      description: 'Mandala comprada.',
      src: mandalaAssetByKey['mandala-13'],
      accessStatus: 'available' as const,
      unlockSource: 'purchased' as const,
    },
  ]

  it('mantiene disponibles los assets locales de las 21 mandalas', () => {
    expect(Object.keys(mandalaAssetByKey).sort()).toEqual(
      Array.from({ length: 21 }, (_, index) => `mandala-${String(index + 1).padStart(2, '0')}`),
    )
    expect(mandalaAssetByKey['mandala-21']).toBeTruthy()
  })

  it('renderiza solamente las mandalas disponibles recibidas', () => {
    render(<MandalaGallery mandalas={availableMandalas} onSelectMandala={vi.fn()} />)

    expect(screen.getByRole('heading', { name: /elegí un mandala/i })).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(2)
    expect(screen.getByText('Gratis')).toBeInTheDocument()
    expect(screen.getByText('Tuya')).toBeInTheDocument()
    expect(new Set(availableMandalas.map(mandala => mandala.src))).toHaveProperty('size', 2)
  })

  it('permite seleccionar cualquiera de las mandalas del catalogo', async () => {
    const user = userEvent.setup()
    const onSelectMandala = vi.fn()

    render(<MandalaGallery mandalas={availableMandalas} onSelectMandala={onSelectMandala} />)

    const paintButtons = screen.getAllByRole('button', { name: 'Pintar' })
    expect(paintButtons).toHaveLength(2)
    paintButtons.forEach(button => expect(button).toBeEnabled())

    await user.click(paintButtons[0])
    expect(onSelectMandala).toHaveBeenCalledWith(availableMandalas[0])
  })
})
