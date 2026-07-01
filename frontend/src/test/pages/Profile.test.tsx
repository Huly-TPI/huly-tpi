import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'
import { AudioSettingsProvider } from '../../context/audioSettings'
import Profile from '../../pages/Profile/Profile'
import { completeProfileTutorial } from '../../api/onboarding'

const mockUseAuth = vi.fn()
const mockRefreshUser = vi.fn()

vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('../../api/onboarding', () => ({
  completeProfileTutorial: vi.fn(),
}))

vi.mock('../../api/auth', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/auth')>()
  return {
    ...actual,
    changePassword: vi.fn(),
  }
})

const authenticatedUser = {
  id: 1,
  name: 'Jimena Alvarez',
  email: 'jimena@mail.com',
  role: 'USER',
  onBoardingCompleted: true,
  onboardingTutorialCompleted: true,
  profileOnboardingTutorialCompleted: true,
  themePreference: 'LIGHT',
}

describe('Profile', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.localStorage.clear()
    window.localStorage.setItem('huly:scene-theme', 'light')
    mockUseAuth.mockReturnValue({
      user: authenticatedUser,
      loading: false,
      refreshUser: mockRefreshUser,
    })
    vi.mocked(completeProfileTutorial).mockResolvedValue(undefined)
    mockRefreshUser.mockResolvedValue(authenticatedUser)
  })

  const renderProfile = () => {
    return render(
      <ThemeProvider>
        <MemoryRouter>
          <AudioSettingsProvider>
            <Profile />
          </AudioSettingsProvider>
        </MemoryRouter>
      </ThemeProvider>,
    )
  }

  it('muestra el estado de carga mientras obtiene el perfil', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: true,
    })

    renderProfile()

    expect(screen.getByText('Cargando perfil...')).toBeInTheDocument()
  })

  it('redirige a login cuando no hay usuario autenticado', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: false,
    })

    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/profile']}>
          <AudioSettingsProvider>
            <Routes>
              <Route path="/profile" element={<Profile />} />
              <Route path="/login" element={<h1>Vista Login</h1>} />
            </Routes>
          </AudioSettingsProvider>
        </MemoryRouter>
      </ThemeProvider>,
    )

    expect(screen.getByRole('heading', { name: 'Vista Login' })).toBeInTheDocument()
  })

  it('renderiza la escena y el saludo con el primer nombre del usuario', () => {
    renderProfile()

    expect(screen.getByLabelText('Perfil de usuario')).toBeInTheDocument()
    expect(screen.getByLabelText('Habitacion de perfil')).toBeInTheDocument()
    expect(screen.getByLabelText('Bienvenido Jimena')).toBeInTheDocument()
  })

  it('renderiza los objetos del perfil con sus hotspots', () => {
    renderProfile()

    expect(screen.getByRole('button', { name: 'Espejo' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Volver al jardin' })).toHaveAttribute('href', '/')
    expect(screen.getByRole('button', { name: 'Baúl' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Reloj' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Musica' })).toBeInTheDocument()
  })

  it('renderiza las imagenes de los elementos de escena', () => {
    renderProfile()

    expect(screen.getByAltText('Espejo del perfil')).toBeInTheDocument()
    expect(screen.getByAltText('Ventana hacia el jardin')).toBeInTheDocument()
    expect(screen.getByAltText('Baúl del perfil')).toBeInTheDocument()
    expect(screen.getByAltText('Reloj del perfil')).toBeInTheDocument()
    expect(screen.getByAltText('Equipo de musica del perfil')).toBeInTheDocument()
  })

  it('redirige al jardin al hacer click en la ventana', async () => {
    const user = userEvent.setup()

    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/profile']}>
          <AudioSettingsProvider>
            <Routes>
              <Route path="/" element={<h1>Vista Jardin</h1>} />
              <Route path="/profile" element={<Profile />} />
            </Routes>
          </AudioSettingsProvider>
        </MemoryRouter>
      </ThemeProvider>,
    )

    await user.click(screen.getByRole('link', { name: 'Volver al jardin' }))

    expect(screen.getByRole('heading', { name: 'Vista Jardin' })).toBeInTheDocument()
  })

  it('muestra el tutorial de perfil la primera vez que se entra', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        ...authenticatedUser,
        profileOnboardingTutorialCompleted: false,
      },
      loading: false,
      refreshUser: mockRefreshUser,
    })

    renderProfile()

    expect(await screen.findByRole('dialog')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu habitación personal' })).toBeInTheDocument()
    expect(screen.queryByText('Bienvenido a')).not.toBeInTheDocument()
    expect(screen.queryByRole('img', { name: 'Huly' })).not.toBeInTheDocument()
    expect(completeProfileTutorial).not.toHaveBeenCalled()
  })

  it('avanza el tutorial de perfil y lo oculta al finalizar', async () => {
    const user = userEvent.setup()
    mockUseAuth.mockReturnValue({
      user: {
        ...authenticatedUser,
        profileOnboardingTutorialCompleted: false,
      },
      loading: false,
      refreshUser: mockRefreshUser,
    })

    renderProfile()

    await user.click(await screen.findByRole('button', { name: 'Comenzar con el tutorial' }))
    expect(screen.getByRole('heading', { name: 'Ventana' })).toBeInTheDocument()
    expect(document.querySelector('.home-onboarding__mascot')).not.toBeInTheDocument()

    for (let index = 0; index < 4; index += 1) {
      await user.click(screen.getByRole('button', { name: 'Siguiente' }))
    }

    await user.click(screen.getByRole('button', { name: 'Finalizar tutorial' }))

    expect(completeProfileTutorial).toHaveBeenCalledTimes(1)
    expect(mockRefreshUser).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('no vuelve a mostrar el tutorial de perfil cuando ya fue visto', () => {
    renderProfile()

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('abre el modal de audio al hacer click en el equipo de musica', async () => {
    const user = userEvent.setup()

    renderProfile()

    await user.click(screen.getByRole('button', { name: 'Musica' }))

    expect(screen.getByRole('dialog', { name: 'Ambiente sonoro' })).toBeInTheDocument()
    expect(screen.getByRole('slider', { name: 'Volumen de interfaz' })).toBeInTheDocument()
    expect(screen.getByRole('slider', { name: 'Volumen de fondo' })).toBeInTheDocument()
    expect(screen.getByRole('slider', { name: 'Volumen de minijuegos' })).toBeInTheDocument()
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument()
  })

  it('permite cambiar el volumen desde el modal de audio', async () => {
    const user = userEvent.setup()

    renderProfile()

    await user.click(screen.getByRole('button', { name: 'Musica' }))
    const volumeSlider = screen.getByRole('slider', { name: 'Volumen de fondo' })

    fireEvent.change(volumeSlider, { target: { value: '0.25' } })

    expect(volumeSlider).toHaveValue('0.25')
  })

  it('abre el modal de cambio de contraseña al hacer click en el Baúl', async () => {
    const user = userEvent.setup()

    renderProfile()

    await user.click(screen.getByRole('button', { name: 'Baúl' }))

    expect(screen.getByRole('dialog', { name: 'Cambiar contraseña' })).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Contraseña actual')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Nueva contraseña')).toBeInTheDocument()
  })

  it('cierra el modal de cambio de contraseña al hacer click en el botón cerrar', async () => {
    const user = userEvent.setup()

    renderProfile()

    await user.click(screen.getByRole('button', { name: 'Baúl' }))
    expect(screen.getByRole('dialog', { name: 'Cambiar contraseña' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Cerrar modal' }))
    expect(screen.queryByRole('dialog', { name: 'Cambiar contraseña' })).not.toBeInTheDocument()
  })
})
