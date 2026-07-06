import { forwardRef, useImperativeHandle } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Mandalas from '../../pages/Mandalas/Mandalas'
import { mandalaAssetByKey } from '../../components/Mandalas/mandalaAssets'
import { clickButton, verifyTextPresent } from '../testHelpers'

// --- SIMULACIONES GLOBALES (MOCKS) ---
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
  let user: ReturnType<typeof userEvent.setup>
  let rerenderMandalas: (ui: React.ReactElement) => void

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

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  it('muestra primero la galeria y abre la vista de pintura al elegir una mandala', () => {
    renderMandalasPage()
    verifyNoHeadingsPresent()
    verifyChooseMandalaButtonsLength(2)

    return clickOnChooseMandala(0).then(() => {
      return verifyCanvasIsPresent()
    }).then(() => {
      verifyActiveDayBackground()
      verifyEaselImageIsPresent()
      verifyActionButtonPresent('Ir a galeria')
      verifyActionButtonPresent('Descargar mandala pintada')
    })
  })

  it('vuelve desde la vista de pintura a la galeria', () => {
    renderMandalasPage()
    return clickOnChooseMandala(0)
      .then(() => verifyCanvasIsPresent())
      .then(() => clickMandalaButton('Ir a galeria'))
      .then(() => {
        verifyChooseMandalaButtonsLength(2)
      })
  })

  it('muestra estado de carga y error del catalogo backend', () => {
    setupCatalogLoadingState()
    renderMandalasPage()
    verifyCatalogLoadingState()

    setupCatalogErrorState()
    rerenderMandalasPage()
    verifyCatalogErrorState()
  })

  it('muestra AuthGateModal en vez del mensaje de error si el usuario no esta autenticado', () => {
    setupUnauthenticatedUser()
    renderMandalasPage()
    verifyNoAlertPresent()
    verifyTextPresent('¡Necesitás una cuenta!')
  })

  /* helpers */

  const renderMandalasPage = () => {
    user = userEvent.setup()
    const rendered = render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>
    )
    rerenderMandalas = rendered.rerender
  }

  const rerenderMandalasPage = () => {
    rerenderMandalas(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>
    )
  }

  const clickOnChooseMandala = async (index: number) => {
    const buttons = screen.getAllByRole('button', { name: (content) => content.includes('Elegir Mandala') })
    await user.click(buttons[index])
  }

  const clickMandalaButton = async (name: string | RegExp) => {
    await clickButton(user, name)
  }

  const setupCatalogLoadingState = () => {
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
  }

  const setupCatalogErrorState = () => {
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
  }

  const setupUnauthenticatedUser = () => {
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
  }

  const verifyNoHeadingsPresent = () => {
    expect(screen.queryByRole('heading')).not.toBeInTheDocument()
  }

  const verifyChooseMandalaButtonsLength = (expectedLength: number) => {
    expect(screen.getAllByRole('button', { name: (content) => content.includes('Elegir Mandala') })).toHaveLength(expectedLength)
  }

  const verifyCanvasIsPresent = async () => {
    expect(await screen.findByTestId('mandala-canvas')).toBeInTheDocument()
  }

  const verifyActiveDayBackground = () => {
    expect(screen.getByAltText('Fondo de dia para pintar mandalas')).toHaveClass('theme-background--active')
  }

  const verifyEaselImageIsPresent = () => {
    expect(document.querySelector('.mandala-easel-image')).toBeInTheDocument()
  }

  const verifyActionButtonPresent = (name: string | RegExp) => {
    expect(screen.getByRole('button', { name })).toBeInTheDocument()
  }

  const verifyCatalogLoadingState = () => {
    expect(screen.getByRole('status')).toHaveAccessibleName(/cargando mandalas/i)
    expect(screen.getByRole('status')).toBeEmptyDOMElement()
  }

  const verifyCatalogErrorState = () => {
    expect(screen.getByRole('alert')).toHaveAccessibleName(/no se pudieron cargar/i)
    expect(screen.getByRole('alert')).toBeEmptyDOMElement()
  }

  const verifyNoAlertPresent = () => {
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  }
})
