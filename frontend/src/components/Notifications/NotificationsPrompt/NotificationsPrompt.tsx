import { usePushNotifications } from '../../../hooks/usePushNotifications'
import { Bell } from 'lucide-react'
import './NotificationsPrompt.css'

interface NotificationsPromptProps {
    onClose: () => void
}

export default function NotificationsPrompt({ onClose }: NotificationsPromptProps) {
    const { isSupported, isSubscribed, isLoading, subscribe } = usePushNotifications()

    if (!isSupported || isSubscribed) return null

    const handleActivate = async () => {
        await subscribe()
        onClose()
    }

    return (
        <div className="notif-prompt-overlay" role="dialog" aria-modal="true" aria-labelledby="notif-prompt-title">
            <div className="notif-prompt-card">
                <Bell className="notif-prompt-icon" strokeWidth={2} aria-hidden="true" />
                <h2 id="notif-prompt-title" className="notif-prompt-title">
                    ¿Querés que te recordemos pasar por tu jardín?
                </h2>
                <p className="notif-prompt-text">
                    Te mandamos un mensajito suave cuando sea un buen momento para hacer una pausa.
                </p>
                <div className="notif-prompt-actions">
                    <button
                        type="button"
                        className="notif-prompt-btn notif-prompt-btn--primary"
                        onClick={handleActivate}
                        disabled={isLoading}
                    >
                        Activar recordatorios
                    </button>
                    <button
                        type="button"
                        className="notif-prompt-btn notif-prompt-btn--ghost"
                        onClick={onClose}
                        disabled={isLoading}
                    >
                        Ahora no
                    </button>
                </div>
            </div>
        </div>
    )
}