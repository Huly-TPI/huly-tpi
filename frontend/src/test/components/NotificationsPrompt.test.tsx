import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import NotificationsPrompt from '../../components/Notifications/NotificationsPrompt/NotificationsPrompt'
import { usePushNotifications } from '../../hooks/usePushNotifications'

vi.mock('../../hooks/usePushNotifications', () => ({
    usePushNotifications: vi.fn(),
}))

describe('NotificationsPrompt', () => {
    const onClose = vi.fn()
    const subscribe = vi.fn()

    const mockPush = (overrides = {}) => {
        vi.mocked(usePushNotifications).mockReturnValue({
            isSubscribed: false,
            isLoading: false,
            isSupported: true,
            subscribe,
            unsubscribe: vi.fn(),
            ...overrides,
        } as unknown as ReturnType<typeof usePushNotifications>)
    }

    beforeEach(() => {
        vi.clearAllMocks()
        mockPush()
    })

    it('no renderiza nada si el navegador no soporta push', () => {
        mockPush({ isSupported: false })
        const { container } = render(<NotificationsPrompt onClose={onClose} />)
        expect(container).toBeEmptyDOMElement()
    })

    it('no renderiza nada si el usuario ya tiene suscripción', () => {
        mockPush({ isSubscribed: true })
        const { container } = render(<NotificationsPrompt onClose={onClose} />)
        expect(container).toBeEmptyDOMElement()
    })

    it('muestra el cartel con el copy y los dos botones', () => {
        render(<NotificationsPrompt onClose={onClose} />)
        expect(screen.getByText(/te recordemos pasar por tu jardín/i)).toBeInTheDocument()
        expect(screen.getByRole('button', { name: /activar recordatorios/i })).toBeInTheDocument()
        expect(screen.getByRole('button', { name: /ahora no/i })).toBeInTheDocument()
    })

    it('al tocar "Activar recordatorios" llama a subscribe y luego cierra el cartel', async () => {
        const user = userEvent.setup()
        subscribe.mockResolvedValue(undefined)
        render(<NotificationsPrompt onClose={onClose} />)

        await user.click(screen.getByRole('button', { name: /activar recordatorios/i }))

        expect(subscribe).toHaveBeenCalledTimes(1)
        expect(onClose).toHaveBeenCalledTimes(1)    
    })

    it('al tocar "Ahora no" cierra sin suscribir', async () => {
        const user = userEvent.setup()
        render(<NotificationsPrompt onClose={onClose} />)

        await user.click(screen.getByRole('button', { name: /ahora no/i }))
        expect(onClose).toHaveBeenCalledTimes(1)
        expect(subscribe).not.toHaveBeenCalled()
    })
})