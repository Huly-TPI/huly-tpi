import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'
import Profile from '../../pages/Profile/Profile'

const mockUseAuth = vi.fn()

vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

const authenticatedUser = {
  id: 1,
  name: 'Jimena Alvarez',
  email: 'jimena@mail.com',
  role: 'USER',
  onBoardingCompleted: true,
  onboardingTutorialCompleted: true,
  themePreference: 'LIGHT',
}

describe('Profile', () => {
  beforeEach(() => {
    window.localStorage.clear()
    window.localStorage.setItem('huly:scene-theme', 'light')
    window.localStorage.setItem('huly:profile-onboarding-seen:v2:1', 'true')
    mockUseAuth.mockReturnValue({
      user: authenticatedUser,
      loading: false,
    })
  })

  const renderProfile = () => {
    return render(
      <ThemeProvider>
        <MemoryRouter>
          <Profile />
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
          <Routes>
            <Route path="/profile" element={<Profile />} />
            <Route path="/login" element={<h1>Vista Login</h1>} />
          </Routes>
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
          <Routes>
            <Route path="/" element={<h1>Vista Jardin</h1>} />
            <Route path="/profile" element={<Profile />} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>,
    )

    await user.click(screen.getByRole('link', { name: 'Volver al jardin' }))

    expect(screen.getByRole('heading', { name: 'Vista Jardin' })).toBeInTheDocument()
  })

  it('muestra el tutorial de perfil la primera vez que se entra', async () => {
    window.localStorage.removeItem('huly:profile-onboarding-seen:v2:1')

    renderProfile()

    expect(await screen.findByRole('dialog')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu habitación personal' })).toBeInTheDocument()
    expect(screen.queryByText('Bienvenido a')).not.toBeInTheDocument()
    expect(screen.queryByRole('img', { name: 'Huly' })).not.toBeInTheDocument()
    expect(window.localStorage.getItem('huly:profile-onboarding-seen:v2:1')).toBeNull()
  })

  it('avanza el tutorial de perfil y lo oculta al finalizar', async () => {
    const user = userEvent.setup()
    window.localStorage.removeItem('huly:profile-onboarding-seen:v2:1')

    renderProfile()

    await user.click(await screen.findByRole('button', { name: 'Comenzar con el tutorial' }))
    expect(screen.getByRole('heading', { name: 'Ventana' })).toBeInTheDocument()
    expect(document.querySelector('.home-onboarding__mascot')).not.toBeInTheDocument()

    for (let index = 0; index < 4; index += 1) {
      await user.click(screen.getByRole('button', { name: 'Siguiente' }))
    }

    await user.click(screen.getByRole('button', { name: 'Finalizar tutorial' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(window.localStorage.getItem('huly:profile-onboarding-seen:v2:1')).toBe('true')
  })

  it('no vuelve a mostrar el tutorial de perfil cuando ya fue visto', () => {
    renderProfile()

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
