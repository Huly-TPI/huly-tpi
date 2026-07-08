import { clearAllMocks, setupMockedPostResponse, setupMockedPutResponse } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { pushNotificationsApi } from '../../api/pushNotifications'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
    api: {
        post: vi.fn(),
        delete: vi.fn(),
        put: vi.fn(),
    },
}))

const mockedPost = vi.mocked(api.post)
const mockedDelete = vi.mocked(api.delete)
const mockedPut = vi.mocked(api.put)

describe('pushNotificationsApi', () => {
    beforeEach(() => {
        clearAllMocks()
    })

    it('subscribe llama POST /push/subscribe con la información correcta', () => {
        setupMockedPostResponse(undefined)
        return callSubscribe({
            userId: 1,
            endpoint: 'https://push.example.com/sub',
            p256dh: 'p256dh',
            auth: 'auth',
        }).then(() => {
            verifyPostCalledWith('/pushNotification/subscribe', {
                userId: 1,
                endpoint: 'https://push.example.com/sub',
                p256dh: 'p256dh',
                auth: 'auth',
            })
        })
    })

    it('unsubscribe llama a DELETE /push/unsubscribe con el endpoint en el body', () => {
        setupMockedDeleteResponse(undefined)
        return callUnsubscribe('https://push.example.com/sub').then(() => {
            verifyDeleteCalledWith('/pushNotification/unsubscribe', {
                body: { endpoint: 'https://push.example.com/sub' },
            })
        })
    })

    it('updateHour llama PUT /pushNotification/hour con la hora', () => {
        setupMockedPutResponse(undefined)
        return callUpdateHour(20).then(() => {
            verifyPutCalledWith('/pushNotification/hour', { hour: 20 })
        })
    })
    

    const setupMockedDeleteResponse = (response: any) => {
        mockedDelete.mockResolvedValueOnce(response as never)
    }

    

    const callSubscribe = (dto: any) => {
        return pushNotificationsApi.subscribe(dto)
    }

    const callUnsubscribe = (endpoint: string) => {
        return pushNotificationsApi.unsubscribe(endpoint)
    }

    const callUpdateHour = (hour: number) => {
        return pushNotificationsApi.updateHour(hour)
    }

    const verifyPostCalledWith = (url: string, body: any) => {
        expect(mockedPost).toHaveBeenCalledWith(url, body)
    }

    const verifyDeleteCalledWith = (url: string, options: any) => {
        expect(mockedDelete).toHaveBeenCalledWith(url, options)
    }

    const verifyPutCalledWith = (url: string, body: any) => {
        expect(mockedPut).toHaveBeenCalledWith(url, body)
    }
})