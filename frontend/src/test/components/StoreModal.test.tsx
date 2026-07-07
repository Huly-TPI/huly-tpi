import { clearAllMocks, verifyTextPresent, verifyTextNotPresent } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import StoreModal from '../../components/Shop/StoreModal'

// --- SIMULACIONES GLOBALES (MOCKS) ---
const mockUseStoreItems = vi.fn()
const mockUseInventory = vi.fn()
const mockUseCosmeticActions = vi.fn()
const mockUseUserCoins = vi.fn()
const mockUseMembership = vi.fn()

vi.mock('../../hooks/store/useStoreItems', () => ({ useStoreItems: () => mockUseStoreItems() }))
vi.mock('../../hooks/store/useInventory', () => ({ useInventory: () => mockUseInventory() }))
vi.mock('../../hooks/store/useCosmeticActions', () => ({ useCosmeticActions: () => mockUseCosmeticActions() }))
vi.mock('../../hooks/shop/useUserCoins', () => ({ useUserCoins: () => mockUseUserCoins() }))
vi.mock('../../hooks/shop/useMembership', () => ({ useMembership: () => mockUseMembership() }))

vi.mock('../../components/Shop/CosmeticCard', () => ({
  CosmeticCard: ({ item }: { item: { name: string } }) => (
    <div data-testid="cosmetic-card">{item.name}</div>
  ),
}))

describe('StoreModal', () => {
  let user: ReturnType<typeof userEvent.setup>

  beforeEach(() => {
    clearAllMocks()
    setupUser()
    setupDefaultStoreItems()
    setupDefaultInventory()
    setupDefaultCosmeticActions()
    setupDefaultUserCoins(100)
    setupDefaultMembership()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  it('no renderiza nada cuando isOpen es false', () => {
    renderClosedModal()
    verifyModalNotVisible()
  })

  it('muestra el titulo y el saldo de semillas', () => {
    renderOpenModal()
    verifyTitleVisible()
    verifyCoinsBalance(100)
  })

  it('renderiza una card por cada item', () => {
    setupStoreItems([makeItem(1, 'Casa rosa'), makeItem(2, 'Casa celeste')])
    renderOpenModal()
    verifyCosmeticCardsCount(2)
  })

  it('muestra loading mientras carga el catalogo', () => {
    setupStoreItemsLoading()
    renderOpenModal()
    verifyLoadingMessageVisible()
  })

  it('muestra el error de una acción', () => {
    setupCosmeticActionsError('Saldo de monedas insuficiente')
    renderOpenModal()
    verifyErrorMessageVisible('Saldo de monedas insuficiente')
  })

  it('llama onClose al hacer click en la X', () => {
    const onClose = vi.fn()
    renderOpenModalWithOnClose(onClose)
    return clickCloseButton().then(() => {
      verifyOnCloseCalled(onClose)
    })
  })

  it('sin filtros activos muestra todos los items', () => {
    setupMixedStoreItems()
    renderOpenModal()
    verifyCosmeticCardsCount(3)
  })

  it('filtrar por "Con dinero" mostrando solo items con precios en pesos', () => {
    setupMixedStoreItems()
    renderOpenModal()
    return clickFilter('Con dinero').then(() => {
      verifyTextPresent('Casa dinero')
      verifyTextNotPresent('Casa semillas')
      verifyTextNotPresent('Casa premium')
    })
  })

  it('permite combinar varios filtros a la vez (semillas + premium)', () => {
    setupMixedStoreItems()
    renderOpenModal()
    return clickFilter('Con semillas')
      .then(() => clickFilter('Solo premium'))
      .then(() => {
        verifyTextPresent('Casa premium')
        verifyTextPresent('Casa semillas')
        verifyTextNotPresent('Casa dinero')
      })
  })

  it('muestra una pestaña por cada categoria con items', () => {
    setupStoreItems([
      makeItem(1, 'Casa rosa', 'HOUSE'),
      makeItem(2, 'Diario rosa', 'NOTEBOOK'),
      makeItem(3, 'Árbol sakura', 'TREE'),
    ])
    renderOpenModal()
    verifyTabVisible('Casas')
    verifyTabVisible('Diarios')
    verifyTabVisible('Árboles')
  })

  it('no muestra pestaña de categorias sin items', () => {
    setupStoreItems([makeItem(1, 'Casa rosa', 'HOUSE')])
    renderOpenModal()
    verifyTabVisible('Casas')
    verifyTabNotVisible('Diarios')
  })

  it('la pestaña "Todos" muestra items de todas las categorias', () => {
    setupStoreItems([
      makeItem(1, 'Casa rosa', 'HOUSE'),
      makeItem(2, 'Diario rosa', 'NOTEBOOK'),
    ])
    renderOpenModal()
    verifyTabVisible('Todos')
    verifyTextPresent('Casa rosa')
    verifyTextPresent('Diario rosa')
  })

  it('cambia de categoria al clickear otra pestaña', () => {
    setupStoreItems([
      makeItem(1, 'Casa rosa', 'HOUSE'),
      makeItem(2, 'Diario rosa', 'NOTEBOOK'),
    ])
    renderOpenModal()
    return clickTab('Casas').then(() => {
      verifyTextPresent('Casa rosa')
      verifyTextNotPresent('Diario rosa')
    })
  })

  /* helpers */

  const makeItem = (id: number, name: string, category = 'HOUSE', extra: object = {}) => ({
    id,
    name,
    description: 'desc',
    category,
    assetKey: 'k',
    priceCoins: 50,
    price: null,
    premiumOnly: false,
    ...extra,
  })

  const setupStoreItems = (items: any[]) => {
    mockUseStoreItems.mockReturnValue({ items, loading: false, error: null })
  }

  const setupStoreItemsLoading = () => {
    mockUseStoreItems.mockReturnValue({ items: [], loading: true, error: null })
  }

  const setupCosmeticActionsError = (error: string) => {
    mockUseCosmeticActions.mockReturnValue({ busyId: null, error, buy: vi.fn(), equip: vi.fn(), unequip: vi.fn() })
  }

  const setupMixedStoreItems = () => {
    setupStoreItems([
      makeItem(1, 'Casa semillas', 'HOUSE'),
      makeItem(2, 'Casa premium', 'HOUSE', { premiumOnly: true }),
      makeItem(3, 'Casa dinero', 'HOUSE', { price: 1000 })
    ])
  }

  const renderOpenModal = () => {
    render(<StoreModal isOpen onClose={() => { }} />)
  }

  const renderOpenModalWithOnClose = (onClose: () => void) => {
    render(<StoreModal isOpen onClose={onClose} />)
  }

  const renderClosedModal = () => {
    render(<StoreModal isOpen={false} onClose={() => { }} />)
  }

  const clickCloseButton = () => {
    return user.click(screen.getByRole('button', { name: 'Cerrar' }))
  }

  const clickFilter = (name: string) => {
    return user.click(screen.getByRole('button', { name }))
  }

  const clickTab = (name: string) => {
    return user.click(screen.getByRole('tab', { name }))
  }

  const verifyModalNotVisible = () => {
    expect(screen.queryByText('Tienda')).not.toBeInTheDocument()
  }

  const verifyTitleVisible = () => {
    expect(screen.getByText('Tienda')).toBeInTheDocument()
  }

  const verifyCoinsBalance = (coins: number) => {
    expect(screen.getByText(`${coins} semillas`)).toBeInTheDocument()
  }

  const verifyCosmeticCardsCount = (count: number) => {
    expect(screen.getAllByTestId('cosmetic-card')).toHaveLength(count)
  }

  const verifyLoadingMessageVisible = () => {
    expect(screen.getByText('Cargando tienda...')).toBeInTheDocument()
  }

  const verifyErrorMessageVisible = (msg: string) => {
    expect(screen.getByText(msg)).toBeInTheDocument()
  }

  const verifyOnCloseCalled = (onClose: any) => {
    expect(onClose).toHaveBeenCalledOnce()
  }

  const verifyTabVisible = (name: string) => {
    expect(screen.getByRole('tab', { name })).toBeInTheDocument()
  }

  const verifyTabNotVisible = (name: string) => {
    expect(screen.queryByRole('tab', { name })).not.toBeInTheDocument()
  }

  

  
  const setupUser = () => {
    user = userEvent.setup()
  }

  const setupDefaultStoreItems = () => {
    mockUseStoreItems.mockReturnValue({ items: [], loading: false, error: null })
  }

  const setupDefaultInventory = () => {
    mockUseInventory.mockReturnValue({ inventory: [], refetch: vi.fn() })
  }

  const setupDefaultCosmeticActions = () => {
    mockUseCosmeticActions.mockReturnValue({ busyId: null, error: null, buy: vi.fn(), equip: vi.fn(), unequip: vi.fn() })
  }

  const setupDefaultUserCoins = (coins: number) => {
    mockUseUserCoins.mockReturnValue({ coins, refresh: vi.fn() })
  }

  const setupDefaultMembership = () => {
    mockUseMembership.mockReturnValue({ membership: { active: false, planCode: null } })
  }
})