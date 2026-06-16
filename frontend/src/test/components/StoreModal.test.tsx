import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import StoreModal from '../../components/Shop/StoreModal'

const mockUseStoreItems = vi.fn()
const mockUseInventory = vi.fn()
const mockUseCosmeticActions = vi.fn()
const mockUseUserCoins = vi.fn()

vi.mock('../../hooks/store/useStoreItems', () => ({ useStoreItems: () => mockUseStoreItems() }))
vi.mock('../../hooks/store/useInventory', () => ({ useInventory: () => mockUseInventory() }))
vi.mock('../../hooks/store/useCosmeticActions', () => ({ useCosmeticActions: () => mockUseCosmeticActions() }))
vi.mock('../../hooks/shop/useUserCoins', () => ({ useUserCoins: () => mockUseUserCoins() }))

vi.mock('../../components/Shop/CosmeticCard', () => ({

     CosmeticCard: ({ item }: { item: { name: string } }) => (
    <div data-testid="cosmetic-card">{item.name}</div>
  ),
}))

const makeItem = (id: number, name: string) => ({
    id, 
    name, 
    description: 'desc',
    category: 'HOUSE',
    assetKey: 'k',
    priceCoins: 50
})

describe('StoreModal', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockUseStoreItems.mockReturnValue({ items: [], loading: false, error: null })
        mockUseInventory.mockReturnValue({ inventory: [], refetch: vi.fn() })
        mockUseCosmeticActions.mockReturnValue({ busyId: null, error: null, buy: vi.fn(), equip: vi.fn(), unequip: vi.fn() })
        mockUseUserCoins.mockReturnValue({ coins: 100, refresh: vi.fn() })
    })

    it('no renderiza nada cuando isOpen es false', () => {
        render(<StoreModal isOpen={false} onClose={() => {}} />)
        expect(screen.queryByText('Tienda')).not.toBeInTheDocument()
    })

    it('muestra el titulo y el saldo de monedas', () => {
        render(<StoreModal isOpen onClose={() => {}} />)
        expect(screen.getByText('Tienda')).toBeInTheDocument()
        expect(screen.getByText('100')).toBeInTheDocument()
    })

    it('renderiza una card por cada item', () => {
        mockUseStoreItems.mockReturnValue({ items: [makeItem(1, 'Casa rosa'), makeItem(2, 'Casa celeste')], loading: false, error: null })
        render(<StoreModal isOpen onClose={() => {}} />)

        expect(screen.getAllByTestId('cosmetic-card')).toHaveLength(2)
    })

    it('muestra loading mientras carga el catalogo', () => {
        mockUseStoreItems.mockReturnValue({ items: [], loading: true, error: null })
        render(<StoreModal isOpen onClose={() => {}} />)

        expect(screen.getByText('Cargando tienda...')).toBeInTheDocument()
    })

    it('muestra el error de una acción', () => {
        mockUseCosmeticActions.mockReturnValue({ busyId: null, error: 'Saldo de monedas insuficiente', buy: vi.fn(), equip: vi.fn() })
        render(<StoreModal isOpen onClose={() => {}} />)

        expect(screen.getByText('Saldo de monedas insuficiente')).toBeInTheDocument()
    })

    it('llama onClose al hacer click en la X', async () => {
        const onClose = vi.fn()
        render(<StoreModal isOpen onClose={onClose} />)

                await userEvent.click(screen.getByRole('button', { name: 'Cerrar' }))

        expect(onClose).toHaveBeenCalledOnce()
    })

})