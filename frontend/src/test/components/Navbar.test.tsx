import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import { ThemeProvider } from '../../context/theme'
import { SubscriptionModalProvider } from '../../context/subscriptionModal'
import type { Membership } from '../../api/auth'
import { clickButton, verifyTextPresent, clearAllMocks } from '../testHelpers'


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


/* ─── Tests ─── */

describe('Navbar', () => {
  beforeEach(() => {
    clearAllMocks()
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      loading: false,
      logout: vi.fn(),
    })
    mockUseMembership.mockReturnValue({ membership: NO_MEMBERSHIP, refresh: vi.fn() })
  })

  it('renderiza el logo de Huly', () => {
    renderDefaultNavbar()
    verifyNavHasClass('z-[300]')
    verifyLogoImagePresent()
  })

  it('renderiza el toggle de tema en navbar', () => {
    renderDefaultNavbar()
    verifyThemeToggleButtonPresent()
  })

  it('renderiza todos los links de navegación', () => {
    renderDefaultNavbar()
    verifyTextPresent('Jardín')
    verifyTextPresent('Pendientes')
    verifyTextPresent('Minijuegos')
    verifyTextPresent('Diario')
    verifyTextPresent('Regar planta')
  })

  it('cada link tiene el href correcto', () => {
    renderDefaultNavbar()
    verifyLinkHref('Jardín', '/')
    verifyLinkHref('Pendientes', '/pending')
    verifyLinkHref('Minijuegos', '/minigames')
    verifyLinkHref('Diario', '/diary')
    verifyLinkHref('Regar planta', '/challenges')
  })

  describe('usuario deslogueado', () => {
    it('muestra los botones de iniciar sesión y registrarse', () => {
      renderDefaultNavbar()
      verifyAuthLinksPresent()
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
      renderDefaultNavbar()
      verifyTextPresent('Mili')
    })

    it('no muestra los botones de auth', () => {
      renderDefaultNavbar()
      verifyAuthLinksNotPresent()
    })

    it('abre el dropdown al hacer click en el botón de usuario', () => {
      renderDefaultNavbarWithUser()
      return clickUserButton(/Mili/).then(() => {
        verifyDropdownMenuitemPresent('Mi perfil')
        verifyDropdownMenuitemPresent('Cerrar sesión')
      })
    })

    it('muestra el item Suscripciones en el dropdown', () => {
      renderDefaultNavbarWithUser()
      return clickUserButton(/Mili/).then(() => {
        verifyDropdownMenuitemPresent('Suscripciones')
      })
    })

    it('abre el modal de suscripciones al hacer click en Suscripciones', () => {
      renderDefaultNavbarWithUser()
      return clickUserButton(/Mili/)
        .then(() => clickDropdownMenuitem('Suscripciones'))
        .then(() => {
          verifySubscriptionDialogPresent()
        })
    })

    describe('icono de suscripción', () => {
      it('muestra el icono bud cuando no hay plan activo', () => {
        setupMembershipMock(NO_MEMBERSHIP)
        renderDefaultNavbar()
        verifyUserButtonHasImage(/Mili/)
      })

      it('muestra un icono cuando el plan es BASIC', () => {
        setupMembershipMock(BASIC_MEMBERSHIP)
        renderDefaultNavbar()
        verifyUserButtonHasImage(/Mili/)
      })

      it('muestra un icono cuando el plan es PREMIUM', () => {
        setupMembershipMock(PREMIUM_MEMBERSHIP)
        renderDefaultNavbar()
        verifyUserButtonHasImage(/Mili/)
      })
    })
  })

  describe('menú mobile', () => {
    it('abre el menú al hacer click en el hamburguesa', () => {
      renderDefaultNavbarWithUser()
      return clickMenuButton('Abrir menú').then(() => {
        verifyGardenLinkCountGreaterThan(1)
      })
    })

    it('muestra el control de tema dentro del menú mobile', () => {
      renderDefaultNavbarWithUser()
      return clickMenuButton('Abrir menú').then(() => {
        verifyTextPresent('Tema')
        verifyThemeToggleButtonCountGreaterThan(1)
      })
    })

    it('muestra los botones de auth dentro del menú mobile cuando está deslogueado', () => {
      renderDefaultNavbarWithUser()
      return clickMenuButton('Abrir menú').then(() => {
        verifyLoginLinkCount(2)
        verifyRegisterLinkCount(2)
      })
    })
  })
  let user: any
  let renderResult: any

  /* helpers */

  const renderDefaultNavbar = () => {
    renderResult = renderWithRouter()
  }

  const renderDefaultNavbarWithUser = () => {
    user = userEvent.setup()
    renderResult = renderWithRouter()
  }

  const verifyNavHasClass = (className: string) => {
    expect(renderResult.container.querySelector('nav')).toHaveClass(className)
  }

  const verifyLogoImagePresent = () => {
    expect(screen.getByAltText('Huly logo')).toBeInTheDocument()
  }

  const verifyThemeToggleButtonPresent = () => {
    expect(screen.getByRole('button', { name: 'Cambiar a modo noche' })).toBeInTheDocument()
  }

  const verifyLinkHref = (text: string, href: string) => {
    expect(screen.getByText(text).closest('a')).toHaveAttribute('href', href)
  }

  const verifyAuthLinksPresent = () => {
    expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Registrarse' })).toBeInTheDocument()
  }

  const verifyAuthLinksNotPresent = () => {
    expect(screen.queryByRole('link', { name: 'Iniciar sesión' })).not.toBeInTheDocument()
  }

  const clickUserButton = (name: RegExp) => {
    return clickButton(user, name)
  }

  const verifyDropdownMenuitemPresent = (name: string) => {
    expect(screen.getByRole('menuitem', { name })).toBeInTheDocument()
  }

  const clickDropdownMenuitem = (name: string) => {
    return user.click(screen.getByRole('menuitem', { name }))
  }

  const verifySubscriptionDialogPresent = () => {
    expect(screen.getByRole('dialog', { name: 'Planes de suscripción' })).toBeInTheDocument()
  }

  const setupMembershipMock = (membership: Membership) => {
    mockUseMembership.mockReturnValue({ membership, refresh: vi.fn() })
  }

  const verifyUserButtonHasImage = (name: RegExp) => {
    const btn = screen.getByRole('button', { name })
    expect(btn.querySelector('img')).toBeInTheDocument()
  }

  const clickMenuButton = (name: string) => {
    return clickButton(user, name)
  }

  const verifyGardenLinkCountGreaterThan = (count: number) => {
    expect(screen.getAllByText('Jardín').length).toBeGreaterThan(count)
  }

  const verifyThemeToggleButtonCountGreaterThan = (count: number) => {
    expect(screen.getAllByRole('button', { name: 'Cambiar a modo noche' }).length).toBeGreaterThan(count)
  }

  const verifyLoginLinkCount = (count: number) => {
    expect(screen.getAllByRole('link', { name: 'Iniciar sesión' }).length).toBe(count)
  }

  const verifyRegisterLinkCount = (count: number) => {
    expect(screen.getAllByRole('link', { name: 'Registrarse' }).length).toBe(count)
  }
})

function renderWithRouter() {
  return render(
    <SubscriptionModalProvider>
      <ThemeProvider>
        <MemoryRouter>
          <Navbar />
        </MemoryRouter>
      </ThemeProvider>
    </SubscriptionModalProvider>,
  )
}
