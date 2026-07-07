import { beforeEach, describe, it, expect, vi, afterEach } from 'vitest'
import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Home from '../../pages/Home/Home'
import { ThemeProvider } from '../../context/theme'
import { completeTutorial } from '../../api/onboarding'
import { clickButton, verifyTextPresent, clearAllMocks } from '../testHelpers'

// --- SIMULACIONES GLOBALES (MOCKS) ---
const mockUseAuth = vi.fn()
const mockRefreshUser = vi.fn()

vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('../../api/onboarding', () => ({
  completeTutorial: vi.fn(),
}))

vi.mock('../../components/Shop/StoreModal', () => ({
  default: () => null,
}))

vi.mock('../../hooks/store/useInventory', () => ({
  useInventory: () => ({
    inventory: [],
    loading: false,
    error: null,
    refetch: vi.fn(),
  }),
}))

describe('Home', () => {
  let user: ReturnType<typeof userEvent.setup>
  let testContainer: HTMLElement

  beforeEach(() => {
    window.localStorage.clear()
    window.localStorage.setItem('huly:scene-theme', 'light')
    
    // Configuración por defecto para usuario común con tutorial y onboarding completados
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        name: 'Jimena',
        email: 'jimena@mail.com',
        role: 'USER',
        onBoardingCompleted: true,
        onboardingTutorialCompleted: true,
        themePreference: 'LIGHT',
      },
      refreshUser: mockRefreshUser,
    })
    vi.mocked(completeTutorial).mockResolvedValue(undefined)
    mockRefreshUser.mockResolvedValue(undefined)
  })

  afterEach(() => {
    clearAllMocks()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---
  // Todos en español, declarativos de alto nivel, sin const, await ni selectores técnicos.

  it('renderiza el fondo del jardin', () => {
    renderHome()
    verifyLightBackgroundIsActive()
  })

  it('activa el fondo oscuro al alternar a modo noche', () => {
    renderHome()
    simulateUserThemeLoadedEvent('DARK')
    verifyDarkBackgroundIsActive()
  })

  it('renderiza hotspots navegables principales', () => {
    renderHome()
    verifyHotspotLink('Perfil', '/profile')
    verifyHotspotLink('Diario', '/diary')
    verifyHotspotLink('Minijuegos', '/minigames')
    verifyHotspotLink('Pendientes', '/pending')
    verifyHotspotLink('Retos', '/challenges')
  })

  it('renderiza acceso a respiraciones guiadas desde las nubes', () => {
    renderHome()
    verifyGuidedBreathingLink('/guided-breathing')
  })

  it('redirige a diario al hacer click en el hotspot Diario', () => {
    renderHomeWithDiaryRoute()
    return clickHotspotLabel('Diario').then(() => {
      return verifyHeadingPresentAsync('Vista Diario')
    })
  })

  it('aplica el modo noche recibido desde el perfil', () => {
    renderHome()
    simulateUserThemeLoadedEvent('DARK')
    verifyDocumentThemeAttribute('dark')
  })

  it('mantiene accesos habilitados en modo noche por defecto', () => {
    renderHome()
    simulateUserThemeLoadedEvent('DARK')
    verifyLinkElementHref('Minijuegos', '/minigames')
    verifyLinkElementHref('Retos', '/challenges')
    verifyLinkElementHref('Diario', '/diary')
  })

  it('renderiza el banco con la clase de espejo mobile', () => {
    renderHome()
    verifyElementImageHasClass('Banco con cuaderno en el jardin', 'scene-element__image--mirror-mobile')
  })

  it('muestra el onboarding de bienvenida en el primer ingreso', () => {
    setupUserWithTutorialNotCompleted()
    renderHome()
    return verifyOnboardingWelcomeDialogsPresent()
  })

  it('no muestra tutorial de home si onboarding emocional no esta completo', () => {
    setupUserWithOnboardingNotCompleted()
    renderHome()
    verifyOnboardingWelcomeDialogNotPresent()
  })

  it('avanza el onboarding y lo marca como completado al finalizar', () => {
    setupUserWithTutorialNotCompleted()
    renderHome()
    return startTutorial()
      .then(() => verifyTextPresent('Casa'))
      .then(() => advanceTutorialSteps(5))
      .then(() => clickButton(user, 'Finalizar tutorial'))
      .then(() => {
        verifyTutorialCompletionRegistered()
      })
  })

  /* helpers */

  const renderHome = () => {
    user = userEvent.setup()
    const { container } = render(
      <ThemeProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </ThemeProvider>
    )
    testContainer = container
  }

  const renderHomeWithDiaryRoute = () => {
    user = userEvent.setup()
    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/']}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/diary" element={<h1>Vista Diario</h1>} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    )
  }

  const setupUserWithTutorialNotCompleted = () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        name: 'Jimena',
        email: 'jimena@mail.com',
        role: 'USER',
        onBoardingCompleted: true,
        onboardingTutorialCompleted: false,
        themePreference: 'LIGHT',
      },
      refreshUser: mockRefreshUser,
    })
  }

  const setupUserWithOnboardingNotCompleted = () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        name: 'Jimena',
        email: 'jimena@mail.com',
        role: 'USER',
        onBoardingCompleted: false,
        themePreference: 'LIGHT',
      },
      refreshUser: mockRefreshUser,
    })
  }

  const simulateUserThemeLoadedEvent = (themePreference: 'LIGHT' | 'DARK') => {
    act(() => {
      window.dispatchEvent(new CustomEvent('auth:user-loaded', {
        detail: {
          id: 1,
          name: 'Jimena',
          email: 'jimena@mail.com',
          role: 'USER',
          onBoardingCompleted: true,
          onboardingTutorialCompleted: true,
          themePreference,
        },
      }))
    })
  }

  const clickHotspotLabel = async (label: string) => {
    await user.click(screen.getByLabelText(label))
  }

  const startTutorial = async () => {
    const btn = await screen.findByRole('button', { name: 'Comenzar con el tutorial' })
    await user.click(btn)
  }

  const advanceTutorialSteps = async (steps: number) => {
    for (let i = 0; i < steps; i++) {
      await clickButton(user, 'Siguiente')
    }
  }

  const verifyLightBackgroundIsActive = () => {
    const activeLightBackgrounds = testContainer.querySelectorAll(
      '.garden-scene__background--light.garden-scene__background--active',
    )
    expect(activeLightBackgrounds.length).toBeGreaterThan(0)
  }

  const verifyDarkBackgroundIsActive = () => {
    const activeDarkBackgrounds = testContainer.querySelectorAll(
      '.garden-scene__background--dark.garden-scene__background--active',
    )
    const activeLightBackgrounds = testContainer.querySelectorAll(
      '.garden-scene__background--light.garden-scene__background--active',
    )
    expect(activeDarkBackgrounds.length).toBeGreaterThan(0)
    expect(activeLightBackgrounds.length).toBe(0)
  }

  const verifyHotspotLink = (label: string, expectedHref: string) => {
    expect(screen.getByLabelText(label).closest('a')).toHaveAttribute('href', expectedHref)
  }

  const verifyGuidedBreathingLink = (expectedHref: string) => {
    const guidedBreathingLinks = screen.getAllByLabelText('Respiraciones guiadas')
    expect(guidedBreathingLinks.length).toBeGreaterThan(0)
    expect(guidedBreathingLinks[0].closest('a')).toHaveAttribute('href', expectedHref)
  }

  const verifyLinkElementHref = (name: string, expectedHref: string) => {
    expect(screen.getByRole('link', { name })).toHaveAttribute('href', expectedHref)
  }

  const verifyElementImageHasClass = (altText: string, className: string) => {
    const image = screen.getByAltText(altText)
    expect(image).toHaveClass(className)
  }

  const verifyDocumentThemeAttribute = (expectedTheme: string) => {
    expect(document.documentElement.dataset.theme).toBe(expectedTheme)
  }

  const verifyOnboardingWelcomeDialogsPresent = async () => {
    expect(await screen.findByRole('dialog')).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Comenzar con el tutorial' })).toBeInTheDocument()
  }

  const verifyOnboardingWelcomeDialogNotPresent = () => {
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  }

  const verifyTutorialCompletionRegistered = () => {
    expect(completeTutorial).toHaveBeenCalledTimes(1)
    expect(mockRefreshUser).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  }

  const verifyHeadingPresentAsync = async (headingText: string) => {
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: headingText })).toBeInTheDocument()
    })
  }
})
