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
        setupProducts([])
        mockHook.loading = false
        mockHook.error = null
    })

    it('muestra el vacío cuando no hay planes', () => {
        renderPage()
        verifyEmptyStateShown()
    })

    it('lista los planes con su código', () => {
        setupProducts([plan])
        renderPage()
        verifyPlanNameShown('Premium')
        verifyPlanCodeShown('PREMIUM')
    })

    it('abre el formulario de edición', async () => {
        setupProducts([plan])
        renderPage()
        await clickEditPlan('Premium')
        verifyEditFormOpened()
    })

    it('modifica el estado con Desactivar', async () => {
        setupProducts([plan])
        renderPage()
        await clickActiveToggle('Premium')
        verifySetActiveCalledWith(1, false)
    })

    it('abre el formulario de nuevo plan (modo crear)', async () => {
        renderPage()
        await clickNewPlan()
        verifyNewPlanFormOpened()
    })

    it('crea un plan al completar y enviar', async () => {
        setupCreateResolved(true)
        renderPage()
        await clickNewPlan()
        await typeName('Premium Anual')
        await typeDescription('anual')
        await typePrice('9999')
        await clickCreateButton()
        verifyCreateCalled()
    })

    const renderPage = () => {
        render(<PlansAdminPage />)
    }

    const setupProducts = (products: P[]) => {
        mockHook.products = products
    }

    const setupCreateResolved = (val: any) => {
        mockHook.create.mockResolvedValue(val)
    }

    const verifyEmptyStateShown = () => {
        expect(screen.getByText('No hay planes.')).toBeInTheDocument()
    }

    const verifyPlanNameShown = (name: string) => {
        expect(screen.getAllByText(name)[0]).toBeInTheDocument()
    }

    const verifyPlanCodeShown = (code: string) => {
        expect(screen.getAllByText(code)[0]).toBeInTheDocument()
    }

    const clickEditPlan = async (name: string) => {
        await userEvent.click(screen.getAllByRole('button', { name: `Editar ${name}` })[0])
    }

    const verifyEditFormOpened = () => {
        expect(screen.getByText('Límite chat/día')).toBeInTheDocument()
    }

    const clickActiveToggle = async (name: string) => {
        await userEvent.click(screen.getAllByRole('checkbox', { name: `Cambiar estado activo de ${name}` })[0])
    }

    const verifySetActiveCalledWith = (id: number, active: boolean) => {
        expect(mockHook.setActive).toHaveBeenCalledWith(id, active)
    }

    const clickNewPlan = async () => {
        await userEvent.click(screen.getByRole('button', { name: /Nuevo plan/ }))
    }

    const verifyNewPlanFormOpened = () => {
        expect(screen.getByRole('combobox')).toBeInTheDocument()
    }

    const typeName = async (name: string) => {
        await userEvent.type(screen.getByLabelText('Nombre'), name)
    }

    const typeDescription = async (desc: string) => {
        await userEvent.type(screen.getByLabelText('Descripción'), desc)
    }

    const typePrice = async (price: string) => {
        await userEvent.type(screen.getByLabelText('Precio ($)'), price)
    }

    const clickCreateButton = async () => {
        await userEvent.click(screen.getByRole('button', { name: 'Crear' }))
    }

    const verifyCreateCalled = () => {
        expect(mockHook.create).toHaveBeenCalledOnce()
    }
})