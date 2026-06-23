import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SeedShopModal from '../../components/SeedShopModal/SeedShopModal'
import type { Product } from '../../api/payment'

/* ─── Mocks ─── */

const mockBuy = vi.fn()
const mockMarkPending = vi.fn()

const mockUseProducts = vi.fn<[], {
  products: Product[]
  loading: boolean
  error: string | null
}>()

const mockUsePurchase = vi.fn<[], {
  buyingId: string | null
  error: string | null
  buy: typeof mockBuy
}>()

const mockUseUserCoins = vi.fn<[], {
  coins: number | null
  refresh: () => void
}>()

vi.mock('../../hooks/shop/useProducts', () => ({
  useProducts: () => mockUseProducts(),
}))

vi.mock('../../hooks/shop/usePurchase', () => ({
  usePurchase: () => mockUsePurchase(),
}))

vi.mock('../../hooks/shop/useUserCoins', () => ({
  useUserCoins: () => mockUseUserCoins(),
}))

vi.mock('../../hooks/shop/useRefreshOnReturn', () => ({
  useRefreshOnReturn: () => mockMarkPending,
}))

/* ─── Test Data ─── */

const PRODUCTS: Product[] = [
  { id: 'prod-1', name: 'Ramo de semillas', description: 'Un pequeño ramo para empezar', price: 500, coinsAmount: 50 },
  { id: 'prod-2', name: 'Jardín de semillas', description: 'Un jardín completo de semillas', price: 900, coinsAmount: 100 },
  { id: 'prod-3', name: 'Gran cosecha', description: 'La cosecha más abundante', price: 1500, coinsAmount: 200 },
]

function createProductsHookReturn(overrides: Partial<ReturnType<typeof mockUseProducts>> = {}) {
  return {
    products: PRODUCTS,
    loading: false,
    error: null,
    ...overrides,
  }
}

function createPurchaseHookReturn(overrides: Partial<ReturnType<typeof mockUsePurchase>> = {}) {
  return {
    buyingId: null,
    error: null,
    buy: mockBuy,
    ...overrides,
  }
}

/* ─── Setup Helpers ─── */

const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
}

function renderModal(propOverrides: Partial<typeof defaultProps> = {}) {
  const props = { ...defaultProps, ...propOverrides }
  return render(<SeedShopModal {...props} />)
}

function renderModalWithUser(propOverrides: Partial<typeof defaultProps> = {}) {
  const user = userEvent.setup()
  renderModal(propOverrides)
  return user
}

/* ─── Query Helpers ─── */

function queryModal() {
  return screen.queryByRole('dialog', { name: /tienda de semillas/i })
}

function getModal() {
  return screen.getByRole('dialog', { name: /tienda de semillas/i })
}

function getTitle() {
  return screen.getByText('Tienda de semillas')
}

function getSubtitle() {
  return screen.getByText(/comprá semillas para decorar tu jardín/i)
}

function getAllBuyButtons() {
  return screen.getAllByRole('button', { name: /comprar/i })
}

function getBuyButtonForProduct(productIndex: number) {
  return getAllBuyButtons()[productIndex]
}

function queryProcessingButton() {
  return screen.queryByRole('button', { name: /procesando/i })
}

function getCloseButton() {
  return screen.getByRole('button', { name: /cerrar tienda de semillas/i })
}

function getLoadingSpinner() {
  return document.querySelector('.animate-spin')
}

function getErrorMessage(message: string) {
  return screen.getByText(message)
}

function getProductName(name: string) {
  return screen.getByText(name)
}

function getSeedsBadge(amount: number) {
  return screen.getByText(new RegExp(`${amount.toLocaleString('es-AR')} semillas`))
}

function getFeaturedRibbon() {
  return screen.getByText(/mejor valor/i)
}

function getTrustMessageSecure() {
  return screen.getByText(/pago seguro y único/i)
}

function getTrustMessageInstant() {
  return screen.getByText(/tus semillas se agregan al instante/i)
}

/* ─── Interaction Helpers ─── */

async function clickBuyButton(user: ReturnType<typeof userEvent.setup>, productIndex = 0) {
  await user.click(getBuyButtonForProduct(productIndex))
}

async function clickCloseButton(user: ReturnType<typeof userEvent.setup>) {
  await user.click(getCloseButton())
}

/* ─── Assertion Helpers ─── */

function expectModalToBeVisible() {
  expect(getModal()).toBeInTheDocument()
}

function expectModalNotToBeRendered() {
  expect(queryModal()).not.toBeInTheDocument()
}

function expectTitleAndSubtitleToBeVisible() {
  expect(getTitle()).toBeInTheDocument()
  expect(getSubtitle()).toBeInTheDocument()
}

function expectAllProductNamesToBeVisible() {
  PRODUCTS.forEach(p => expect(getProductName(p.name)).toBeInTheDocument())
}

function expectAllSeedsBadgesToBeVisible() {
  PRODUCTS.forEach(p => expect(getSeedsBadge(p.coinsAmount)).toBeInTheDocument())
}

function expectAllDescriptionsToBeVisible() {
  PRODUCTS.forEach(p => expect(screen.getByText(p.description)).toBeInTheDocument())
}

function expectLoadingSpinnerToBeVisible() {
  expect(getLoadingSpinner()).toBeInTheDocument()
}

function expectLoadingSpinnerNotToBeVisible() {
  expect(getLoadingSpinner()).not.toBeInTheDocument()
}

function expectBuyButtonsCount(count: number) {
  expect(getAllBuyButtons()).toHaveLength(count)
}

function expectProcessingButtonToBeVisible() {
  const processingBtn = queryProcessingButton()
  expect(processingBtn).toBeInTheDocument()
}

function expectBuyButtonsToBeDisabled() {
  getAllBuyButtons().forEach(btn => expect(btn).toBeDisabled())
}

function expectErrorToBeVisible(message: string) {
  expect(getErrorMessage(message)).toBeInTheDocument()
}

function expectCallbackCalledOnce(callback: ReturnType<typeof vi.fn>) {
  expect(callback).toHaveBeenCalledOnce()
}

function expectCallbackCalledWith(callback: ReturnType<typeof vi.fn>, ...args: unknown[]) {
  expect(callback).toHaveBeenCalledWith(...args)
}

/* ─── Tests ─── */

describe('SeedShopModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseProducts.mockReturnValue(createProductsHookReturn())
    mockUsePurchase.mockReturnValue(createPurchaseHookReturn())
    mockUseUserCoins.mockReturnValue({ coins: 150, refresh: vi.fn() })
  })

  describe('visibilidad', () => {
    it('no renderiza nada cuando isOpen es false', () => {
      renderModal({ isOpen: false })
      expectModalNotToBeRendered()
    })

    it('renderiza el modal cuando isOpen es true', () => {
      renderModal()
      expectModalToBeVisible()
    })
  })

  describe('contenido del header', () => {
    it('muestra el título y subtítulo', () => {
      renderModal()
      expectTitleAndSubtitleToBeVisible()
    })

  })

  describe('cards de productos', () => {
    it('muestra los nombres de todos los productos', () => {
      renderModal()
      expectAllProductNamesToBeVisible()
    })

    it('muestra la cantidad de semillas en cada card', () => {
      renderModal()
      expectAllSeedsBadgesToBeVisible()
    })

    it('muestra la descripción de cada producto', () => {
      renderModal()
      expectAllDescriptionsToBeVisible()
    })

    it('muestra el precio en ARS de cada producto', () => {
      renderModal()
      PRODUCTS.forEach(p => {
        expect(screen.getByText(new RegExp(`\\$${p.price.toLocaleString('es-AR')}`))).toBeInTheDocument()
      })
    })

    it('marca el último producto como "Mejor valor"', () => {
      renderModal()
      expect(getFeaturedRibbon()).toBeInTheDocument()
    })
  })

  describe('estado de carga', () => {
    it('muestra el spinner mientras carga', () => {
      mockUseProducts.mockReturnValue(createProductsHookReturn({ loading: true }))
      renderModal()
      expectLoadingSpinnerToBeVisible()
    })

    it('no muestra el spinner cuando termina de cargar', () => {
      renderModal()
      expectLoadingSpinnerNotToBeVisible()
    })

    it('no muestra las cards mientras carga', () => {
      mockUseProducts.mockReturnValue(createProductsHookReturn({ loading: true }))
      renderModal()
      expect(screen.queryByText(PRODUCTS[0].name)).not.toBeInTheDocument()
    })
  })

  describe('compra de semillas', () => {
    it('muestra un botón Comprar por cada producto', () => {
      renderModal()
      expectBuyButtonsCount(PRODUCTS.length)
    })

    it('llama a buy con el id del producto al hacer click en Comprar', async () => {
      const user = renderModalWithUser()
      await clickBuyButton(user, 0)
      expectCallbackCalledWith(mockBuy, PRODUCTS[0].id)
    })

    it('llama a buy con el id correcto al comprar el segundo producto', async () => {
      const user = renderModalWithUser()
      await clickBuyButton(user, 1)
      expectCallbackCalledWith(mockBuy, PRODUCTS[1].id)
    })

    it('deshabilita todos los botones cuando hay una compra en proceso', () => {
      mockUsePurchase.mockReturnValue(createPurchaseHookReturn({ buyingId: 'prod-1' }))
      renderModal()
      expectBuyButtonsToBeDisabled()
    })

    it('muestra "Procesando…" en el botón del producto que se está comprando', () => {
      mockUsePurchase.mockReturnValue(createPurchaseHookReturn({ buyingId: 'prod-2' }))
      renderModal()
      expectProcessingButtonToBeVisible()
    })
  })

  describe('manejo de errores', () => {
    it('muestra el error cuando falla la carga de productos', () => {
      const errorMsg = 'No se pudieron cargar los productos.'
      mockUseProducts.mockReturnValue(createProductsHookReturn({ error: errorMsg }))
      renderModal()
      expectErrorToBeVisible(errorMsg)
    })

    it('muestra el error cuando falla la compra', () => {
      const errorMsg = 'Error al iniciar el pago. Intentá de nuevo.'
      mockUsePurchase.mockReturnValue(createPurchaseHookReturn({ error: errorMsg }))
      renderModal()
      expectErrorToBeVisible(errorMsg)
    })
  })

  describe('mensaje de confianza', () => {
    it('muestra el mensaje de pago seguro', () => {
      renderModal()
      expect(getTrustMessageSecure()).toBeInTheDocument()
    })

    it('muestra el mensaje de semillas al instante', () => {
      renderModal()
      expect(getTrustMessageInstant()).toBeInTheDocument()
    })
  })

  describe('cierre del modal', () => {
    it('llama a onClose al hacer click en el botón cerrar', async () => {
      const onClose = vi.fn()
      const user = renderModalWithUser({ onClose })
      await clickCloseButton(user)
      expectCallbackCalledOnce(onClose)
    })
  })

  describe('accesibilidad', () => {
    it('tiene el rol dialog con aria-modal', () => {
      renderModal()
      expect(getModal()).toHaveAttribute('aria-modal', 'true')
    })

    it('tiene aria-label "Tienda de semillas"', () => {
      renderModal()
      expect(getModal()).toHaveAttribute('aria-label', 'Tienda de semillas')
    })

    it('el botón de cerrar tiene aria-label descriptivo', () => {
      renderModal()
      expect(getCloseButton()).toHaveAttribute('aria-label', 'Cerrar tienda de semillas')
    })
  })
})