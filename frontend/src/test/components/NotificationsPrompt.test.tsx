import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import NotificationsPrompt from '../../components/Notifications/NotificationsPrompt/NotificationsPrompt'
import { usePushNotifications } from '../../hooks/usePushNotifications'
import { clickButton, verifyTextPresent, clearAllMocks } from '../testHelpers'


vi.mock('../../hooks/usePushNotifications', () => ({
    usePushNotifications: vi.fn(),
}))

describe('NotificationsPrompt', () => {
    let onCloseSpy: any
    let subscribeSpy: any

    beforeEach(() => {
        clearAllMocks()
        subscribeSpy = vi.fn()
        setupPushNotificationMock({ subscribe: subscribeSpy })
    })

    it('no renderiza nada si el navegador no soporta push', () => {
        setupPushNotificationMock({ isSupported: false })
        renderPrompt()
        verifyDOMIsEmpty()
    })

    it('no renderiza nada si el usuario ya está suscrito', () => {
        setupPushNotificationMock({ isSubscribed: true })
        renderPrompt()
        verifyDOMIsEmpty()
    })

    it('muestra el cartel con el copy y los dos botones', () => {
        renderPrompt()
        verifyPromptCopyPresent()
        verifySubscribeButtonPresent()
        verifyDismissButtonPresent()
    })

    it('llama a subscribe y a onClose al hacer click en activar recordatorios', () => {
        setupPushNotificationMock({ subscribe: subscribeSpy })
        setupSubscribeSuccess()
        renderPromptWithUser()
        return clickSubscribeButton().then(() => {
            verifySubscribeCalledTimes(1)
            verifyOnCloseCalledTimes(1)
        })
    })

    it('llama a onClose sin suscribir al hacer click en ahora no', () => {
        renderPromptWithUser()
        return clickDismissButton().then(() => {
            verifyOnCloseCalledTimes(1)
            verifySubscribeCalledTimes(0)
        })
    })
    let user: any
    let renderResult: any

    const setupPushNotificationMock = (overrides = {}) => {
        vi.mocked(usePushNotifications).mockReturnValue({
            isSubscribed: false,
            isLoading: false,
            isSupported: true,
            subscribe: subscribeSpy || vi.fn(),
            unsubscribe: vi.fn(),
            ...overrides,
        } as unknown as ReturnType<typeof usePushNotifications>)
    }

    const renderPrompt = () => {
        onCloseSpy = vi.fn()
        renderResult = render(<NotificationsPrompt onClose={onCloseSpy} />)
    }

    const renderPromptWithUser = () => {
        user = userEvent.setup()
        onCloseSpy = vi.fn()
        renderResult = render(<NotificationsPrompt onClose={onCloseSpy} />)
    }

    const verifyDOMIsEmpty = () => {
        expect(renderResult.container).toBeEmptyDOMElement()
    }

    const verifyPromptCopyPresent = () => {
        verifyTextPresent('¿Querés que te recordemos pasar por tu jardín?')
    }

    const verifySubscribeButtonPresent = () => {
        expect(screen.getByRole('button', { name: 'Activar recordatorios' })).toBeInTheDocument()
    }

    const verifyDismissButtonPresent = () => {
        expect(screen.getByRole('button', { name: 'Ahora no' })).toBeInTheDocument()
    }

    const setupSubscribeSuccess = () => {
        subscribeSpy.mockResolvedValue(undefined)
    }

    const clickSubscribeButton = () => {
        return clickButton(user, 'Activar recordatorios')
    }

    const clickDismissButton = () => {
        return clickButton(user, 'Ahora no')
    }

    const verifySubscribeCalledTimes = (times: number) => {
        expect(subscribeSpy).toHaveBeenCalledTimes(times)
    }

    const verifyOnCloseCalledTimes = (times: number) => {
        expect(onCloseSpy).toHaveBeenCalledTimes(times)
    }
})