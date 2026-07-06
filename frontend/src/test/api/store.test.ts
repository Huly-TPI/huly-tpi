import { clearAllMocks, setupMockedGetResponse, setupMockedPostResponse } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { storeApi } from '../../api/store'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
    api: {
        get: vi.fn(),
        post: vi.fn(),
    },
}))

const mockedGet = vi.mocked(api.get)
const mockedPost = vi.mocked(api.post)

describe('storeApi', () => {
    beforeEach(() => {
        clearAllMocks()
    })

    it('getItems llama a GET /store/items', () => {
        setupMockedGetResponse([])
        return callGetItems().then(() => {
            verifyGetCalledWith('/store/items')
        })
    })

    it('getInventory llama a GET /store/inventory', () => {
        setupMockedGetResponse([])
        return callGetInventory().then(() => {
            verifyGetCalledWith('/store/inventory')
        })
    })

    it('buy llama a POST /store/buy', () => {
        setupMockedPostResponse(undefined)
        return callBuy(10).then(() => {
            verifyPostCalledWith('/store/items/10/buy', {})
        })
    })

    it('equip llama a POST /store/equip', () => {
        setupMockedPostResponse(undefined)
        return callEquip(10).then(() => {
            verifyPostCalledWith('/store/items/10/equip', {})
        })
    })

    it('unequip llama a POST /store/items/:id/unequip', () => {
        setupMockedPostResponse(undefined)
        return callUnequip(10).then(() => {
            verifyPostCalledWith('/store/items/10/unequip', {})
        })
    })
    

    

    const callGetItems = () => {
        return storeApi.getItems()
    }

    const callGetInventory = () => {
        return storeApi.getInventory()
    }

    const callBuy = (id: number) => {
        return storeApi.buy(id)
    }

    const callEquip = (id: number) => {
        return storeApi.equip(id)
    }

    const callUnequip = (id: number) => {
        return storeApi.unequip(id)
    }

    const verifyGetCalledWith = (url: string) => {
        expect(mockedGet).toHaveBeenCalledWith(url)
    }

    const verifyPostCalledWith = (url: string, body: any) => {
        expect(mockedPost).toHaveBeenCalledWith(url, body)
    }
})