import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PlansAdminPage from '../../pages/Backoffice/PlansAdminPage'

interface P { id: number; name: string; description: string; price: number; coinsAmount: number | null; type: string; planCode: string | null; chatDailyLimit: number | null; audioDailyLimit: number | null; active: boolean }

const mockHook = {
    products: [] as P[],
    loading: false,
    error: null as string | null,
    clearError: vi.fn(),
    reload: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    setActive: vi.fn(),
}

vi.mock('../../hooks/backoffice/useAdminProducts', () => ({
    useAdminProducts: () => mockHook,
}))

const plan: P = { id: 1, name: 'Premium', description: 'd', price: 9999, coinsAmount: 0, type: 'PLAN', planCode: 'PREMIUM', chatDailyLimit: 20, audioDailyLimit: 5, active: true }

describe('PlansAdminPage', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockHook.products = []
        mockHook.loading = false
        mockHook.error = null
    })

    it('muestra el vacío cuando no hay planes', () => {
        render(<PlansAdminPage />)
        expect(screen.getByText('No hay planes.')).toBeInTheDocument()
    })

    it('lista los planes con su código', () => {
        mockHook.products = [plan]
        render(<PlansAdminPage />)
        expect(screen.getByText('Premium')).toBeInTheDocument()
        expect(screen.getByText('PREMIUM')).toBeInTheDocument()
    })

    it('abre el formulario de edición', async () => {
        mockHook.products = [plan]
        render(<PlansAdminPage />)
        await userEvent.click(screen.getByRole('button', { name: 'Editar Premium' }))
        expect(screen.getByText('Límite chat/día')).toBeInTheDocument()
    })

    it('modifica el estado con Desactivar', async () => {
        mockHook.products = [plan]
        render(<PlansAdminPage />)
        await userEvent.click(screen.getByRole('button', { name: 'Desactivar Premium' }))
        expect(mockHook.setActive).toHaveBeenCalledWith(1, false)
    })

    it('abre el formulario de nuevo plan (modo crear)', async () => {
        render(<PlansAdminPage />)
        await userEvent.click(screen.getByRole('button', { name: /Nuevo plan/ }))
        expect(screen.getByRole('combobox')).toBeInTheDocument()
    })

    it('crea un plan al completar y enviar', async () => {
        mockHook.create.mockResolvedValue(true)
        render(<PlansAdminPage />)

        await userEvent.click(screen.getByRole('button', { name: /Nuevo plan/ }))
        await userEvent.type(screen.getByLabelText('Nombre'), 'Premium Anual')
        await userEvent.type(screen.getByLabelText('Descripción'), 'anual')
        await userEvent.type(screen.getByLabelText('Precio ($)'), '9999')
        await userEvent.click(screen.getByRole('button', { name: 'Crear' }))

        expect(mockHook.create).toHaveBeenCalledOnce()
    })
})