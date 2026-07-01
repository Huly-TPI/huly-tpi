import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import StoreModal from '../../components/Shop/StoreModal'

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

const setItems = (...items: object[]) => mockUseStoreItems.mockReturnValue({ items, loading: false, error: null })

const renderModal = () => render(<StoreModal isOpen onClose={() => { }} />)

const clickFilter = (name: string) => userEvent.click(screen.getByRole('button', { name }))

const mixedItems = () => [
    makeItem(1, 'Casa semillas', 'HOUSE'),
    makeItem(2, 'Casa premium', 'HOUSE', { premiumOnly: true }),
    makeItem(3, 'Casa dinero', 'HOUSE', { price: 1000 })
]

describe('StoreModal', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockUseStoreItems.mockReturnValue({ items: [], loading: false, error: null })
        mockUseInventory.mockReturnValue({ inventory: [], refetch: vi.fn() })
        mockUseCosmeticActions.mockReturnValue({ busyId: null, error: null, buy: vi.fn(), equip: vi.fn(), unequip: vi.fn() })
        mockUseUserCoins.mockReturnValue({ coins: 100, refresh: vi.fn() })
        mockUseMembership.mockReturnValue({ membership: { active: false, planCode: null } })
    })

    it('no renderiza nada cuando isOpen es false', () => {
        render(<StoreModal isOpen={false} onClose={() => { }} />)
        expect(screen.queryByText('Tienda')).not.toBeInTheDocument()
    })

    it('muestra el titulo y el saldo de semillas', () => {
        render(<StoreModal isOpen onClose={() => { }} />)
        expect(screen.getByText('Tienda')).toBeInTheDocument()
        expect(screen.getByText('100 semillas')).toBeInTheDocument()
    })

    it('renderiza una card por cada item', () => {
        mockUseStoreItems.mockReturnValue({ items: [makeItem(1, 'Casa rosa'), makeItem(2, 'Casa celeste')], loading: false, error: null })
        render(<StoreModal isOpen onClose={() => { }} />)

        expect(screen.getAllByTestId('cosmetic-card')).toHaveLength(2)
    })

    it('muestra loading mientras carga el catalogo', () => {
        mockUseStoreItems.mockReturnValue({ items: [], loading: true, error: null })
        render(<StoreModal isOpen onClose={() => { }} />)

        expect(screen.getByText('Cargando tienda...')).toBeInTheDocument()
    })

    it('muestra el error de una acción', () => {
        mockUseCosmeticActions.mockReturnValue({ busyId: null, error: 'Saldo de monedas insuficiente', buy: vi.fn(), equip: vi.fn() })
        render(<StoreModal isOpen onClose={() => { }} />)

        expect(screen.getByText('Saldo de monedas insuficiente')).toBeInTheDocument()
    })

    it('llama onClose al hacer click en la X', async () => {
        const onClose = vi.fn()
        render(<StoreModal isOpen onClose={onClose} />)

        await userEvent.click(screen.getByRole('button', { name: 'Cerrar' }))

        expect(onClose).toHaveBeenCalledOnce()
    })

    it('sin filtros activos muestra todos los items', () => {
        setItems(...mixedItems())
        renderModal()

        expect(screen.getAllByTestId('cosmetic-card')).toHaveLength(3)
    })

    it('filtrar por "Con dinero" mostrando solo items con preicos en pesos', async () => {
        setItems(...mixedItems())
        renderModal()

        await clickFilter('Con dinero')

        expect(screen.getByText('Casa dinero')).toBeInTheDocument()
        expect(screen.queryByText('Casa semillas')).not.toBeInTheDocument()
        expect(screen.queryByText('Casa premium')).not.toBeInTheDocument()
    })

    it('permite combinar varios filtros a la vez (semillas + premium)', async () => {
        setItems(...mixedItems())
        renderModal()

        await clickFilter('Con semillas')
        await clickFilter('Solo premium')

        expect(screen.getByText('Casa premium')).toBeInTheDocument()
        expect(screen.queryByText('Casa semillas')).toBeInTheDocument()
        expect(screen.queryByText('Casa dinero')).not.toBeInTheDocument()
    })

    it('muestra una pestaña por cada categoria con items', () => {
        setItems(
            makeItem(1, 'Casa rosa', 'HOUSE'),
            makeItem(2, 'Diario rosa', 'NOTEBOOK'),
            makeItem(3, 'Árbol sakura', 'TREE'),
        )
        renderModal()

        expect(screen.getByRole('tab', { name: 'Casas' })).toBeInTheDocument()
        expect(screen.getByRole('tab', { name: 'Diarios' })).toBeInTheDocument()
        expect(screen.getByRole('tab', { name: 'Árboles' })).toBeInTheDocument()
    })

    it('no muestra pestaña de categorias sin items', () => {
        setItems(makeItem(1, 'Casa rosa', 'HOUSE'))
        renderModal()

        expect(screen.getByRole('tab', { name: 'Casas' })).toBeInTheDocument()
        expect(screen.queryByRole('tab', { name: 'Diarios' })).not.toBeInTheDocument()
    })

    it('la pestaña "Todos" muestra items de todas las categorias', () => {
        setItems(
            makeItem(1, 'Casa rosa', 'HOUSE'),
            makeItem(2, 'Diario rosa', 'NOTEBOOK'),
        )
        renderModal()

        expect(screen.getByRole('tab', { name: 'Todos' })).toBeInTheDocument()
        expect(screen.getByText('Casa rosa')).toBeInTheDocument()
        expect(screen.getByText('Diario rosa')).toBeInTheDocument()
    })

    it('cambia de categoria al clickear otra pestaña', async () => {
        setItems(
            makeItem(1, 'Casa rosa', 'HOUSE'),
            makeItem(2, 'Diario rosa', 'NOTEBOOK'),
        )
        renderModal()

        await userEvent.click(screen.getByRole('tab', { name: 'Casas' }))

        expect(screen.getByText('Casa rosa')).toBeInTheDocument()
        expect(screen.queryByText('Diario rosa')).not.toBeInTheDocument()
    })
})