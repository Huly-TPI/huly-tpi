import { forwardRef, useImperativeHandle } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Mandalas from '../../pages/Mandalas/Mandalas'
import { mandalaAssetByKey } from '../../components/Mandalas/mandalaAssets'

const mockUseAvailableMandalas = vi.fn()
const mockUseMandalaProgress = vi.fn()
const mockUseAuth = vi.fn()

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
]

vi.mock('../../hooks/useAvailableMandalas', () => ({
  useAvailableMandalas: () => mockUseAvailableMandalas(),
}))

vi.mock('../../hooks/useMandalaProgress', () => ({
  useMandalaProgress: () => mockUseMandalaProgress(),
}))

vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('../../context/subscriptionModal', () => ({
  useSubscriptionModal: () => ({
    subscriptionOpen: false,
    openSubscriptionModal: vi.fn(),
    closeSubscriptionModal: vi.fn(),
  }),
}))

vi.mock('../../context/theme', () => ({
  useTheme: () => ({
    theme: 'light',
    setTheme: () => {},
    toggleTheme: () => {},
  }),
}))

vi.mock('../../components/Mandalas/MandalaCanvas', () => ({
  default: forwardRef(function MockMandalaCanvas(_, ref) {
    useImperativeHandle(ref, () => ({ clear: vi.fn(), exportImage: vi.fn() }))
    return <div data-testid="mandala-canvas" />
  }),
}))

describe('Mandalas page', () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { id: 1, name: 'Test User' },
    })
    mockUseAvailableMandalas.mockReturnValue({
      mandalas: availableMandalas,
      loading: false,
      error: null,
      page: 0,
      pageSize: 6,
      totalElements: availableMandalas.length,
      totalPages: 1,
      first: true,
      last: true,
      setPage: vi.fn(),
      refetch: vi.fn(),
    })
    mockUseMandalaProgress.mockReturnValue({
      clearProgress: vi.fn(),
      loading: false,
      paintBlob: null,
      saveProgress: vi.fn(),
    })
  })

  it('muestra primero la galeria y abre la vista de pintura al elegir una mandala', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.queryByRole('heading')).not.toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /elegir mandala/i })).toHaveLength(2)

    await user.click(screen.getAllByRole('button', { name: /elegir mandala/i })[0])

    expect(await screen.findByTestId('mandala-canvas')).toBeInTheDocument()
    expect(screen.getByAltText('Fondo de dia para pintar mandalas')).toHaveClass('theme-background--active')
    expect(document.querySelector('.mandala-easel-image')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /ir a galeria/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /descargar mandala pintada/i })).toBeInTheDocument()
  })

  it('vuelve desde la vista de pintura a la galeria', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    await user.click(screen.getAllByRole('button', { name: /elegir mandala/i })[0])
    await screen.findByTestId('mandala-canvas')
    await user.click(screen.getByRole('button', { name: /ir a galeria/i }))

    expect(screen.getAllByRole('button', { name: /elegir mandala/i })).toHaveLength(2)
  })

  it('muestra estado de carga y error del catalogo backend', () => {
    mockUseAvailableMandalas.mockReturnValue({
      mandalas: [],
      loading: true,
      error: null,
      page: 0,
      pageSize: 6,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
      setPage: vi.fn(),
      refetch: vi.fn(),
    })

    const { rerender } = render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.getByRole('status')).toHaveAccessibleName(/cargando mandalas/i)
    expect(screen.getByRole('status')).toBeEmptyDOMElement()

    mockUseAvailableMandalas.mockReturnValue({
      mandalas: [],
      loading: false,
      error: 'No se pudieron cargar las mandalas disponibles.',
      page: 0,
      pageSize: 6,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
      setPage: vi.fn(),
      refetch: vi.fn(),
    })

    rerender(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.getByRole('alert')).toHaveAccessibleName(/no se pudieron cargar/i)
    expect(screen.getByRole('alert')).toBeEmptyDOMElement()
  })

  it('muestra AuthGateModal en vez del mensaje de error si el usuario no esta autenticado', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
    })
    mockUseAvailableMandalas.mockReturnValue({
      mandalas: [],
      loading: false,
      error: 'No se pudieron cargar las mandalas disponibles.',
      page: 0,
      pageSize: 6,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
      setPage: vi.fn(),
      refetch: vi.fn(),
    })

    render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(screen.getByText('¡Necesitás una cuenta!')).toBeInTheDocument()
  })
})
