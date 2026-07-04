import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import NotificationSettingsModal from '../../components/Profile/NotificationSettingsModal'

const baseProps = {
    isSubscribed: true,
    isLoading: false,
    notificationHour: 9,
    onToggle: vi.fn(),
    onHourChange: vi.fn(),
    onClose: vi.fn(),
}

describe('NotificationSettingsModal', () => {
    it('muestra el estado activado y la hora seleccionada', () => {
        render(<NotificationSettingsModal {...baseProps} notificationHour={16} />)
        expect(screen.getByText('Activados')).toBeInTheDocument()
        expect(screen.getByRole('switch')).toBeChecked()
        expect((screen.getByLabelText('Hora del recordatorio') as HTMLSelectElement).value).toBe('16')
    })

    it('llama onToggle al tocar el switch', async () => {
        const onToggle = vi.fn()
        render(<NotificationSettingsModal {...baseProps} onToggle={onToggle} />)
        await userEvent.click(screen.getByRole('switch'))
        expect(onToggle).toHaveBeenCalledOnce()
    })

    it('llama onHourChange al cambiar la hora', async () => {
        const onHourChange = vi.fn()
        render(<NotificationSettingsModal {...baseProps} onHourChange={onHourChange} />)
        await userEvent.selectOptions(screen.getByLabelText('Hora del recordatorio'), '20')
        expect(onHourChange).toHaveBeenCalledWith(20)
    })

    it('deshabilita el selector cuando está desactivado', () => {
        render(<NotificationSettingsModal {...baseProps} isSubscribed={false} />)
        expect(screen.getByText('Desactivados')).toBeInTheDocument()
        expect(screen.getByLabelText('Hora del recordatorio')).toBeDisabled()
    })
})