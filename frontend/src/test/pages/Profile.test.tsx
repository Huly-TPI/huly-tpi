import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'
import { AudioSettingsProvider } from '../../context/audioSettings'
import Profile from '../../pages/Profile/Profile'
import { completeProfileTutorial } from '../../api/onboarding'
import { clickButton, verifyTextPresent, verifyTextNotPresent, verifyPlaceholderPresent, clearAllMocks } from '../testHelpers'

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
  let user: ReturnType<typeof userEvent.setup>

  beforeEach(() => {
    clearAllMocks()
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

  it('muestra el estado de carga mientras obtiene el perfil', () => {
    setupLoadingAuth()
    renderProfilePage()
    verifyLoadingMessageVisible()
  })

  it('redirige a login cuando no hay usuario autenticado', () => {
    setupUnauthenticatedAuth()
    renderProfileWithRoutes('/login', <h1>Vista Login</h1>)
    verifyHeadingVisible('Vista Login')
  })

  it('renderiza la escena y el saludo con el primer nombre del usuario', () => {
    renderProfilePage()
    verifySceneElementsAndGreetingVisible()
  })

  it('renderiza los objetos del perfil con sus hotspots', () => {
    renderProfilePage()
    verifyObjectsAndHotspotsVisible()
  })

  it('renderiza las imagenes de los elementos de escena', () => {
    renderProfilePage()
    verifySceneImagesVisible()
  })

  it('redirige al jardin al hacer click en la ventana', () => {
    renderProfileWithRoutes('/', <h1>Vista Jardin</h1>)
    return clickVolverAlJardinLink().then(() => {
      verifyHeadingVisible('Vista Jardin')
    })
  })

  it('muestra el tutorial de perfil la primera vez que se entra', () => {
    setupNewUserProfileTutorialNotCompleted()
    renderProfilePage()
    return waitForTutorialDialogVisible().then(() => {
      verifyTutorialContent()
    })
  })

  it('avanza el tutorial de perfil y lo oculta al finalizar', () => {
    setupNewUserProfileTutorialNotCompleted()
    renderProfilePage()
    return clickComenzarTutorialButton().then(() => {
      verifyWindowHeadingInTutorial()
      return clickNextInTutorialMultipleTimes(4).then(() => {
        return clickFinalizarTutorialButton().then(() => {
          verifyTutorialFinished()
        })
      })
    })
  })

  it('no vuelve a mostrar el tutorial de perfil cuando ya fue visto', () => {
    renderProfilePage()
    verifyTutorialDialogNotVisible()
  })

  it('abre el modal de audio al hacer click en el equipo de musica', () => {
    renderProfilePage()
    return clickMusicaButton().then(() => {
      verifyAudioModalOpened()
    })
  })

  it('permite cambiar el volumen desde el modal de audio', () => {
    renderProfilePage()
    return clickMusicaButton().then(() => {
      changeBackgroundVolumeSlider('0.25')
      verifyBackgroundVolumeValue('0.25')
    })
  })

  it('abre el modal de cambio de contraseña al hacer click en el Baúl', () => {
    renderProfilePage()
    return clickBaulButton().then(() => {
      verifyChangePasswordModalVisible()
    })
  })

  it('cierra el modal de cambio de contraseña al hacer click en el botón cerrar', () => {
    renderProfilePage()
    return clickBaulButton().then(() => {
      verifyChangePasswordModalVisible()
      return clickCerrarModalButton().then(() => {
        verifyChangePasswordModalNotVisible()
      })
    })
  })

  /* helpers */

  const renderProfilePage = () => {
    user = userEvent.setup()
    render(
      <ThemeProvider>
        <MemoryRouter>
          <AudioSettingsProvider>
            <Profile />
          </AudioSettingsProvider>
        </MemoryRouter>
      </ThemeProvider>
    )
  }

  const renderProfileWithRoutes = (targetPath: string, targetElement: React.ReactNode) => {
    user = userEvent.setup()
    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/profile']}>
          <AudioSettingsProvider>
            <Routes>
              <Route path="/profile" element={<Profile />} />
              <Route path={targetPath} element={targetElement} />
            </Routes>
          </AudioSettingsProvider>
        </MemoryRouter>
      </ThemeProvider>
    )
  }

  const setupLoadingAuth = () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: true,
    })
  }

  const setupUnauthenticatedAuth = () => {
    mockUseAuth.mockReturnValue({
      user: null,
      loading: false,
    })
  }

  const setupNewUserProfileTutorialNotCompleted = () => {
    mockUseAuth.mockReturnValue({
      user: {
        ...authenticatedUser,
        profileOnboardingTutorialCompleted: false,
      },
      loading: false,
      refreshUser: mockRefreshUser,
    })
  }

  const verifyLoadingMessageVisible = () => {
    verifyTextPresent('Cargando perfil...')
  }

  const verifyHeadingVisible = (text: string) => {
    expect(screen.getByRole('heading', { name: text })).toBeInTheDocument()
  }

  const verifySceneElementsAndGreetingVisible = () => {
    expect(screen.getByLabelText('Perfil de usuario')).toBeInTheDocument()
    expect(screen.getByLabelText('Habitacion de perfil')).toBeInTheDocument()
    expect(screen.getByLabelText('Bienvenido Jimena')).toBeInTheDocument()
  }

  const verifyObjectsAndHotspotsVisible = () => {
    expect(screen.getByRole('button', { name: 'Espejo' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Volver al jardin' })).toHaveAttribute('href', '/')
    expect(screen.getByRole('button', { name: 'Baúl' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Reloj' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Musica' })).toBeInTheDocument()
  }

  const verifySceneImagesVisible = () => {
    expect(screen.getByAltText('Espejo del perfil')).toBeInTheDocument()
    expect(screen.getByAltText('Ventana hacia el jardin')).toBeInTheDocument()
    expect(screen.getByAltText('Baúl del perfil')).toBeInTheDocument()
    expect(screen.getByAltText('Reloj del perfil')).toBeInTheDocument()
    expect(screen.getByAltText('Equipo de musica del perfil')).toBeInTheDocument()
  }

  const clickVolverAlJardinLink = () => {
    return user.click(screen.getByRole('link', { name: 'Volver al jardin' }))
  }

  const waitForTutorialDialogVisible = () => {
    return screen.findByRole('dialog').then(el => {
      expect(el).toBeInTheDocument()
    })
  }

  const verifyTutorialContent = () => {
    expect(screen.getByRole('heading', { name: 'Tu habitación personal' })).toBeInTheDocument()
    verifyTextNotPresent('Bienvenido a')
    expect(screen.queryByRole('img', { name: 'Huly' })).not.toBeInTheDocument()
    expect(completeProfileTutorial).not.toHaveBeenCalled()
  }

  const clickComenzarTutorialButton = () => {
    return screen.findByRole('button', { name: 'Comenzar con el tutorial' }).then(btn => {
      return user.click(btn)
    })
  }

  const verifyWindowHeadingInTutorial = () => {
    expect(screen.getByRole('heading', { name: 'Ventana' })).toBeInTheDocument()
    expect(document.querySelector('.home-onboarding__mascot')).not.toBeInTheDocument()
  }

  const clickNextInTutorialMultipleTimes = (times: number) => {
    let p = Promise.resolve()
    for (let i = 0; i < times; i++) {
      p = p.then(() => clickButton(user, 'Siguiente'))
    }
    return p
  }

  const clickFinalizarTutorialButton = () => {
    return clickButton(user, 'Finalizar tutorial')
  }

  const verifyTutorialFinished = () => {
    expect(completeProfileTutorial).toHaveBeenCalledTimes(1)
    expect(mockRefreshUser).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  }

  const verifyTutorialDialogNotVisible = () => {
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  }

  const clickMusicaButton = () => {
    return clickButton(user, 'Musica')
  }

  const verifyAudioModalOpened = () => {
    expect(screen.getByRole('dialog', { name: 'Ambiente sonoro' })).toBeInTheDocument()
    expect(screen.getByRole('slider', { name: 'Volumen de interfaz' })).toBeInTheDocument()
    expect(screen.getByRole('slider', { name: 'Volumen de fondo' })).toBeInTheDocument()
    expect(screen.getByRole('slider', { name: 'Volumen de minijuegos' })).toBeInTheDocument()
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument()
  }

  const changeBackgroundVolumeSlider = (value: string) => {
    const volumeSlider = screen.getByRole('slider', { name: 'Volumen de fondo' })
    fireEvent.change(volumeSlider, { target: { value } })
  }

  const verifyBackgroundVolumeValue = (value: string) => {
    const volumeSlider = screen.getByRole('slider', { name: 'Volumen de fondo' })
    expect(volumeSlider).toHaveValue(value)
  }

  const clickBaulButton = () => {
    return clickButton(user, 'Baúl')
  }

  const verifyChangePasswordModalVisible = () => {
    expect(screen.getByRole('dialog', { name: 'Cambiar contraseña' })).toBeInTheDocument()
    verifyPlaceholderPresent('Contraseña actual')
    verifyPlaceholderPresent('Nueva contraseña')
  }

  const clickCerrarModalButton = () => {
    return clickButton(user, 'Cerrar modal')
  }

  const verifyChangePasswordModalNotVisible = () => {
    expect(screen.queryByRole('dialog', { name: 'Cambiar contraseña' })).not.toBeInTheDocument()
  }
})
