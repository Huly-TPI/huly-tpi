import { beforeEach, describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Home from '../../pages/Home/Home'
import { ThemeProvider } from '../../context/theme'
import { completeTutorial } from '../../api/onboarding'

const mockUseAuth = vi.fn()
const mockRefreshUser = vi.fn()
vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('../../api/onboarding', () => ({
  completeTutorial: vi.fn(),
}))

describe('Home', () => {
  beforeEach(() => {
    window.localStorage.clear()
    window.localStorage.setItem('huly:scene-theme', 'light')
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        name: 'Jimena',
        email: 'jimena@mail.com',
        role: 'USER',
        onBoardingCompleted: true,
        onboardingTutorialCompleted: true,
      },
      refreshUser: mockRefreshUser,
    })
    vi.mocked(completeTutorial).mockResolvedValue(undefined)
    mockRefreshUser.mockResolvedValue(undefined)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  const renderWithRouter = () => {
    return render(
      <ThemeProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </ThemeProvider>
    )
  }

  it('renderiza el fondo del jardin', () => {
    const { container } = renderWithRouter()
    const activeLightBackgrounds = container.querySelectorAll(
      '.garden-scene__background--light.garden-scene__background--active',
    )

    expect(activeLightBackgrounds.length).toBeGreaterThan(0)
  })

  it('activa el fondo oscuro al alternar a modo noche', async () => {
    const user = userEvent.setup()
    const { container } = renderWithRouter()

    await user.click(screen.getByRole('button', { name: 'Cambiar a modo noche' }))

    const activeDarkBackgrounds = container.querySelectorAll(
      '.garden-scene__background--dark.garden-scene__background--active',
    )
    const activeLightBackgrounds = container.querySelectorAll(
      '.garden-scene__background--light.garden-scene__background--active',
    )

    expect(activeDarkBackgrounds.length).toBeGreaterThan(0)
    expect(activeLightBackgrounds.length).toBe(0)
  })

  it('renderiza hotspots navegables principales', () => {
    renderWithRouter()
    expect(screen.getByLabelText('Perfil').closest('a')).toHaveAttribute('href', '/profile')
    expect(screen.getByLabelText('Diario').closest('a')).toHaveAttribute('href', '/diary')
    expect(screen.getByLabelText('Minijuegos').closest('a')).toHaveAttribute('href', '/minigames')
    expect(screen.getByLabelText('Pendientes').closest('a')).toHaveAttribute('href', '/pending')
    expect(screen.getByLabelText('Retos').closest('a')).toHaveAttribute('href', '/challenges')
  })

  it('renderiza acceso a respiraciones guiadas desde las nubes', () => {
    renderWithRouter()
    const guidedBreathingLinks = screen.getAllByLabelText('Respiraciones guiadas')
    expect(guidedBreathingLinks.length).toBeGreaterThan(0)
    expect(guidedBreathingLinks[0].closest('a')).toHaveAttribute('href', '/guided-breathing')
  })

  it('redirige a diario al hacer click en el hotspot Diario', async () => {
    const user = userEvent.setup()

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

    await user.click(screen.getByLabelText('Diario'))
    expect(screen.getByRole('heading', { name: 'Vista Diario' })).toBeInTheDocument()
  })

  it('permite alternar entre modo noche y modo dia', async () => {
    const user = userEvent.setup()
    renderWithRouter()

    const toggleButton = screen.getByRole('button', { name: 'Cambiar a modo noche' })
    await user.click(toggleButton)

    expect(screen.getByRole('button', { name: 'Cambiar a modo dia' })).toBeInTheDocument()
  })

  it('mantiene accesos habilitados en modo noche por defecto', async () => {
    const user = userEvent.setup()
    renderWithRouter()

    await user.click(screen.getByRole('button', { name: 'Cambiar a modo noche' }))

    expect(screen.getByRole('link', { name: 'Minijuegos' })).toHaveAttribute('href', '/minigames')
    expect(screen.getByRole('link', { name: 'Retos' })).toHaveAttribute('href', '/challenges')
    expect(screen.getByRole('link', { name: 'Diario' })).toHaveAttribute('href', '/diary')
  })

  it('renderiza el banco con la clase de espejo mobile', () => {
    renderWithRouter()

    const benchImage = screen.getByAltText('Banco con cuaderno en el jardin')
    expect(benchImage).toHaveClass('scene-element__image--mirror-mobile')
  })

  it('muestra el onboarding de bienvenida en el primer ingreso', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        name: 'Jimena',
        email: 'jimena@mail.com',
        role: 'USER',
        onBoardingCompleted: true,
        onboardingTutorialCompleted: false,
      },
      refreshUser: mockRefreshUser,
    })

    renderWithRouter()

    expect(await screen.findByRole('dialog')).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Comenzar con el tutorial' })).toBeInTheDocument()
  })

  it('no muestra tutorial de home si onboarding emocional no esta completo', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        name: 'Jimena',
        email: 'jimena@mail.com',
        role: 'USER',
        onBoardingCompleted: false,
      },
      refreshUser: mockRefreshUser,
    })

    renderWithRouter()

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('avanza el onboarding y lo marca como completado al finalizar', async () => {
    const user = userEvent.setup()
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        name: 'Jimena',
        email: 'jimena@mail.com',
        role: 'USER',
        onBoardingCompleted: true,
        onboardingTutorialCompleted: false,
      },
      refreshUser: mockRefreshUser,
    })

    renderWithRouter()

    await user.click(await screen.findByRole('button', { name: 'Comenzar con el tutorial' }))
    expect(screen.getByText('Casa')).toBeInTheDocument()

    for (let index = 0; index < 5; index += 1) {
      await user.click(screen.getByRole('button', { name: 'Siguiente' }))
    }

    await user.click(screen.getByRole('button', { name: 'Finalizar tutorial' }))

    expect(completeTutorial).toHaveBeenCalledTimes(1)
    expect(mockRefreshUser).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
