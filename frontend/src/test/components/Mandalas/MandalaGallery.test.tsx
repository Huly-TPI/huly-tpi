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
      accessType: 'free' as const,
      isLocked: false,
    },
    {
      id: 'mandala-13',
      title: 'Mandala 13',
      description: 'Mandala comprada.',
      src: mandalaAssetByKey['mandala-13'],
      accessStatus: 'available' as const,
      unlockSource: 'purchased' as const,
      accessType: 'purchasable' as const,
      isLocked: false,
    },
    {
      id: 'mandala-20',
      title: 'Mandala 20',
      description: 'Mandala incluida en suscripción.',
      src: mandalaAssetByKey['mandala-20'],
      accessStatus: 'included' as const,
      unlockSource: 'premiumPlan' as const,
      accessType: 'subscription' as const,
      isLocked: false,
    },
  ]

  it('mantiene disponibles los assets locales de las 21 mandalas', () => {
    expect(Object.keys(mandalaAssetByKey).sort()).toEqual(
      Array.from({ length: 21 }, (_, index) => `mandala-${String(index + 1).padStart(2, '0')}`),
    )
    expect(mandalaAssetByKey['mandala-21']).toBeTruthy()
  })

  it('renderiza solamente las mandalas disponibles recibidas', () => {
    render(
      <MandalaGallery
        first
        last
        mandalas={availableMandalas}
        onPageChange={vi.fn()}
        onSelectMandala={vi.fn()}
        page={0}
        totalPages={1}
      />,
    )

    expect(screen.queryByRole('heading')).not.toBeInTheDocument()
    expect(screen.getByText('Gratis')).toBeInTheDocument()
    expect(screen.getByText('Comprado')).toBeInTheDocument()
    expect(screen.getByText('Suscripción')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /elegir mandala/i })).toHaveLength(3)
    expect(new Set(availableMandalas.map(mandala => mandala.src))).toHaveProperty('size', 3)
  })

  it('permite seleccionar cualquiera de las mandalas del catalogo', async () => {
    const user = userEvent.setup()
    const onSelectMandala = vi.fn()

    render(
      <MandalaGallery
        first
        last
        mandalas={availableMandalas}
        onPageChange={vi.fn()}
        onSelectMandala={onSelectMandala}
        page={0}
        totalPages={1}
      />,
    )

    const mandalaButtons = screen.getAllByRole('button', { name: /elegir mandala/i })
    expect(mandalaButtons).toHaveLength(3)
    mandalaButtons.forEach(button => expect(button).toBeEnabled())

    await user.click(mandalaButtons[0])
    expect(onSelectMandala).toHaveBeenCalledWith(availableMandalas[0])
  })

  it('permite navegar entre paginas sin mostrar texto visible', async () => {
    const user = userEvent.setup()
    const onPageChange = vi.fn()

    render(
      <MandalaGallery
        first={false}
        last={false}
        mandalas={availableMandalas}
        onPageChange={onPageChange}
        onSelectMandala={vi.fn()}
        page={1}
        totalPages={3}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Página anterior' }))
    await user.click(screen.getByRole('button', { name: 'Página siguiente' }))

    expect(onPageChange).toHaveBeenNthCalledWith(1, 0)
    expect(onPageChange).toHaveBeenNthCalledWith(2, 2)
  })
})
