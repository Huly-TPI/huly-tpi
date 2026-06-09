import { describe, it, expect, vi, beforeEach } from 'vitest'
import { badgesApi } from '../../api/badges'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
    api: {
        get: vi.fn(),
    },
}))

const mockedGet = vi.mocked(api.get)

describe('badgesApi', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('getAll llama a GET /badges', async () => {
        mockedGet.mockResolvedValueOnce([] as never)
        await badgesApi.getAll()
        expect(mockedGet).toHaveBeenCalledWith('/badges')
    })

    it('getMyBadges llama a GET /badges/my', async () => {
        mockedGet.mockResolvedValueOnce([] as never)
        await badgesApi.getMyBadges()
        expect(mockedGet).toHaveBeenCalledWith('/badges/my')
    })
})