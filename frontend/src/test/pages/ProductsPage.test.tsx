import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ProductsPage from '../../pages/Backoffice/ProductsPage'
import { verifyTextPresent, clearAllMocks } from '../testHelpers'

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
  let user: ReturnType<typeof userEvent.setup>
  let confirmSpy: any

  beforeEach(() => {
    clearAllMocks()
    mockHook.products = []
    mockHook.loading = false
    mockHook.error = null
    if (confirmSpy) {
      confirmSpy.mockRestore()
    }
  })

  it('muestra el mensaje de vacío cuando no hay productos', () => {
    renderProductsPage()
    verifyEmptyMessageVisible()
  })

  it('lista los productos', () => {
    setupProducts([product])
    renderProductsPage()
    verifyProductVisible('Casa rosa')
  })

  it('abre el formulario al tocar "Nuevo producto"', () => {
    renderProductsPage()
    return clickNewProductButton().then(() => {
      verifyProductFormVisible()
    })
  })

  it('elimina con confirmación', () => {
    setupProducts([product])
    setupConfirmSpy(true)
    renderProductsPage()
    return clickDeleteProductButton('Casa rosa').then(() => {
      verifyRemoveCalledWith(1)
    })
  })

  /* helpers */

  const renderProductsPage = () => {
    user = userEvent.setup()
    render(<ProductsPage />)
  }

  const setupProducts = (products: Product[]) => {
    mockHook.products = products
  }

  const setupConfirmSpy = (returnValue: boolean) => {
    confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(returnValue)
  }

  const verifyEmptyMessageVisible = () => {
    verifyTextPresent('Todavía no hay productos.')
  }

  const verifyProductVisible = (name: string) => {
    verifyTextPresent(name)
  }

  const clickNewProductButton = () => {
    return user.click(screen.getByRole('button', { name: 'Nuevo producto' }))
  }

  const verifyProductFormVisible = () => {
    expect(screen.getByTestId('product-form')).toBeInTheDocument()
  }

  const clickDeleteProductButton = (productName: string) => {
    return user.click(screen.getByRole('button', { name: `Eliminar ${productName}` }))
  }

  const verifyRemoveCalledWith = (id: number) => {
    expect(mockHook.remove).toHaveBeenCalledWith(id)
  }
})