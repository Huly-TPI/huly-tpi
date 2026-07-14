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
    setupProducts([])
    mockHook.loading = false
    mockHook.error = null
  })

  it('muestra el vacío cuando no hay paquetes', () => {
    renderPage()
    verifyEmptyStateShown()
  })

  it('lista los paquetes', () => {
    setupProducts([product])
    renderPage()
    verifyProductNameShown('Pack Estándar')
  })

  it('abre el formulario al tocar "Nuevo paquete"', async () => {
    renderPage()
    await clickNewProduct()
    verifyNewProductFormOpened()
  })

  it('actualiza el estado con el botón Desactivar', async () => {
    setupProducts([product])
    renderPage()
    await clickActiveToggle('Pack Estándar')
    verifySetActiveCalledWith(1, false)
  })

  const renderPage = () => {
    render(<SeedProductsPage />)
  }

  const setupProducts = (products: P[]) => {
    mockHook.products = products
  }

  const verifyEmptyStateShown = () => {
    expect(screen.getByText('Todavía no hay paquetes.')).toBeInTheDocument()
  }

  const verifyProductNameShown = (name: string) => {
    expect(screen.getAllByText(name)[0]).toBeInTheDocument()
  }

  const clickNewProduct = async () => {
    await userEvent.click(screen.getByRole('button', { name: /Nuevo paquete/ }))
  }

  const verifyNewProductFormOpened = () => {
    expect(screen.getByText('Precio ($)')).toBeInTheDocument()
  }

  const clickActiveToggle = async (name: string) => {
    await userEvent.click(screen.getAllByRole('checkbox', { name: `Cambiar estado activo de ${name}` })[0])
  }

  const verifySetActiveCalledWith = (id: number, active: boolean) => {
    expect(mockHook.setActive).toHaveBeenCalledWith(id, active)
  }
})