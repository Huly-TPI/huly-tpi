import { forwardRef, useImperativeHandle } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Mandalas from '../../pages/Mandalas/Mandalas'
import { mandalaAssetByKey } from '../../components/Mandalas/mandalaAssets'

const mockUseAvailableMandalas = vi.fn()
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

vi.mock('../../hooks/useAvailableMandalas', () => ({
  useAvailableMandalas: () => mockUseAvailableMandalas(),
}))

vi.mock('../../components/Mandalas/MandalaCanvas', () => ({
  default: forwardRef(function MockMandalaCanvas(_, ref) {
    useImperativeHandle(ref, () => ({ clear: vi.fn() }))
    return <div data-testid="mandala-canvas" />
  }),
}))

describe('Mandalas page', () => {
  beforeEach(() => {
    mockUseAvailableMandalas.mockReturnValue({
      mandalas: availableMandalas,
      loading: false,
      error: null,
      refetch: vi.fn(),
    })
  })

  it('muestra primero la galeria y abre la vista de pintura al elegir una mandala', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: /elegí un mandala/i })).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(2)

    await user.click(screen.getAllByRole('button', { name: 'Pintar' })[0])

    expect(await screen.findByTestId('mandala-canvas')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /elegir otro mandala/i })).toBeInTheDocument()
  })

  it('vuelve desde la vista de pintura a la galeria', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    await user.click(screen.getAllByRole('button', { name: 'Pintar' })[0])
    await screen.findByTestId('mandala-canvas')
    await user.click(screen.getByRole('button', { name: /elegir otro mandala/i }))

    expect(screen.getByRole('heading', { name: /elegí un mandala/i })).toBeInTheDocument()
  })

  it('muestra estado de carga y error del catalogo backend', () => {
    mockUseAvailableMandalas.mockReturnValue({
      mandalas: [],
      loading: true,
      error: null,
      refetch: vi.fn(),
    })

    const { rerender } = render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.getByRole('status')).toHaveTextContent(/cargando mandalas/i)

    mockUseAvailableMandalas.mockReturnValue({
      mandalas: [],
      loading: false,
      error: 'No se pudieron cargar las mandalas disponibles.',
      refetch: vi.fn(),
    })

    rerender(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.getByRole('alert')).toHaveTextContent(/no se pudieron cargar/i)
  })
})
