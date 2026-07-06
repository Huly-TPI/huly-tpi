import { clearAllMocks, verifyTextPresent, verifyTextNotPresent } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SubscriptionModal from '../../components/SubscriptionModal/SubscriptionModal'
import type { Plan } from '../../api/payment'
import type { Membership } from '../../api/auth'
// --- SIMULACIONES GLOBALES (MOCKS) ---
const mockBuy = vi.fn()
const mockMarkPending = vi.fn()

const mockUsePlans = vi.fn<[], { plans: Plan[]; loading: boolean; error: string | null }>()
const mockUseMembership = vi.fn<[], { membership: Membership | null; refresh: () => void }>()
const mockUsePurchase = vi.fn<[], { buyingId: string | null; error: string | null; buy: typeof mockBuy }>()

vi.mock('../../hooks/shop/usePlans', () => ({ usePlans: () => mockUsePlans() }))
vi.mock('../../hooks/shop/useMembership', () => ({ useMembership: () => mockUseMembership() }))
vi.mock('../../hooks/shop/usePurchase', () => ({ usePurchase: () => mockUsePurchase() }))
vi.mock('../../hooks/shop/useRefreshOnReturn', () => ({ useRefreshOnReturn: () => mockMarkPending }))
// --- DATOS DE PRUEBA ---
const PLANS: Plan[] = [
  { id: 'plan-basic', name: 'Plan Basico', description: 'Acceso por 30 días', price: 5999, coinsAmount: 0, planCode: 'BASIC' },
  { id: 'plan-premium', name: 'Plan Premium', description: 'Acceso premium por 30 días', price: 9999, coinsAmount: 0, planCode: 'PREMIUM' },
]

const NO_MEMBERSHIP: Membership = { active: false, planCode: null, productId: null, expiresAt: null }
const BASIC_MEMBERSHIP: Membership = { active: true, planCode: 'BASIC', productId: 'plan-basic', expiresAt: '2026-07-22T00:00:00Z' }
const PREMIUM_MEMBERSHIP: Membership = { active: true, planCode: 'PREMIUM', productId: 'plan-premium', expiresAt: '2026-07-22T00:00:00Z' }


describe('SubscriptionModal', () => {
  let user: ReturnType<typeof userEvent.setup>

  beforeEach(() => {
    clearAllMocks()
    setupUser()
    setupDefaultPlans()
    setupDefaultMembership()
    setupDefaultPurchase()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  describe('visibilidad', () => {
    it('no renderiza nada cuando isOpen es false', () => {
      renderModal({ isOpen: false })
      verifyModalNotVisible()
    })

    it('renderiza el modal cuando isOpen es true', () => {
      renderModal()
      verifyModalVisible()
    })
  })

  describe('header', () => {
    it('muestra el título Planes de Suscripción', () => {
      renderModal()
      verifyTextPresent('Planes de Suscripción')
    })

    it('muestra el subtítulo', () => {
      renderModal()
      verifyTextPresent('Elegí el plan que mejor se adapte a vos')
    })
  })

  describe('card Free', () => {
    it('muestra el título Gratuito', () => {
      renderModal()
      verifyTextPresent('Gratuito')
    })

    it('muestra $0 y Siempre gratis', () => {
      renderModal()
      verifyTextPresent('$0')
      verifyTextPresent('Siempre gratis')
    })

    it('muestra la feature 5 mensajes al día', () => {
      renderModal()
      verifyTextPresent('5 mensajes al día')
    })

    it('muestra Plan actual cuando el usuario no tiene plan activo', () => {
      renderModal()
      verifyTextPresent('Plan actual')
    })

    it('muestra Plan base cuando el usuario tiene un plan pago activo', () => {
      setupMembership(BASIC_MEMBERSHIP)
      renderModal()
      verifyTextPresent('Plan base')
    })
  })

  describe('cards de planes pagos', () => {
    it('muestra el nombre visual de cada plan', () => {
      renderModal()
      verifyTextPresent('Basico')
      verifyTextPresent('Huly')
    })

    it('muestra los precios en ARS', () => {
      renderModal()
      verifyTextPresent('$5.999')
      verifyTextPresent('$9.999')
    })

    it('muestra Acceso por 30 días en cada plan pago', () => {
      renderModal()
      verifyTextPresentCount('Acceso por 30 días', 2)
    })

    it('muestra features exclusivas del plan Basico', () => {
      renderModal()
      verifyTextPresent('3 audios por día')
      verifyCountGreaterThanOrEqual('Mandalas exclusivos', 1)
    })

    it('muestra features exclusivas del plan Huly', () => {
      renderModal()
      verifyTextPresent('1000 monedas')
      verifyTextPresent('Audios libres')
      verifyCountGreaterThanOrEqual('1.5x recompensas diarias', 1)
    })

    it('muestra features compartidas entre planes pagos', () => {
      renderModal()
      verifyTextPresentCount('Chatbot libre', 2)
      verifyTextPresentCount('Items exclusivos de la tienda', 2)
    })

    it('muestra botones Elegir cuando no hay plan activo', () => {
      renderModal()
      verifyChooseButtonVisible('Basico')
      verifyChooseButtonVisible('Huly')
    })

    it('muestra Plan actual en la card del plan activo del usuario', () => {
      setupMembership(BASIC_MEMBERSHIP)
      renderModal()
      verifyTextPresent('Plan actual')
    })

    it('no muestra botón de compra en el plan activo del usuario', () => {
      setupMembership(BASIC_MEMBERSHIP)
      renderModal()
      verifyChooseButtonNotVisible('Basico')
      verifyChooseButtonVisible('Huly')
    })

    it('muestra Plan actual en el plan activo del usuario', () => {
      setupMembership(PREMIUM_MEMBERSHIP)
      renderModal()
      verifyTextPresent('Plan actual')
    })
  })

  describe('estado de carga', () => {
    it('muestra el spinner mientras carga los planes', () => {
      setupPlansLoading()
      renderModal()
      verifySpinnerVisible()
    })

    it('no muestra las cards mientras carga', () => {
      setupPlansLoading()
      renderModal()
      verifyTextNotPresent('Basico')
    })

    it('no muestra el spinner cuando terminó de cargar', () => {
      renderModal()
      verifySpinnerNotVisible()
    })
  })

  describe('compra', () => {
    it('llama a buy con el id del plan Basico', () => {
      renderModal()
      return clickChooseButton('Basico').then(() => {
        verifyBuyCalledWith('plan-basic')
      })
    })

    it('llama a buy con el id del plan Huly', () => {
      renderModal()
      return clickChooseButton('Huly').then(() => {
        verifyBuyCalledWith('plan-premium')
      })
    })

    it('muestra Procesando… en el botón del plan que se está comprando', () => {
      setupPurchaseInProgress('plan-basic')
      renderModal()
      verifyTextPresent('Procesando…')
    })

    it('deshabilita todos los botones de compra cuando hay una en curso', () => {
      setupPurchaseInProgress('plan-basic')
      renderModal()
      verifyPurchaseButtonsDisabled()
    })
  })

  describe('manejo de errores', () => {
    it('muestra el error cuando falla la carga de planes', () => {
      setupPlansError('No se pudieron cargar los planes.')
      renderModal()
      verifyTextPresent('No se pudieron cargar los planes.')
    })

    it('muestra el error cuando falla la compra', () => {
      setupPurchaseError('Error al iniciar el pago. Intentá de nuevo.')
      renderModal()
      verifyTextPresent('Error al iniciar el pago. Intentá de nuevo.')
    })
  })

  describe('cierre del modal', () => {
    it('llama a onClose al hacer click en el botón X', () => {
      const onClose = vi.fn()
      renderModal({ onClose })
      return clickCloseButton().then(() => {
        verifyOnCloseCalled(onClose)
      })
    })

    it('llama a onClose al hacer click en el overlay', () => {
      const onClose = vi.fn()
      renderModal({ onClose })
      return clickOverlay().then(() => {
        verifyOnCloseCalled(onClose)
      })
    })
  })

  describe('accesibilidad', () => {
    it('tiene role dialog con aria-modal true', () => {
      renderModal()
      verifyModalAriaAttribute('aria-modal', 'true')
    })

    it('el botón cerrar tiene aria-label descriptivo', () => {
      renderModal()
      verifyCloseButtonAriaAttribute('aria-label', 'Cerrar planes de suscripción')
    })
  })

  /* helpers */

  const renderModal = (propOverrides: any = {}) => {
    render(<SubscriptionModal isOpen={true} onClose={vi.fn()} onRefreshMembership={vi.fn()} {...propOverrides} />)
  }

  const setupMembership = (membership: Membership) => {
    mockUseMembership.mockReturnValue({ membership, refresh: vi.fn() })
  }

  const setupPlansLoading = () => {
    mockUsePlans.mockReturnValue({ plans: PLANS, loading: true, error: null })
  }

  const setupPlansError = (error: string) => {
    mockUsePlans.mockReturnValue({ plans: [], loading: false, error })
  }

  const setupPurchaseInProgress = (buyingId: string) => {
    mockUsePurchase.mockReturnValue({ buyingId, error: null, buy: mockBuy })
  }

  const setupPurchaseError = (error: string) => {
    mockUsePurchase.mockReturnValue({ buyingId: null, error, buy: mockBuy })
  }

  const clickChooseButton = (name: string) => {
    return user.click(screen.getByRole('button', { name: `Elegir ${name}` }))
  }

  const clickCloseButton = () => {
    return user.click(screen.getByRole('button', { name: 'Cerrar planes de suscripción' }))
  }

  const clickOverlay = () => {
    return user.click(document.querySelector('.fixed.inset-0') as Element)
  }

  const verifyModalVisible = () => {
    expect(screen.getByRole('dialog', { name: 'Planes de suscripción' })).toBeInTheDocument()
  }

  const verifyModalNotVisible = () => {
    expect(screen.queryByRole('dialog', { name: 'Planes de suscripción' })).not.toBeInTheDocument()
  }

  

  

  const verifyTextPresentCount = (text: string, count: number) => {
    expect(screen.getAllByText(text)).toHaveLength(count)
  }

  const verifyCountGreaterThanOrEqual = (text: string, count: number) => {
    expect(screen.getAllByText(text).length).toBeGreaterThanOrEqual(count)
  }

  const verifyChooseButtonVisible = (name: string) => {
    expect(screen.getByRole('button', { name: `Elegir ${name}` })).toBeInTheDocument()
  }

  const verifyChooseButtonNotVisible = (name: string) => {
    expect(screen.queryByRole('button', { name: `Elegir ${name}` })).not.toBeInTheDocument()
  }

  const verifySpinnerVisible = () => {
    expect(document.querySelector('.animate-spin')).toBeInTheDocument()
  }

  const verifySpinnerNotVisible = () => {
    expect(document.querySelector('.animate-spin')).not.toBeInTheDocument()
  }

  const verifyBuyCalledWith = (id: string) => {
    expect(mockBuy).toHaveBeenCalledWith(id)
  }

  const verifyPurchaseButtonsDisabled = () => {
    screen.getAllByRole('button')
      .filter(b => b.textContent?.includes('Elegir') || b.textContent?.includes('Procesando'))
      .forEach(btn => expect(btn).toBeDisabled())
  }

  const verifyOnCloseCalled = (onClose: any) => {
    expect(onClose).toHaveBeenCalledOnce()
  }

  const verifyModalAriaAttribute = (attr: string, val: string) => {
    expect(screen.getByRole('dialog', { name: 'Planes de suscripción' })).toHaveAttribute(attr, val)
  }

  const verifyCloseButtonAriaAttribute = (attr: string, val: string) => {
    expect(screen.getByRole('button', { name: 'Cerrar planes de suscripción' })).toHaveAttribute(attr, val)
  }
  const setupUser = () => {
    user = userEvent.setup()
  }

  const setupDefaultPlans = () => {
    mockUsePlans.mockReturnValue({ plans: PLANS, loading: false, error: null })
  }

  const setupDefaultMembership = () => {
    mockUseMembership.mockReturnValue({ membership: NO_MEMBERSHIP, refresh: vi.fn() })
  }

  const setupDefaultPurchase = () => {
    mockUsePurchase.mockReturnValue({ buyingId: null, error: null, buy: mockBuy })
  }
})