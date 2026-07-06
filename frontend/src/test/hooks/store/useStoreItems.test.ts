import { clearAllMocks } from '../../testHelpers'
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



describe('useStoreItems', () => {
    beforeEach(() => {
        clearAllMocks()
    })

    it('carga el catalogo de items', () => {
        setupGetItemsResolved([makeItem(10), makeItem(20)])
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyItemsLength(2)
        })
    })

    it('setea error si falla la carga' , () => {
        setupGetItemsRejected('red')
        setupHook()
        return waitForError('No se pudieron cargar los items de la tienda.')
    })
    let rendered: ReturnType<typeof renderHook<ReturnType<typeof useStoreItems>, undefined>>

    const setupHook = () => {
        rendered = renderHook(() => useStoreItems())
    }

    const setupGetItemsResolved = (items: any[]) => {
        mockedGetItems.mockResolvedValueOnce(items as never)
    }

    const setupGetItemsRejected = (msg: string) => {
        mockedGetItems.mockRejectedValueOnce(new Error(msg) as never)
    }

    const waitForLoadingFinished = () => {
        return waitFor(() => {
            expect(rendered.result.current.loading).toBe(false)
        })
    }

    const waitForError = (expectedError: string) => {
        return waitFor(() => {
            expect(rendered.result.current.error).toBe(expectedError)
        })
    }

    const verifyItemsLength = (length: number) => {
        expect(rendered.result.current.items).toHaveLength(length)
    }
})

function makeItem(id: number) {
  return ({
    id, 
    name: 'Casa rosa', 
    description: 'desc',
    category: 'HOUSE',
    assetKey: 'casa-rosa',
    priceCoins: 50
})
}
