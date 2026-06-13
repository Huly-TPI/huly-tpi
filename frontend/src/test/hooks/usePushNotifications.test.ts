import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { usePushNotifications } from '../../hooks/usePushNotifications'
import { pushNotificationsApi } from '../../api/pushNotifications'

vi.mock('../../api/pushNotifications', () => ({
    pushNotificationsApi: {
        subscribe: vi.fn(),
        unsubscribe: vi.fn(),
    },
}))

vi.mock('../../context/auth', () => ({
    useAuth: vi.fn(() => ({
        user: { id: 42, name: 'Test', email: 'test@huly', role: 'USER' },
    })),
}))

const mockedSubscribe = vi.mocked(pushNotificationsApi.subscribe)
const mockedUnsubscribe = vi.mocked(pushNotificationsApi.unsubscribe)

const mockPushSub = {
    endpoint: 'https://push.example.com/sub',
    toJSON: () => ({
        endpoint: 'https://push.example.com/sub',
        keys: { p256dh: 'p256dhKey', auth: 'authKey' },
    }),
    unsubscribe: vi.fn().mockResolvedValue(true),
}

const mockPushManager = {
    getSubscription: vi.fn(),
    subscribe: vi.fn(),
}

const mockRegistration = { pushManager: mockPushManager }

const mockServiceWorker = {
    register: vi.fn().mockResolvedValue(mockRegistration),
    get ready() { return Promise.resolve(mockRegistration) },
}

describe('usePushNotifications', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        Object.defineProperty(navigator, 'serviceWorker', { value: mockServiceWorker, configurable: true })
        Object.defineProperty(window, 'PushManager', { value: {}, configurable: true })
    })


    afterEach(() => {
        Object.defineProperty(navigator, 'serviceWorker', { value: undefined, configurable: true })
        Object.defineProperty(window, 'PushManager', { value: undefined, configurable: true })
    })

    it('isSupported es true cuando el browser soporta push', async () => {
        mockPushManager.getSubscription.mockResolvedValue(null)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        expect(result.current.isSupported).toBe(true)
    })

    it('isSupported es false cuando no hay suscripción existente', async () => {
        mockPushManager.getSubscription.mockResolvedValue(null)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        expect(result.current.isSubscribed).toBe(false)
    })

    it('isSuscribed es true cuando ya existe una suscripción', async () => {
        mockPushManager.getSubscription.mockResolvedValue(mockPushSub)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        expect(result.current.isSubscribed).toBe(true)
    })

    it('suscribe llama a la api con userId, endpoint, p256dh y auth', async () => {
        mockPushManager.getSubscription.mockResolvedValue(null)
        mockPushManager.subscribe.mockResolvedValue(mockPushSub)
        mockedSubscribe.mockResolvedValue(undefined as never)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        await act(async () => {
            await result.current.subscribe()
        })

        expect(mockedSubscribe).toHaveBeenCalledWith({
            userId: 42,
            endpoint: 'https://push.example.com/sub',
            p256dh: 'p256dhKey',
            auth: 'authKey',
        })

        expect(result.current.isSubscribed).toBe(true)
    })

    it('unsubscribe llama a la api y al unsubscribe del browser', async () => {
        mockPushManager.getSubscription.mockResolvedValue(mockPushSub)
        mockedUnsubscribe.mockResolvedValue(undefined as never)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        await act(async () => {
            await result.current.unsubscribe()
        })

        expect(mockedUnsubscribe).toHaveBeenCalledWith('https://push.example.com/sub')
        expect(mockPushSub.unsubscribe).toHaveBeenCalled()
        expect(result.current.isSubscribed).toBe(false)
    })

})
