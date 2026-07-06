import { clearAllMocks } from '../testHelpers'
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
        clearAllMocks()
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

    it('isSupported es true cuando el browser soporta push', () => {
        setupGetSubscriptionResolved(null)
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyIsSupported(true)
        })
    })

    it('isSuscribed es false cuando no hay suscripción existente', () => {
        setupGetStatusResolved({ subscribed: false, notificationHour: 9 })
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyIsSubscribed(false)
        })
    })

    it('isSuscribed es true cuando ya existe una suscripción', () => {
        setupGetStatusResolved({ subscribed: true, notificationHour: 9 })
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyIsSubscribed(true)
        })
    })

    it('pide permiso de notificaciones al suscribir', () => {
        setupGetSubscriptionResolved(null)
        setupBrowserSubscribeResolved(mockPushSub)
        setupApiSubscribeResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callSubscribe())
            .then(() => {
                verifyRequestPermissionCalled()
            })
    })

    it('no se suscribe si el usuario rechaza el permiso', () => {
        setupPermissionResolved('denied')
        setupGetSubscriptionResolved(null)
        setupHook()
        return waitForLoadingFinished()
            .then(() => callSubscribe())
            .then(() => {
                verifyBrowserSubscribeNotCalled()
                verifyApiSubscribeNotCalled()
                verifyIsSubscribed(false)
            })
    })

    it('suscribe llama a la api con userId, endpoint, p256dh y auth', () => {
        setupGetSubscriptionResolved(null)
        setupBrowserSubscribeResolved(mockPushSub)
        setupApiSubscribeResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callSubscribe())
            .then(() => {
                verifyApiSubscribeCalledWith({
                    userId: 42,
                    endpoint: 'https://push.example.com/sub',
                    p256dh: 'p256dhKey',
                    auth: 'authKey',
                })
                verifyIsSubscribed(true)
            })
    })

    it('reusa la suscripción existente sin volver a crear una nueva', () => {
        setupGetSubscriptionResolved(mockPushSub)
        setupApiSubscribeResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callSubscribe())
            .then(() => {
                verifyBrowserSubscribeNotCalled()
                verifyApiSubscribeCalledWith({
                    userId: 42,
                    endpoint: 'https://push.example.com/sub',
                    p256dh: 'p256dhKey',
                    auth: 'authKey',
                })
            })
    })

    it('unsubscribe llama a la api y al unsubscribe del browser', () => {
        setupGetSubscriptionResolved(mockPushSub)
        setupApiUnsubscribeResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callUnsubscribe())
            .then(() => {
                verifyApiUnsubscribeCalledWith('https://push.example.com/sub')
                verifyBrowserUnsubscribeCalled()
                verifyIsSubscribed(false)
            })
    })

    it('carga la hora de notificación del status', () => {
        setupGetStatusResolved({ subscribed: true, notificationHour: 20 })
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyNotificationHour(20)
        })
    })

    it('updateHour actualiza el estado y llama a la api', () => {
        setupUpdateHourResolved()
        setupHook()
        return waitForLoadingFinished()
            .then(() => callUpdateHour(21))
            .then(() => {
                verifyApiUpdateHourCalledWith(21)
                verifyNotificationHour(21)
            })
    })
    let rendered: ReturnType<typeof renderHook<ReturnType<typeof usePushNotifications>, undefined>>

    const setupHook = () => {
        rendered = renderHook(() => usePushNotifications())
    }

    const setupGetSubscriptionResolved = (sub: any) => {
        mockPushManager.getSubscription.mockResolvedValue(sub)
    }

    const setupBrowserSubscribeResolved = (sub: any) => {
        mockPushManager.subscribe.mockResolvedValue(sub)
    }

    const setupApiSubscribeResolved = () => {
        mockedSubscribe.mockResolvedValue(undefined as never)
    }

    const setupApiUnsubscribeResolved = () => {
        mockedUnsubscribe.mockResolvedValue(undefined as never)
    }

    const setupGetStatusResolved = (val: { subscribed: boolean; notificationHour: number }) => {
        mockedGetStatus.mockResolvedValue(val)
    }

    const setupPermissionResolved = (perm: string) => {
        mockRequestPermission.mockResolvedValue(perm)
    }

    const setupUpdateHourResolved = () => {
        mockedUpdateHour.mockResolvedValue(undefined as never)
    }

    const waitForLoadingFinished = () => {
        return waitFor(() => {
            expect(rendered.result.current.isLoading).toBe(false)
        })
    }

    const callSubscribe = () => {
        return act(() => rendered.result.current.subscribe())
    }

    const callUnsubscribe = () => {
        return act(() => rendered.result.current.unsubscribe())
    }

    const callUpdateHour = (hour: number) => {
        return act(() => rendered.result.current.updateHour(hour))
    }

    const verifyIsSupported = (expected: boolean) => {
        expect(rendered.result.current.isSupported).toBe(expected)
    }

    const verifyIsSubscribed = (expected: boolean) => {
        expect(rendered.result.current.isSubscribed).toBe(expected)
    }

    const verifyRequestPermissionCalled = () => {
        expect(mockRequestPermission).toHaveBeenCalled()
    }

    const verifyBrowserSubscribeNotCalled = () => {
        expect(mockPushManager.subscribe).not.toHaveBeenCalled()
    }

    const verifyApiSubscribeNotCalled = () => {
        expect(mockedSubscribe).not.toHaveBeenCalled()
    }

    const verifyApiSubscribeCalledWith = (expected: any) => {
        expect(mockedSubscribe).toHaveBeenCalledWith(expected)
    }

    const verifyApiUnsubscribeCalledWith = (endpoint: string) => {
        expect(mockedUnsubscribe).toHaveBeenCalledWith(endpoint)
    }

    const verifyBrowserUnsubscribeCalled = () => {
        expect(mockPushSub.unsubscribe).toHaveBeenCalled()
    }

    const verifyNotificationHour = (expected: number) => {
        expect(rendered.result.current.notificationHour).toBe(expected)
    }

    const verifyApiUpdateHourCalledWith = (hour: number) => {
        expect(mockedUpdateHour).toHaveBeenCalledWith(hour)
    }
})
