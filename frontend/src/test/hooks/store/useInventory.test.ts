import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { useInventory } from '../../../hooks/store/useInventory'
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
    useAuth: () => ({ loading: false }),
}))

const mockedGetInventory = vi.mocked(storeApi.getInventory)

const makeOwned = (id: number, equipped: boolean) => ({
    storeItemId: id,
    name: 'Casa rosa',
    category: 'HOUSE',
    assetKey: 'casa-rosa',
    equipped,
})

describe('useInventory', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('carga el inventario', async () => {
        mockedGetInventory.mockResolvedValueOnce([makeOwned(10, false)] as never)
        const { result } = renderHook(() => useInventory())

        await waitFor(() => expect(result.current.loading).toBe(false))
        expect(result.current.inventory).toHaveLength(1)
        expect(result.current.inventory[0].equipped).toBe(false)
    })

    it('refetch vuelve a pedir el inventario', async () => {
        mockedGetInventory.mockResolvedValueOnce([] as never)
            .mockResolvedValueOnce([makeOwned(10, false)] as never)
        const { result } = renderHook(() => useInventory())

        await waitFor(() => expect(result.current.loading).toBe(false))
        expect(result.current.inventory).toHaveLength(0)

        await act(async () => { await result.current.refetch() })


        expect(result.current.inventory).toHaveLength(1)
        expect(mockedGetInventory).toHaveBeenCalledTimes(2)
    })
})