import { clearAllMocks } from '../../testHelpers'
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



describe('useInventory', () => {
    beforeEach(() => {
        clearAllMocks()
    })

    it('carga el inventario', () => {
        setupGetInventoryOnce([makeOwned(10, false)])
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyInventoryLength(1)
            verifyEquippedAtIndex(0, false)
        })
    })

    it('refetch vuelve a pedir el inventario', () => {
        setupGetInventoryMultiple([], [makeOwned(10, false)])
        setupHook()
        return waitForLoadingFinished()
            .then(() => {
                verifyInventoryLength(0)
                return callRefetch()
            })
            .then(() => {
                verifyInventoryLength(1)
                verifyGetInventoryCalledTimes(2)
            })
    })
    let rendered: ReturnType<typeof renderHook<ReturnType<typeof useInventory>, undefined>>

    const setupHook = () => {
        rendered = renderHook(() => useInventory())
    }

    const setupGetInventoryOnce = (ownedItems: any[]) => {
        mockedGetInventory.mockResolvedValueOnce(ownedItems as never)
    }

    const setupGetInventoryMultiple = (firstRes: any[], secondRes: any[]) => {
        mockedGetInventory
            .mockResolvedValueOnce(firstRes as never)
            .mockResolvedValueOnce(secondRes as never)
    }

    const waitForLoadingFinished = () => {
        return waitFor(() => {
            expect(rendered.result.current.loading).toBe(false)
        })
    }

    const callRefetch = () => {
        return act(() => rendered.result.current.refetch())
    }

    const verifyInventoryLength = (length: number) => {
        expect(rendered.result.current.inventory).toHaveLength(length)
    }

    const verifyEquippedAtIndex = (index: number, equipped: boolean) => {
        expect(rendered.result.current.inventory[index].equipped).toBe(equipped)
    }

    const verifyGetInventoryCalledTimes = (times: number) => {
        expect(mockedGetInventory).toHaveBeenCalledTimes(times)
    }
})

function makeOwned(id: number, equipped: boolean) {
  return ({
    storeItemId: id,
    name: 'Casa rosa',
    category: 'HOUSE',
    assetKey: 'casa-rosa',
    equipped,
})
}
