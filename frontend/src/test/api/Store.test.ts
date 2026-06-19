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
        vi.clearAllMocks()
    })

    it('getItems llama a GET /store/items', async () => {
        mockedGet.mockResolvedValueOnce([] as never)
        await storeApi.getItems()
        expect(mockedGet).toHaveBeenCalledWith('/store/items')
    })

    it('getInventory llama a GET /store/inventory', async () => {
        mockedGet.mockResolvedValueOnce([] as never)
        await storeApi.getInventory()
        expect(mockedGet).toHaveBeenCalledWith('/store/inventory')
    })

    it('buy llama a POST /store/buy', async () => {
        mockedPost.mockResolvedValueOnce(undefined as never)
        await storeApi.buy(10)
        expect(mockedPost).toHaveBeenCalledWith('/store/items/10/buy', {})
    })

    it('equip llama a POST /store/equip', async () => {
        mockedPost.mockResolvedValueOnce(undefined as never)
        await storeApi.equip(10)
        expect(mockedPost).toHaveBeenCalledWith('/store/items/10/equip', {})
    })

    it('unequip llama a POST /store/items/:id/unequip', async () => {
        mockedPost.mockResolvedValueOnce(undefined as never)
        await storeApi.unequip(10)
        expect(mockedPost).toHaveBeenCalledWith('/store/items/10/unequip', {})
    })
})