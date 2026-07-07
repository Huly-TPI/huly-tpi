import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { act } from 'react'
import { MemoryRouter } from 'react-router-dom'
import ChatbotLauncher from '../../components/Chatbot/ChatbotLauncher'
import { verifyTextPresent, clearAllMocks } from '../testHelpers'

const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('../../components/Chatbot/ChatbotModal', () => ({
  default: ({ isOpen }: { isOpen: boolean }) => (
    <div>{isOpen ? 'modal-open' : 'modal-closed'}</div>
  ),
}))

describe('ChatbotLauncher', () => {
  beforeEach(() => {
    clearAllMocks()
    document.body.removeAttribute('data-home-onboarding-active')
  })

  it('no renderiza el launcher cuando no está autenticado', () => {
    setupAuth(false)
    renderLauncher()
    verifyLauncherButtonNotPresent()
  })

  it('renderiza el launcher cuando está autenticado y abre el modal', () => {
    setupAuth(true)
    renderLauncher()
    return verifyLauncherButtonPresent()
      .then(() => clickLauncherButton())
      .then(() => verifyModalOpen())
  })

  it('no renderiza el launcher en la ruta de onboarding emocional', () => {
    setupAuth(true)
    renderLauncher('/onboarding')
    verifyLauncherButtonNotPresent()
  })

  it('no renderiza el launcher mientras el onboarding de inicio está activo', () => {
    setupAuth(true)
    setupHomeOnboardingActive()
    renderLauncher()
    verifyLauncherButtonNotPresent()
  })

  it('oculta el launcher cuando el onboarding de inicio se activa después de renderizar', () => {
    setupAuth(true)
    renderLauncher()
    return verifyLauncherButtonPresent().then(() => {
      triggerHomeOnboardingVisibilityChange()
      verifyLauncherButtonNotPresent()
    })
  })

  /* helpers */

  const setupAuth = (isAuthenticated: boolean) => {
    mockUseAuth.mockReturnValue({ isAuthenticated })
  }

  const renderLauncher = (initialRoute: string = '/') => {
    render(
      <MemoryRouter initialEntries={[initialRoute]}>
        <ChatbotLauncher />
      </MemoryRouter>
    )
  }

  const verifyLauncherButtonNotPresent = () => {
    expect(screen.queryByRole('button', { name: 'Abrir chat de Huly' })).not.toBeInTheDocument()
  }

  const verifyLauncherButtonPresent = () => {
    return screen.findByRole('button', { name: 'Abrir chat de Huly' }).then(button => {
      expect(button).toBeInTheDocument()
    })
  }

  const clickLauncherButton = () => {
    const user = userEvent.setup()
    return screen.findByRole('button', { name: 'Abrir chat de Huly' }).then(button => {
      return user.click(button)
    })
  }

  const verifyModalOpen = () => {
    verifyTextPresent('modal-open')
  }

  const setupHomeOnboardingActive = () => {
    document.body.setAttribute('data-home-onboarding-active', 'true')
    window.dispatchEvent(new CustomEvent('home-onboarding-visibility-change'))
  }

  const triggerHomeOnboardingVisibilityChange = () => {
    act(() => {
      document.body.setAttribute('data-home-onboarding-active', 'true')
      window.dispatchEvent(new CustomEvent('home-onboarding-visibility-change'))
    })
  }
})
