import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { useStoreItems } from '../../../hooks/store/useStoreItems'
import { storeApi } from '../../../api/store'

vi.mock('../../../api/store', () => ({
    storeApi: {
        getItems: vi.fn(),
        getInventory: vi.fn(),
        buy: vi.fn(),
        equip: vi.fn(),
    },
}))

vi.mock('../../../context/auth', () => ({
    useAuth: () => ({loading: false}),
}))

const mockedGetItems = vi.mocked(storeApi.getItems)

const makeItem = (id: number) => ({
    id, 
    name: 'Casa rosa', 
    description: 'desc',
    category: 'HOUSE',
    assetKey: 'casa-rosa',
    priceCoins: 50
})

describe('useStoreItems', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('carga el catalogo de items', async () => {
        mockedGetItems.mockResolvedValueOnce([makeItem(10), makeItem(20)] as never)
        const { result } = renderHook(() => useStoreItems())
        await waitFor(() => expect(result.current.loading).toBe(false))
        expect(result.current.items).toHaveLength(2)
    })

    it('setea error si falla la carga' , async () => {
        mockedGetItems.mockRejectedValueOnce(new Error('red') as never)
        const { result } = renderHook(() => useStoreItems())
        await waitFor(() => expect(result.current.error).toBe("No se pudieron cargar los items de la tienda."))    
    
    })
})