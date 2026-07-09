import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SeedProductsPage from '../../pages/Backoffice/SeedProductsPage'

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

const product: P = { id: 1, name: 'Pack Estándar', description: 'd', price: 1999, coinsAmount: 500, type: 'COIN_PACK', planCode: null, chatDailyLimit: null, audioDailyLimit: null, active: true }

describe('SeedProductsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockHook.products = []
    mockHook.loading = false
    mockHook.error = null
  })

  it('muestra el vacío cuando no hay paquetes', () => {
    render(<SeedProductsPage />)
    expect(screen.getByText('Todavía no hay paquetes.')).toBeInTheDocument()
  })

  it('lista los paquetes', () => {
    mockHook.products = [product]
    render(<SeedProductsPage />)
    expect(screen.getByText('Pack Estándar')).toBeInTheDocument()
  })

  it('abre el formulario al tocar "Nuevo paquete"', async () => {
    render(<SeedProductsPage />)
    await userEvent.click(screen.getByRole('button', { name: /Nuevo paquete/ }))
    expect(screen.getByText('Precio ($)')).toBeInTheDocument()
  })

  it('actualiza el estado con el botón Desactivar', async () => {
    mockHook.products = [product]
    render(<SeedProductsPage />)
    await userEvent.click(screen.getByRole('button', { name: 'Desactivar Pack Estándar' }))
    expect(mockHook.setActive).toHaveBeenCalledWith(1, false)
  })
})