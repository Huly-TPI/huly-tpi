import { describe, it, expect, vi, beforeEach } from 'vitest'
import { pushNotificationsApi } from '../../api/pushNotifications'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
    api: {
        post: vi.fn(),
        delete: vi.fn(),
    },
}))

const mockedPost = vi.mocked(api.post)
const mockedDelete = vi.mocked(api.delete)

describe('pushNotificationsApi', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('subscribe llama POST /push/subscribe con la información correcta', async () => {
        mockedPost.mockResolvedValueOnce(undefined as never)

        await pushNotificationsApi.subscribe({
            userId: 1,
            endpoint: 'https://push.example.com/sub',
            p256dh: 'p256dh',
            auth: 'auth',
        })

        expect(mockedPost).toHaveBeenCalledWith('/pushNotification/subscribe', {
            userId: 1,
            endpoint: 'https://push.example.com/sub',
            p256dh: 'p256dh',
            auth: 'auth',
        })
    })

    it('unsuscribe llama a DELETE /push/unsubscribe con el endpoint en el body', async () => {
        mockedDelete.mockResolvedValueOnce(undefined as never)
        await pushNotificationsApi.unsubscribe('https://push.example.com/sub')
        expect(mockedDelete).toHaveBeenCalledWith('/pushNotification/unsubscribe', {
            body: { endpoint: 'https://push.example.com/sub' },

        })
    })
})