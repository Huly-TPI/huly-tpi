import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SubscriptionModal from '../../components/SubscriptionModal/SubscriptionModal'
import type { Plan } from '../../api/payment'
import type { Membership } from '../../api/auth'

/* ─── Mocks ─── */

const mockBuy = vi.fn()
const mockMarkPending = vi.fn()

const mockUsePlans = vi.fn<[], { plans: Plan[]; loading: boolean; error: string | null }>()
const mockUseMembership = vi.fn<[], { membership: Membership | null; refresh: () => void }>()
const mockUsePurchase = vi.fn<[], { buyingId: string | null; error: string | null; buy: typeof mockBuy }>()

vi.mock('../../hooks/shop/usePlans', () => ({ usePlans: () => mockUsePlans() }))
vi.mock('../../hooks/shop/useMembership', () => ({ useMembership: () => mockUseMembership() }))
vi.mock('../../hooks/shop/usePurchase', () => ({ usePurchase: () => mockUsePurchase() }))
vi.mock('../../hooks/shop/useRefreshOnReturn', () => ({ useRefreshOnReturn: () => mockMarkPending }))

/* ─── Test Data ─── */

const PLANS: Plan[] = [
  { id: 'plan-basic', name: 'Plan Basico', description: 'Acceso por 30 días', price: 5999, coinsAmount: 0, planCode: 'BASIC' },
  { id: 'plan-premium', name: 'Plan Premium', description: 'Acceso premium por 30 días', price: 9999, coinsAmount: 0, planCode: 'PREMIUM' },
]

const NO_MEMBERSHIP: Membership = { active: false, planCode: null, productId: null, expiresAt: null }
const BASIC_MEMBERSHIP: Membership = { active: true, planCode: 'BASIC', productId: 'plan-basic', expiresAt: '2026-07-22T00:00:00Z' }
const PREMIUM_MEMBERSHIP: Membership = { active: true, planCode: 'PREMIUM', productId: 'plan-premium', expiresAt: '2026-07-22T00:00:00Z' }

function createPlansHook(overrides: Partial<ReturnType<typeof mockUsePlans>> = {}) {
  return { plans: PLANS, loading: false, error: null, ...overrides }
}

function createPurchaseHook(overrides: Partial<ReturnType<typeof mockUsePurchase>> = {}) {
  return { buyingId: null, error: null, buy: mockBuy, ...overrides }
}

/* ─── Setup Helpers ─── */

const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
  onRefreshMembership: vi.fn(),
}

function renderModal(propOverrides: Partial<typeof defaultProps> = {}) {
  return render(<SubscriptionModal {...defaultProps} {...propOverrides} />)
}

function renderModalWithUser(propOverrides: Partial<typeof defaultProps> = {}) {
  const user = userEvent.setup()
  renderModal(propOverrides)
  return user
}

/* ─── Query Helpers ─── */

const getModal = () => screen.getByRole('dialog', { name: /planes de suscripción/i })
const queryModal = () => screen.queryByRole('dialog', { name: /planes de suscripción/i })
const getCloseButton = () => screen.getByRole('button', { name: /cerrar planes de suscripción/i })
const getLoadingSpinner = () => document.querySelector('.animate-spin')

/* ─── Tests ─── */

describe('SubscriptionModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUsePlans.mockReturnValue(createPlansHook())
    mockUseMembership.mockReturnValue({ membership: NO_MEMBERSHIP, refresh: vi.fn() })
    mockUsePurchase.mockReturnValue(createPurchaseHook())
  })

  describe('visibilidad', () => {
    it('no renderiza nada cuando isOpen es false', () => {
      renderModal({ isOpen: false })
      expect(queryModal()).not.toBeInTheDocument()
    })

    it('renderiza el modal cuando isOpen es true', () => {
      renderModal()
      expect(getModal()).toBeInTheDocument()
    })
  })

  describe('header', () => {
    it('muestra el título Planes de Suscripción', () => {
      renderModal()
      expect(screen.getByText('Planes de Suscripción')).toBeInTheDocument()
    })

    it('muestra el subtítulo', () => {
      renderModal()
      expect(screen.getByText(/elegí el plan que mejor se adapte/i)).toBeInTheDocument()
    })
  })

  describe('card Free', () => {
    it('muestra el título Gratuito', () => {
      renderModal()
      expect(screen.getByText('Gratuito')).toBeInTheDocument()
    })

    it('muestra $0 y Siempre gratis', () => {
      renderModal()
      expect(screen.getByText('$0')).toBeInTheDocument()
      expect(screen.getByText('Siempre gratis')).toBeInTheDocument()
    })

    it('muestra la feature 5 mensajes al día', () => {
      renderModal()
      expect(screen.getByText('5 mensajes al día')).toBeInTheDocument()
    })

    it('muestra Plan actual cuando el usuario no tiene plan activo', () => {
      renderModal()
      expect(screen.getByText('Plan actual')).toBeInTheDocument()
    })

    it('muestra Plan base cuando el usuario tiene un plan pago activo', () => {
      mockUseMembership.mockReturnValue({ membership: BASIC_MEMBERSHIP, refresh: vi.fn() })
      renderModal()
      expect(screen.getByText('Plan base')).toBeInTheDocument()
    })
  })

  describe('cards de planes pagos', () => {
    it('muestra el nombre visual de cada plan', () => {
      renderModal()
      expect(screen.getByText('Basico')).toBeInTheDocument()
      expect(screen.getByText('Huly')).toBeInTheDocument()
    })

    it('muestra los precios en ARS', () => {
      renderModal()
      expect(screen.getByText(/\$5\.999/)).toBeInTheDocument()
      expect(screen.getByText(/\$9\.999/)).toBeInTheDocument()
    })

    it('muestra Acceso por 30 días en cada plan pago', () => {
      renderModal()
      expect(screen.getAllByText('Acceso por 30 días')).toHaveLength(2)
    })

    it('muestra features exclusivas del plan Basico', () => {
      renderModal()
      expect(screen.getByText('3 audios por día')).toBeInTheDocument()
      expect(screen.getByText('Mandalas exclusivos (plan básico)')).toBeInTheDocument()
    })

    it('muestra features exclusivas del plan Huly', () => {
      renderModal()
      expect(screen.getByText('1000 monedas')).toBeInTheDocument()
      expect(screen.getByText('Audios libres')).toBeInTheDocument()
      expect(screen.getByText('1.5x recompensas diarias')).toBeInTheDocument()
    })

    it('muestra features compartidas entre planes pagos', () => {
      renderModal()
      expect(screen.getAllByText('Chatbot libre')).toHaveLength(2)
      expect(screen.getAllByText('Items exclusivos de la tienda')).toHaveLength(2)
    })

    it('muestra botones Elegir cuando no hay plan activo', () => {
      renderModal()
      expect(screen.getByRole('button', { name: 'Elegir Basico' })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Elegir Huly' })).toBeInTheDocument()
    })

    it('muestra Renovar en el plan activo del usuario', () => {
      mockUseMembership.mockReturnValue({ membership: BASIC_MEMBERSHIP, refresh: vi.fn() })
      renderModal()
      expect(screen.getByRole('button', { name: 'Renovar' })).toBeInTheDocument()
    })

    it('deshabilita el otro plan cuando el usuario ya tiene uno activo', () => {
      mockUseMembership.mockReturnValue({ membership: BASIC_MEMBERSHIP, refresh: vi.fn() })
      renderModal()
      expect(screen.getByRole('button', { name: 'Plan activo' })).toBeDisabled()
    })

    it('muestra Plan actual en el plan activo del usuario', () => {
      mockUseMembership.mockReturnValue({ membership: PREMIUM_MEMBERSHIP, refresh: vi.fn() })
      renderModal()
      expect(screen.getByText('Plan actual')).toBeInTheDocument()
    })
  })

  describe('estado de carga', () => {
    it('muestra el spinner mientras carga los planes', () => {
      mockUsePlans.mockReturnValue(createPlansHook({ loading: true }))
      renderModal()
      expect(getLoadingSpinner()).toBeInTheDocument()
    })

    it('no muestra las cards mientras carga', () => {
      mockUsePlans.mockReturnValue(createPlansHook({ loading: true }))
      renderModal()
      expect(screen.queryByText('Basico')).not.toBeInTheDocument()
    })

    it('no muestra el spinner cuando terminó de cargar', () => {
      renderModal()
      expect(getLoadingSpinner()).not.toBeInTheDocument()
    })
  })

  describe('compra', () => {
    it('llama a buy con el id del plan Basico', async () => {
      const user = renderModalWithUser()
      await user.click(screen.getByRole('button', { name: 'Elegir Basico' }))
      expect(mockBuy).toHaveBeenCalledWith('plan-basic')
    })

    it('llama a buy con el id del plan Huly', async () => {
      const user = renderModalWithUser()
      await user.click(screen.getByRole('button', { name: 'Elegir Huly' }))
      expect(mockBuy).toHaveBeenCalledWith('plan-premium')
    })

    it('muestra Procesando… en el botón del plan que se está comprando', () => {
      mockUsePurchase.mockReturnValue(createPurchaseHook({ buyingId: 'plan-basic' }))
      renderModal()
      expect(screen.getByText('Procesando…')).toBeInTheDocument()
    })

    it('deshabilita todos los botones de compra cuando hay una en curso', () => {
      mockUsePurchase.mockReturnValue(createPurchaseHook({ buyingId: 'plan-basic' }))
      renderModal()
      screen.getAllByRole('button').filter(b => b.textContent?.includes('Elegir') || b.textContent?.includes('Procesando'))
        .forEach(btn => expect(btn).toBeDisabled())
    })
  })

  describe('manejo de errores', () => {
    it('muestra el error cuando falla la carga de planes', () => {
      mockUsePlans.mockReturnValue(createPlansHook({ error: 'No se pudieron cargar los planes.' }))
      renderModal()
      expect(screen.getByText('No se pudieron cargar los planes.')).toBeInTheDocument()
    })

    it('muestra el error cuando falla la compra', () => {
      mockUsePurchase.mockReturnValue(createPurchaseHook({ error: 'Error al iniciar el pago. Intentá de nuevo.' }))
      renderModal()
      expect(screen.getByText('Error al iniciar el pago. Intentá de nuevo.')).toBeInTheDocument()
    })
  })

  describe('cierre del modal', () => {
    it('llama a onClose al hacer click en el botón X', async () => {
      const onClose = vi.fn()
      const user = renderModalWithUser({ onClose })
      await user.click(getCloseButton())
      expect(onClose).toHaveBeenCalledOnce()
    })

    it('llama a onClose al hacer click en el overlay', async () => {
      const onClose = vi.fn()
      const user = renderModalWithUser({ onClose })
      await user.click(document.querySelector('.fixed.inset-0') as Element)
      expect(onClose).toHaveBeenCalledOnce()
    })
  })

  describe('accesibilidad', () => {
    it('tiene role dialog con aria-modal true', () => {
      renderModal()
      expect(getModal()).toHaveAttribute('aria-modal', 'true')
    })

    it('el botón cerrar tiene aria-label descriptivo', () => {
      renderModal()
      expect(getCloseButton()).toHaveAttribute('aria-label', 'Cerrar planes de suscripción')
    })
  })
})