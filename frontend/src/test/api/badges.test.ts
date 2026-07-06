import { clearAllMocks, setupMockedGetResponse } from '../testHelpers'
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
        clearAllMocks()
    })

    it('getAll llama a GET /badges', () => {
        setupMockedGetResponse([])
        return callGetAllBadges().then(() => {
            verifyGetCalledWith('/badges')
        })
    })

    it('getMyBadges llama a GET /badges/my', () => {
        setupMockedGetResponse([])
        return callGetMyBadges().then(() => {
            verifyGetCalledWith('/badges/my')
        })
    })
    

    const callGetAllBadges = () => {
        return badgesApi.getAll()
    }

    const callGetMyBadges = () => {
        return badgesApi.getMyBadges()
    }

    const verifyGetCalledWith = (url: string) => {
        expect(mockedGet).toHaveBeenCalledWith(url)
    }
})