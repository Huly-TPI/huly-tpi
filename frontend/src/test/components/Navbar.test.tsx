import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import { ThemeProvider } from '../../context/theme'
import { SubscriptionModalProvider } from '../../context/subscriptionModal'
import type { Membership } from '../../api/auth'

/* ─── Mocks ─── */

const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
  hasSessionFlag: () => false,
}))

const mockUseMembership = vi.fn()
vi.mock('../../hooks/shop/useMembership', () => ({
  useMembership: () => mockUseMembership(),
}))


vi.mock('../../components/SubscriptionModal/SubscriptionModal', () => ({
  default: ({ isOpen }: { isOpen: boolean }) =>
    isOpen ? <div role="dialog" aria-label="Planes de suscripción" /> : null,
}))

vi.mock('../../components/Badges/BadgeModal', () => ({
  default: () => null,
}))

/* ─── Test Data ─── */

const NO_MEMBERSHIP: Membership = { active: false, planCode: null, productId: null, expiresAt: null }
const BASIC_MEMBERSHIP: Membership = { active: true, planCode: 'BASIC', productId: 'plan-basic', expiresAt: null }
const PREMIUM_MEMBERSHIP: Membership = { active: true, planCode: 'PREMIUM', productId: 'plan-premium', expiresAt: null }

/* ─── Setup Helpers ─── */

const renderWithRouter = () =>
  render(
    <SubscriptionModalProvider>
      <ThemeProvider>
        <MemoryRouter>
          <Navbar />
        </MemoryRouter>
      </ThemeProvider>
    </SubscriptionModalProvider>,
  )

/* ─── Tests ─── */

describe('Navbar', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      loading: false,
      logout: vi.fn(),
    })
    mockUseMembership.mockReturnValue({ membership: NO_MEMBERSHIP, refresh: vi.fn() })
  })

  it('renderiza el logo de Huly', () => {
    const { container } = renderWithRouter()
    expect(container.querySelector('nav')).toHaveClass('z-[300]')
    expect(screen.getByAltText('Huly logo')).toBeInTheDocument()
  })

  it('renderiza el toggle de tema en navbar', () => {
    renderWithRouter()
    expect(screen.getByRole('button', { name: 'Cambiar a modo noche' })).toBeInTheDocument()
  })

  it('renderiza todos los links de navegación', () => {
    renderWithRouter()
    expect(screen.getByText('Jardín')).toBeInTheDocument()
    expect(screen.getByText('Pendientes')).toBeInTheDocument()
    expect(screen.getByText('Minijuegos')).toBeInTheDocument()
    expect(screen.getByText('Diario')).toBeInTheDocument()
    expect(screen.getByText('Regar planta')).toBeInTheDocument()
  })

  it('cada link tiene el href correcto', () => {
    renderWithRouter()
    expect(screen.getByText('Jardín').closest('a')).toHaveAttribute('href', '/')
    expect(screen.getByText('Pendientes').closest('a')).toHaveAttribute('href', '/pending')
    expect(screen.getByText('Minijuegos').closest('a')).toHaveAttribute('href', '/minigames')
    expect(screen.getByText('Diario').closest('a')).toHaveAttribute('href', '/diary')
    expect(screen.getByText('Regar planta').closest('a')).toHaveAttribute('href', '/challenges')
  })

  describe('usuario deslogueado', () => {
    it('muestra los botones de iniciar sesión y registrarse', () => {
      renderWithRouter()
      expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toBeInTheDocument()
      expect(screen.getByRole('link', { name: 'Registrarse' })).toBeInTheDocument()
    })
  })

  describe('usuario logueado', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { id: 1, name: 'Mili', email: 'mili@huly.com', role: 'USER', themePreference: 'LIGHT' },
        loading: false,
        logout: vi.fn(),
      })
    })

    it('muestra el nombre del usuario', () => {
      renderWithRouter()
      expect(screen.getByText('Mili')).toBeInTheDocument()
    })

    it('no muestra los botones de auth', () => {
      renderWithRouter()
      expect(screen.queryByRole('link', { name: 'Iniciar sesión' })).not.toBeInTheDocument()
    })

    it('abre el dropdown al hacer click en el botón de usuario', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: /Mili/ }))

      expect(screen.getByRole('menuitem', { name: 'Mi perfil' })).toBeInTheDocument()
      expect(screen.getByRole('menuitem', { name: 'Cerrar sesión' })).toBeInTheDocument()
    })

    it('muestra el item Suscripciones en el dropdown', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: /Mili/ }))

      expect(screen.getByRole('menuitem', { name: 'Suscripciones' })).toBeInTheDocument()
    })

    it('abre el modal de suscripciones al hacer click en Suscripciones', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: /Mili/ }))
      await user.click(screen.getByRole('menuitem', { name: 'Suscripciones' }))

      expect(screen.getByRole('dialog', { name: 'Planes de suscripción' })).toBeInTheDocument()
    })

    describe('icono de suscripción', () => {
      it('muestra el icono bud cuando no hay plan activo', () => {
        mockUseMembership.mockReturnValue({ membership: NO_MEMBERSHIP, refresh: vi.fn() })
        renderWithRouter()
        const btn = screen.getByRole('button', { name: /Mili/ })
        expect(btn.querySelector('img')).toBeInTheDocument()
      })

      it('muestra un icono cuando el plan es BASIC', () => {
        mockUseMembership.mockReturnValue({ membership: BASIC_MEMBERSHIP, refresh: vi.fn() })
        renderWithRouter()
        const btn = screen.getByRole('button', { name: /Mili/ })
        expect(btn.querySelector('img')).toBeInTheDocument()
      })

      it('muestra un icono cuando el plan es PREMIUM', () => {
        mockUseMembership.mockReturnValue({ membership: PREMIUM_MEMBERSHIP, refresh: vi.fn() })
        renderWithRouter()
        const btn = screen.getByRole('button', { name: /Mili/ })
        expect(btn.querySelector('img')).toBeInTheDocument()
      })
    })
  })

  describe('menú mobile', () => {
    it('abre el menú al hacer click en el hamburguesa', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: 'Abrir menú' }))

      expect(screen.getAllByText('Jardín').length).toBeGreaterThan(1)
    })

    it('muestra el control de tema dentro del menú mobile', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: 'Abrir menú' }))

      expect(screen.getByText('Tema')).toBeInTheDocument()
      expect(screen.getAllByRole('button', { name: 'Cambiar a modo noche' }).length).toBeGreaterThan(1)
    })

    it('muestra los botones de auth dentro del menú mobile cuando está deslogueado', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: 'Abrir menú' }))

      expect(screen.getAllByRole('link', { name: 'Iniciar sesión' }).length).toBe(2)
      expect(screen.getAllByRole('link', { name: 'Registrarse' }).length).toBe(2)
    })
  })
})
