import Button from '../Buttons/Button/Button'

interface Props {
    isSubscribed: boolean
    isLoading: boolean
    notificationHour: number
    onToggle: () => void
    onHourChange: (hour: number) => void
    onClose: () => void
}

const HOURS = Array.from({ length: 24 }, (_, h) => h)
const pad = (h: number) => `${h.toString().padStart(2, '0')}:00`

export default function NotificationSettingsModal({
    isSubscribed, isLoading, notificationHour, onToggle, onHourChange, onClose,
}: Props) {
    return (
        <div className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] px-4 backdrop-blur-sm">
            <div
                role="dialog"
                aria-modal="true"
                aria-labelledby="notif-settings-title"
                className="relative my-8 w-full max-w-md rounded-2xl bg-[var(--surface-primary)] p-8 text-center text-[var(--text-primary)] shadow-xl"
            >
                <button
                    type="button"
                    onClick={onClose}
                    className="absolute right-4 top-4 text-xl font-bold text-[var(--text-secondary)] transition duration-150 hover:text-[var(--text-primary)]"
                    aria-label="Cerrar modal"
                >
                    &times;
                </button>

                <h2 id="notif-settings-title" className="mb-3 text-2xl font-bold text-[#8869AC] dark:text-violeta-claro">
                    Recordatorios diarios
                </h2>
                <p className="mb-6 text-left text-sm leading-relaxed text-[var(--text-secondary)]">
                    Te mandamos un mensajito  diario para que recuerdes por tu jardín. Elegí si querés recibirlos y a qué hora.
                </p>

                <div className="mb-6 space-y-4 border-y border-[var(--border-soft)] py-4 text-left">
                    <div className="flex items-center justify-between gap-4 rounded-xl bg-[var(--surface-secondary)] p-3">
                        <div>
                            <div className="text-sm font-semibold text-[var(--text-primary)]">Recordatorios</div>
                            <div className="mt-0.5 text-xs text-[var(--text-secondary)]">
                                {isSubscribed ? 'Activados' : 'Desactivados'}
                            </div>
                        </div>
                        <button
                            type="button"
                            role="switch"
                            aria-checked={isSubscribed}
                            aria-label={isSubscribed ? 'Desactivar recordatorios' : 'Activar recordatorios'}
                            onClick={onToggle}
                            disabled={isLoading}
                            className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors disabled:opacity-50 ${isSubscribed ? 'bg-[#8869AC]' : 'bg-gray-300 dark:bg-gray-600'}`}
                        >
                            <span className={`inline-block h-5 w-5 transform rounded-full bg-white shadow transition-transform ${isSubscribed ? 'translate-x-[22px]' : 'translate-x-0.5'}`} />
                        </button>
                    </div>

                    <div className={`rounded-xl bg-[var(--surface-secondary)] p-3 transition-opacity ${isSubscribed ? '' : 'pointer-events-none opacity-50'}`}>
                        <label htmlFor="notif-hour" className="text-sm font-semibold text-[var(--text-primary)]">
                            Hora del recordatorio
                        </label>
                        <p className="mt-0.5 mb-2 text-xs text-[var(--text-secondary)]">A qué hora te mandamos el mensajito.</p>
                        <select
                            id="notif-hour"
                            value={notificationHour}
                            onChange={e => onHourChange(Number(e.target.value))}
                            disabled={!isSubscribed || isLoading}
                            className="w-full rounded-lg border border-[var(--border-soft)] bg-[var(--surface-primary)] px-3 py-2 text-sm text-[var(--text-primary)]"
                        >
                            {HOURS.map(h => <option key={h} value={h}>{pad(h)}</option>)}
                        </select>
                    </div>
                </div>

                <Button variant="primary" fullWidth onClick={onClose}>Listo</Button>
            </div>
        </div>
    )
}