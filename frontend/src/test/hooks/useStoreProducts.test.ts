import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { useStoreProducts } from '../../hooks/backoffice/useStoreProducts'

const getItems = vi.fn()
const create = vi.fn()
const update = vi.fn()
const remove = vi.fn()

vi.mock('../../api/store', () => ({ storeApi: { getItems: () => getItems() } }))
vi.mock('../../api/adminStore', () => ({
    adminStoreApi: {
        create: (...a: unknown[]) => create(...a),
        update: (...a: unknown[]) => update(...a),
        remove: (...a: unknown[]) => remove(...a),
    },
}))


const item = {
    id: 1, name: 'Casa', description: 'd', category: 'HOUSE', assetKey: null,
    priceCoins: 50, price: null, premiumOnly: false, imageUrlLight: null, imageUrlDark: null,
}


describe('useStoreProducts', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        getItems.mockResolvedValue([item])
    })


    it('carga los productos al montar', async () => {
        const { result } = renderHook(() => useStoreProducts())
        await waitFor(() => expect(result.current.loading).toBe(false))
        expect(result.current.products).toHaveLength(1)
    })

    it('create llama a la api, recarga y devuelve true', async () => {
        create.mockResolvedValue(undefined)
        const { result } = renderHook(() => useStoreProducts())
        await waitFor(() => expect(result.current.loading).toBe(false))

        let ok: boolean | undefined
        await act(async () => { ok = await result.current.create({} as never) })

        expect(create).toHaveBeenCalledOnce()
        expect(ok).toBe(true)
    })

    it('create setea error y devuelve false si falla', async () => {
        create.mockRejectedValue(new Error('boom'))
        const { result } = renderHook(() => useStoreProducts())
        await waitFor(() => expect(result.current.loading).toBe(false))

        let ok: boolean | undefined
        await act(async () => { ok = await result.current.create({} as never) })

        expect(ok).toBe(false)
        expect(result.current.error).toBe('boom')
    })

    it('remove llama a la api con el id', async () => {
        remove.mockResolvedValue(undefined)
        const { result } = renderHook(() => useStoreProducts())
        await waitFor(() => expect(result.current.loading).toBe(false))

        await act(async () => { await result.current.remove(1) })

        expect(remove).toHaveBeenCalledWith(1)
    })

    it('update llama a la api, recarga y devuelve true', async () => {
        update.mockResolvedValue(undefined)
        const { result } = renderHook(() => useStoreProducts())
        await waitFor(() => expect(result.current.loading).toBe(false))

        let ok: boolean | undefined
        await act(async () => { ok = await result.current.update(5, {} as never) })

        expect(update).toHaveBeenCalledWith(5, {})
        expect(ok).toBe(true)
    })

})
