import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import NotificationSettingsModal from '../../components/Profile/NotificationSettingsModal'
import { verifyTextPresent } from '../testHelpers'


const baseProps = {
    isSubscribed: true,
    isLoading: false,
    notificationHour: 9,
    onToggle: vi.fn(),
    onHourChange: vi.fn(),
    onClose: vi.fn(),
}

describe('NotificationSettingsModal', () => {
    let onToggleSpy: any
    let onHourChangeSpy: any

    it('muestra el estado activado y la hora seleccionada', () => {
        renderWithHour(16)
        verifyStatusText('Activados')
        verifySwitchChecked(true)
        verifySelectedHour('16')
    })

    it('llama a onToggle al hacer click en el switch', () => {
        renderWithToggleSpy()
        return clickSwitch().then(() => {
            verifyOnToggleCalledTimes(1)
        })
    })

    it('llama a onHourChange al cambiar la hora en el selector', () => {
        renderWithHourChangeSpy()
        return selectReminderHour('20').then(() => {
            verifyOnHourChangeCalledWith(20)
        })
    })

    it('deshabilita el selector cuando está desactivado', () => {
        renderDisabled()
        verifyStatusText('Desactivados')
        verifyReminderHourSelectorDisabled()
    })
    let user: any

    const renderWithHour = (hour: number) => {
        render(<NotificationSettingsModal {...baseProps} notificationHour={hour} />)
    }

    const renderWithToggleSpy = () => {
        user = userEvent.setup()
        onToggleSpy = vi.fn()
        render(<NotificationSettingsModal {...baseProps} onToggle={onToggleSpy} />)
    }

    const renderWithHourChangeSpy = () => {
        user = userEvent.setup()
        onHourChangeSpy = vi.fn()
        render(<NotificationSettingsModal {...baseProps} onHourChange={onHourChangeSpy} />)
    }

    const renderDisabled = () => {
        render(<NotificationSettingsModal {...baseProps} isSubscribed={false} />)
    }

    const verifyStatusText = (text: string) => {
        verifyTextPresent(text)
    }

    const verifySwitchChecked = (checked: boolean) => {
        if (checked) {
            expect(screen.getByRole('switch')).toBeChecked()
        } else {
            expect(screen.getByRole('switch')).not.toBeChecked()
        }
    }

    const verifySelectedHour = (hour: string) => {
        expect((screen.getByLabelText('Hora del recordatorio') as HTMLSelectElement).value).toBe(hour)
    }

    const clickSwitch = () => {
        return user.click(screen.getByRole('switch'))
    }

    const verifyOnToggleCalledTimes = (times: number) => {
        expect(onToggleSpy).toHaveBeenCalledTimes(times)
    }

    const selectReminderHour = (hour: string) => {
        return user.selectOptions(screen.getByLabelText('Hora del recordatorio'), hour)
    }

    const verifyOnHourChangeCalledWith = (hour: number) => {
        expect(onHourChangeSpy).toHaveBeenCalledWith(hour)
    }

    const verifyReminderHourSelectorDisabled = () => {
        expect(screen.getByLabelText('Hora del recordatorio')).toBeDisabled()
    }
})