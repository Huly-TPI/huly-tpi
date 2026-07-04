import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { usePushNotifications } from '../../hooks/usePushNotifications'
import { pushNotificationsApi } from '../../api/pushNotifications'

vi.mock('../../api/pushNotifications', () => ({
    pushNotificationsApi: {
        subscribe: vi.fn(),
        unsubscribe: vi.fn(),
        getStatus: vi.fn(),
        updateHour: vi.fn(),
    },
}))

vi.mock('../../context/auth', () => ({
    useAuth: vi.fn(() => ({
        user: { id: 42, name: 'Test', email: 'test@huly', role: 'USER' },
    })),
}))

const mockedSubscribe = vi.mocked(pushNotificationsApi.subscribe)
const mockedUnsubscribe = vi.mocked(pushNotificationsApi.unsubscribe)
const mockedGetStatus = vi.mocked(pushNotificationsApi.getStatus)
const mockRequestPermission = vi.fn()
const mockedUpdateHour = vi.mocked(pushNotificationsApi.updateHour)

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
        mockRequestPermission.mockResolvedValue('granted')
        mockedGetStatus.mockResolvedValue({ subscribed: false, notificationHour: 9 })
        mockPushManager.getSubscription.mockResolvedValue(null)
        Object.defineProperty(navigator, 'serviceWorker', { value: mockServiceWorker, configurable: true })
        Object.defineProperty(window, 'PushManager', { value: {}, configurable: true })
        Object.defineProperty(window, 'Notification', {
            value: { requestPermission: mockRequestPermission },
            configurable: true,
        })
    })


    afterEach(() => {
        Object.defineProperty(navigator, 'serviceWorker', { value: undefined, configurable: true })
        Object.defineProperty(window, 'PushManager', { value: undefined, configurable: true })
        Object.defineProperty(window, 'Notification', { value: undefined, configurable: true })
    })

    it('isSupported es true cuando el browser soporta push', async () => {
        mockPushManager.getSubscription.mockResolvedValue(null)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        expect(result.current.isSupported).toBe(true)
    })


    it('isSuscribed es false cuando no hay suscripción existente', async () => {
        mockedGetStatus.mockResolvedValue({ subscribed: false, notificationHour: 9 })
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        expect(result.current.isSubscribed).toBe(false)
    })

    it('isSuscribed es true cuando ya existe una suscripción', async () => {
        mockedGetStatus.mockResolvedValue({ subscribed: true, notificationHour: 9 })
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        expect(result.current.isSubscribed).toBe(true)
    })

    it('pide permiso de notificaciones al suscribir', async () => {
        mockPushManager.getSubscription.mockResolvedValue(null)
        mockPushManager.subscribe.mockResolvedValue(mockPushSub)
        mockedSubscribe.mockResolvedValue(undefined as never)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        await act(async () => { await result.current.subscribe() })

        expect(mockRequestPermission).toHaveBeenCalled()
    })

    it('no se suscribe si el usuario rechaza el permiso', async () => {
        mockRequestPermission.mockResolvedValue('denied')
        mockPushManager.getSubscription.mockResolvedValue(null)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        await act(async () => { await result.current.subscribe() })

        expect(mockPushManager.subscribe).not.toHaveBeenCalled()
        expect(mockedSubscribe).not.toHaveBeenCalled()
        expect(result.current.isSubscribed).toBe(false)
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

    it('reusa la suscripción existente sin volver a crear una nueva', async () => {
        mockPushManager.getSubscription.mockResolvedValue(mockPushSub)
        mockedSubscribe.mockResolvedValue(undefined as never)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        await act(async () => { await result.current.subscribe() })

        expect(mockPushManager.subscribe).not.toHaveBeenCalled()
        expect(mockedSubscribe).toHaveBeenCalledWith({
            userId: 42,
            endpoint: 'https://push.example.com/sub',
            p256dh: 'p256dhKey',
            auth: 'authKey',
        })
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

    it('carga la hora de notificación del status', async () => {
        mockedGetStatus.mockResolvedValue({ subscribed: true, notificationHour: 20 })
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))
        expect(result.current.notificationHour).toBe(20)
    })

    it('updateHour actualiza el estado y llama a la api', async () => {
        mockedUpdateHour.mockResolvedValue(undefined as never)
        const { result } = renderHook(() => usePushNotifications())
        await waitFor(() => expect(result.current.isLoading).toBe(false))

        await act(async () => { await result.current.updateHour(21) })

        expect(mockedUpdateHour).toHaveBeenCalledWith(21)
        expect(result.current.notificationHour).toBe(21)
    })

})
