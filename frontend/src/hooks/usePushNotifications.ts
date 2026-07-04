import { useState, useEffect, useCallback } from 'react'
import { pushNotificationsApi } from '../api/pushNotifications'
import { useAuth } from '../context/auth'

const VAPID_PUBLIC_KEY = import.meta.env.VITE_VAPID_PUBLIC_KEY as string

function urlBase64ToUint8Array(base64String: string): Uint8Array {
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4)
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/')
    const rawData = atob(base64)
    const output = new Uint8Array(rawData.length)
    for (let i = 0; i < rawData.length; i++) {
        output[i] = rawData.charCodeAt(i)
    }
    return output
}

export function usePushNotifications() {
    const { user } = useAuth()
    const isSupported =
        typeof window !== 'undefined' &&
        'serviceWorker' in navigator &&
        'PushManager' in window

    const [isSubscribed, setIsSubscribed] = useState(false)
    const [isLoading, setIsLoading] = useState(true)
    const [notificationHour, setNotificationHour] = useState(9)

    useEffect(() => {
        if (!isSupported) {
            setIsLoading(false)
            return
        }
        navigator.serviceWorker.register('/service-worker.js').catch(() => { })
        if (!user) {
            setIsSubscribed(false)
            setIsLoading(false)
            return
        }
        setIsLoading(true)
        pushNotificationsApi.getStatus()
            .then(status => {
                setIsSubscribed(status.subscribed)
                setNotificationHour(status.notificationHour)
            })
            .catch(() => { })
            .finally(() => setIsLoading(false))
    }, [isSupported, user?.id])

    const subscribe = useCallback(async () => {
        if (!user || !isSupported) return
        setIsLoading(true)
        try {
            const permission = await Notification.requestPermission()
            if (permission !== 'granted') return

            const reg = await navigator.serviceWorker.ready
            const existing = await reg.pushManager.getSubscription()
            const sub = existing ?? await reg.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY) as unknown as BufferSource,
            })
            const json = sub.toJSON() as {
                endpoint: string
                keys: { p256dh: string; auth: string }
            }
            await pushNotificationsApi.subscribe({
                userId: user.id,
                endpoint: json.endpoint,
                p256dh: json.keys.p256dh,
                auth: json.keys.auth,
            })
            setIsSubscribed(true)
        } catch (err) {
            console.error('Error al suscribirse a notificaciones', err)
        } finally {
            setIsLoading(false)
        }
    }, [user, isSupported])

    const unsubscribe = useCallback(async () => {
        if (!isSupported) return
        setIsLoading(true)
        try {
            const reg = await navigator.serviceWorker.ready
            const sub = await reg.pushManager.getSubscription()
            if (sub) {
                await pushNotificationsApi.unsubscribe(sub.endpoint)
                await sub.unsubscribe()
                setIsSubscribed(false)
            }
        } catch (err) {
            console.error('Error al desuscribirse de notificaciones', err)
        } finally {
            setIsLoading(false)
        }
    }, [isSupported])

    const updateHour = useCallback(async (hour: number) => {
        setNotificationHour(hour)
        try {
            await pushNotificationsApi.updateHour(hour)
        } catch (err) {
            console.error('Error al actualizar la hora de notificaciones', err)
        }
    }, [])

    return {
        isSubscribed,
        isLoading,
        isSupported,
        subscribe,
        unsubscribe,
        notificationHour,
        updateHour,
    }
}