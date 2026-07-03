import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ProductsPage from '../../pages/Backoffice/ProductsPage'

interface Product { id: number; name: string; description: string; category: string; assetKey: string | null; priceCoins: number; price: number | null; premiumOnly: boolean; imageUrlLight: string | null; imageUrlDark: string | null }

const mockHook = {
  products: [] as Product[],
  loading: false,
  error: null as string | null,
  clearError: vi.fn(),
  reload: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
  remove: vi.fn(),
}

vi.mock('../../hooks/backoffice/useStoreProducts', () => ({
  useStoreProducts: () => mockHook,
}))

vi.mock('../../components/backoffice/StoreProductForm', () => ({
  StoreProductForm: () => <div data-testid="product-form" />,
}))

const product: Product = {
  id: 1, name: 'Casa rosa', description: 'desc', category: 'HOUSE', assetKey: 'house-pink',
  priceCoins: 50, price: null, premiumOnly: false, imageUrlLight: null, imageUrlDark: null,
}

describe('ProductsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockHook.products = []
    mockHook.loading = false
    mockHook.error = null
  })

  it('muestra el mensaje de vacío cuando no hay productos', () => {
    render(<ProductsPage />)
    expect(screen.getByText('Todavía no hay productos.')).toBeInTheDocument()
  })

  it('lista los productos', () => {
    mockHook.products = [product]
    render(<ProductsPage />)
    expect(screen.getByText('Casa rosa')).toBeInTheDocument()
  })

  it('abre el formulario al tocar "Nuevo producto"', async () => {
    render(<ProductsPage />)
    await userEvent.click(screen.getByRole('button', { name: /Nuevo producto/ }))
    expect(screen.getByTestId('product-form')).toBeInTheDocument()
  })

  it('elimina con confirmación', async () => {
    mockHook.products = [product]
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    render(<ProductsPage />)
    await userEvent.click(screen.getByRole('button', { name: 'Eliminar Casa rosa' }))
    expect(mockHook.remove).toHaveBeenCalledWith(1)
  })
})