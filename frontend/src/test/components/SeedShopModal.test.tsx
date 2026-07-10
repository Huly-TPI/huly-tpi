import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SeedShopModal from '../../components/SeedShopModal/SeedShopModal'
import type { Product } from '../../api/payment'
import { verifyTextPresent, verifyTextNotPresent, clearAllMocks, getLoadingSpinner } from '../testHelpers'


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





/* ─── Setup Helpers ─── */

const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
}





/* ─── Query Helpers ─── */































/* ─── Interaction Helpers ─── */





/* ─── Assertion Helpers ─── */



























  // helpers

describe('SeedShopModal', () => {
    let onCloseSpy: any

    beforeEach(() => {
      clearAllMocks()
      setupProductsHookReturn(createProductsHookReturn())
      setupPurchaseHookReturn(createPurchaseHookReturn())
      setupUserCoinsMock({ coins: 150, refresh: vi.fn() })
    })

    describe('visibilidad', () => {
      it('no renderiza nada cuando isOpen es false', () => {
        renderModalWithProps({ isOpen: false })
        expectModalNotToBeRendered()
      })

      it('renderiza el modal cuando isOpen es true', () => {
        renderModalWithProps({ isOpen: true })
        expectModalToBeVisible()
      })
    })

    describe('contenido del header', () => {
      it('muestra el título y subtítulo', () => {
        renderDefault()
        expectTitleAndSubtitleToBeVisible()
      })
    })

    describe('cards de productos', () => {
      it('muestra los nombres de todos los productos', () => {
        renderDefault()
        expectAllProductNamesToBeVisible()
      })

      it('muestra la cantidad de semillas en cada card', () => {
        renderDefault()
        expectAllSeedsBadgesToBeVisible()
      })

      it('muestra el precio en ARS de cada producto', () => {
        renderDefault()
        verifyPricesInARS()
      })

      it('marca el último producto como "Mejor valor"', () => {
        renderDefault()
        verifyFeaturedRibbonPresent()
      })
    })

    describe('estado de carga', () => {
      it('muestra el spinner mientras carga', () => {
        setupProductsHookReturn(createProductsHookReturn({ loading: true }))
        renderDefault()
        expectLoadingSpinnerToBeVisible()
      })

      it('no muestra el spinner cuando termina de cargar', () => {
        renderDefault()
        expectLoadingSpinnerNotToBeVisible()
      })

      it('no muestra las cards mientras carga', () => {
        setupProductsHookReturn(createProductsHookReturn({ loading: true }))
        renderDefault()
        verifyTextNotPresent(PRODUCTS[0].name)
      })
    })

    describe('compra de semillas', () => {
      it('muestra un botón Comprar por cada producto', () => {
        renderDefault()
        expectBuyButtonsCount(PRODUCTS.length)
      })

      it('llama a buy con el id del producto al hacer click en Comprar', () => {
        renderWithUser()
        return clickBuy(0).then(() => {
          expectCallbackCalledWith(mockBuy, PRODUCTS[0].id)
        })
      })

      it('llama a buy con el id correcto al comprar el segundo producto', () => {
        renderWithUser()
        return clickBuy(1).then(() => {
          expectCallbackCalledWith(mockBuy, PRODUCTS[1].id)
        })
      })

      it('deshabilita todos los botones cuando hay una compra en proceso', () => {
        setupPurchaseHookReturn(createPurchaseHookReturn({ buyingId: 'prod-1' }))
        renderDefault()
        expectBuyButtonsToBeDisabled()
      })

      it('muestra "Procesando…" en el botón del producto que se está comprando', () => {
        setupPurchaseHookReturn(createPurchaseHookReturn({ buyingId: 'prod-2' }))
        renderDefault()
        expectProcessingButtonToBeVisible()
      })
    })

    describe('manejo de errores', () => {
      it('muestra el error cuando falla la carga de productos', () => {
        setupProductsHookReturn(createProductsHookReturn({ error: 'No se pudieron cargar los productos.' }))
        renderDefault()
        expectErrorToBeVisible('No se pudieron cargar los productos.')
      })

      it('muestra el error cuando falla la compra', () => {
        setupPurchaseHookReturn(createPurchaseHookReturn({ error: 'Error al iniciar el pago. Intentá de nuevo.' }))
        renderDefault()
        expectErrorToBeVisible('Error al iniciar el pago. Intentá de nuevo.')
      })
    })

    describe('mensaje de confianza', () => {
      it('muestra el mensaje de pago seguro', () => {
        renderDefault()
        verifyTrustMessageSecurePresent()
      })

      it('muestra el mensaje de semillas al instante', () => {
        renderDefault()
        verifyTrustMessageInstantPresent()
      })
    })

    describe('cierre del modal', () => {
      it('llama a onClose al hacer click en el botón cerrar', () => {
        setupOnCloseSpy()
        renderWithUserAndCloseSpy()
        return clickClose().then(() => {
          expectCallbackCalledOnce(onCloseSpy)
        })
      })
    })

    describe('accesibilidad', () => {
      it('tiene el rol dialog con aria-modal', () => {
        renderDefault()
        verifyModalHasAriaModal('true')
      })

      it('tiene aria-label "Tienda de semillas"', () => {
        renderDefault()
        verifyModalHasAriaLabel('Tienda de semillas')
      })

      it('el botón de cerrar tiene aria-label descriptivo', () => {
        renderDefault()
        verifyCloseButtonHasAriaLabel('Cerrar tienda de semillas')
      })
    })
    let user: any

    const setupProductsHookReturn = (val: any) => {
      mockUseProducts.mockReturnValue(val)
    }

    const setupPurchaseHookReturn = (val: any) => {
      mockUsePurchase.mockReturnValue(val)
    }

    const setupUserCoinsMock = (val: any) => {
      mockUseUserCoins.mockReturnValue(val)
    }

    const renderModalWithProps = (props: Partial<typeof defaultProps>) => {
      renderModal(props)
    }

    const renderDefault = () => {
      renderModal()
    }

    const verifyPricesInARS = () => {
      PRODUCTS.forEach(p => {
        verifyTextPresent(new RegExp(`\\$${p.price.toLocaleString('es-AR')}`))
      })
    }

    const verifyFeaturedRibbonPresent = () => {
      expect(getFeaturedRibbon()).toBeInTheDocument()
    }

    const renderWithUser = () => {
      user = userEvent.setup()
      renderModal()
    }

    const clickBuy = (index: number) => {
      return clickBuyButton(user, index)
    }

    const verifyTrustMessageSecurePresent = () => {
      expect(getTrustMessageSecure()).toBeInTheDocument()
    }

    const verifyTrustMessageInstantPresent = () => {
      expect(getTrustMessageInstant()).toBeInTheDocument()
    }

    const setupOnCloseSpy = () => {
      onCloseSpy = vi.fn()
    }

    const renderWithUserAndCloseSpy = () => {
      user = userEvent.setup()
      renderModal({ onClose: onCloseSpy })
    }

    const clickClose = () => {
      return clickCloseButton(user)
    }

    const verifyModalHasAriaModal = (val: string) => {
      expect(getModal()).toHaveAttribute('aria-modal', val)
    }

    const verifyModalHasAriaLabel = (val: string) => {
      expect(getModal()).toHaveAttribute('aria-label', val)
    }

    const verifyCloseButtonHasAriaLabel = (val: string) => {
      expect(getCloseButton()).toHaveAttribute('aria-label', val)
    }

    function queryModal() {
      return screen.queryByRole('dialog', { name: 'Tienda de semillas' })
    }

    function getModal() {
      return screen.getByRole('dialog', { name: 'Tienda de semillas' })
    }

    function getTitle() {
      return screen.getByText('Tienda de semillas')
    }

    function getSubtitle() {
      return screen.getByText(/comprá semillas para decorar tu jardín/i)
    }

    function getAllBuyButtons() {
      return screen.getAllByRole('button', { name: 'Comprar' })
    }

    function getBuyButtonForProduct(productIndex: number) {
      return getAllBuyButtons()[productIndex]
    }

    function queryProcessingButton() {
      return screen.queryByRole('button', { name: (content) => content.includes('Procesando') })
    }

    function getCloseButton() {
      return screen.getByRole('button', { name: 'Cerrar tienda de semillas' })
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

    async function clickBuyButton(usr: ReturnType<typeof userEvent.setup>, productIndex = 0) {
      await usr.click(getBuyButtonForProduct(productIndex))
    }

    async function clickCloseButton(usr: ReturnType<typeof userEvent.setup>) {
      await usr.click(getCloseButton())
    }

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
  })

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

function renderModal(propOverrides: Partial<typeof defaultProps> = {}) {
  const props = { ...defaultProps, ...propOverrides }
  return render(<SeedShopModal {...props} />)
}

function expectCallbackCalledOnce(callback: ReturnType<typeof vi.fn>) {
  expect(callback).toHaveBeenCalledOnce()
}

function expectCallbackCalledWith(callback: ReturnType<typeof vi.fn>, ...args: unknown[]) {
  expect(callback).toHaveBeenCalledWith(...args)
}
