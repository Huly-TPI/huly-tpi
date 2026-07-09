import { clearAllMocks } from '../testHelpers'
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
        clearAllMocks()
        getItems.mockResolvedValue([item])
    })

    it('carga los productos al montar', () => {
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyProductsLength(1)
        })
    })

    it('create llama a la api, recarga y devuelve true', () => {
        setupCreateResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callCreate({} as never))
            .then((ok) => {
                verifyCreateCalledOnce()
                verifyResultOk(ok, true)
            })
    })

    it('create setea error y devuelve false si falla', () => {
        setupCreateRejected('boom')
        setupHook()
        return waitForLoadingFinished()
            .then(() => callCreate({} as never))
            .then((ok) => {
                verifyResultOk(ok, false)
                verifyErrorMessage('boom')
            })
    })

    it('remove llama a la api con el id', () => {
        setupRemoveResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callRemove(1))
            .then(() => {
                verifyRemoveCalledWith(1)
            })
    })

    it('update llama a la api, recarga y devuelve true', () => {
        setupUpdateResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callUpdate(5, {} as never))
            .then((ok) => {
                verifyUpdateCalledWith(5, {})
                verifyResultOk(ok, true)
            })
    })
    let rendered: ReturnType<typeof renderHook<ReturnType<typeof useStoreProducts>, undefined>>

    const setupHook = () => {
        rendered = renderHook(() => useStoreProducts())
    }

    const setupCreateResolved = () => {
        create.mockResolvedValue(undefined)
    }

    const setupCreateRejected = (msg: string) => {
        create.mockRejectedValue(new Error(msg))
    }

    const setupRemoveResolved = () => {
        remove.mockResolvedValue(undefined)
    }

    const setupUpdateResolved = () => {
        update.mockResolvedValue(undefined)
    }

    const waitForLoadingFinished = () => {
        return waitFor(() => expect(rendered.result.current.loading).toBe(false))
    }

    const callCreate = (prod: any) => {
        return act(() => rendered.result.current.create(prod))
    }

    const callRemove = (id: number) => {
        return act(() => rendered.result.current.remove(id))
    }

    const callUpdate = (id: number, prod: any) => {
        return act(() => rendered.result.current.update(id, prod))
    }

    const verifyProductsLength = (len: number) => {
        expect(rendered.result.current.products).toHaveLength(len)
    }

    const verifyCreateCalledOnce = () => {
        expect(create).toHaveBeenCalledOnce()
    }

    const verifyResultOk = (ok: boolean, expected: boolean) => {
        expect(ok).toBe(expected)
    }

    const verifyErrorMessage = (msg: string) => {
        expect(rendered.result.current.error).toBe(msg)
    }

    const verifyRemoveCalledWith = (id: number) => {
        expect(remove).toHaveBeenCalledWith(id)
    }

    const verifyUpdateCalledWith = (id: number, prod: any) => {
        expect(update).toHaveBeenCalledWith(id, prod)
    }
})
