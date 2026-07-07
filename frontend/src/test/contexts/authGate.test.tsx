import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthGateProvider, useAuthGate } from '../../context/authGate'
import { clickButton, clearAllMocks } from '../testHelpers'


const mockNavigate = vi.fn()
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))





describe('useAuthGate', () => {
  let actionMock: any
  let user: any

  beforeEach(() => {
    clearAllMocks()
    actionMock = vi.fn()
    user = userEvent.setup()
  })

  it('ejecuta la acción si el usuario está autenticado', () => {
    setupAuthMock(true)
    renderGateWithMockAction()
    return clickHacerAccionButton().then(() => {
      verifyActionCalledOnce()
      verifyDialogIsNotInDocument()
    })
  })

  it('abre el modal si el usuario NO está autenticado', () => {
    setupAuthMock(false)
    renderGateWithMockAction()
    return clickHacerAccionButton().then(() => {
      verifyActionNotCalled()
      verifyDialogIsInDocument()
    })
  })

  it('navega a /login desde el modal', () => {
    setupAuthMock(false)
    renderGateWithMockAction()
    return clickHacerAccionButton()
      .then(() => clickIniciarSesionButton())
      .then(() => {
        verifyNavigateCalledWith('/login')
      })
  })

  it('navega a /register desde el modal', () => {
    setupAuthMock(false)
    renderGateWithMockAction()
    return clickHacerAccionButton()
      .then(() => clickRegistrarseButton())
      .then(() => {
        verifyNavigateCalledWith('/register')
      })
  })

  it('cierra el modal con "ahora no" sin navegar', () => {
    setupAuthMock(false)
    renderGateWithMockAction()
    return clickHacerAccionButton()
      .then(() => clickAhoraNoButton())
      .then(() => {
        verifyDialogIsNotInDocument()
        verifyNavigateNotCalled()
      })
  })

  /* helpers */

  const setupAuthMock = (isAuthenticated: boolean) => {
    mockUseAuth.mockReturnValue({ isAuthenticated })
  }

  const renderGateWithMockAction = () => {
    renderWithGate(actionMock)
  }

  const clickHacerAccionButton = () => {
    return clickButton(user, 'Hacer acción')
  }

  const clickIniciarSesionButton = () => {
    return clickButton(user, 'Iniciar sesión')
  }

  const clickRegistrarseButton = () => {
    return clickButton(user, 'Registrarse')
  }

  const clickAhoraNoButton = () => {
    return clickButton(user, 'Ahora no')
  }

  const verifyActionCalledOnce = () => {
    expect(actionMock).toHaveBeenCalledOnce()
  }

  const verifyActionNotCalled = () => {
    expect(actionMock).not.toHaveBeenCalled()
  }

  const verifyDialogIsInDocument = () => {
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  }

  const verifyDialogIsNotInDocument = () => {
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  }

  const verifyNavigateCalledWith = (path: string) => {
    expect(mockNavigate).toHaveBeenCalledWith(path)
  }

  const verifyNavigateNotCalled = () => {
    expect(mockNavigate).not.toHaveBeenCalled()
  }
})

function TestComponent({ action }: { action: () => void }) {
  const { requireAuth } = useAuthGate()
  return <button onClick={() => requireAuth(action)}>Hacer acción</button>
}

function renderWithGate(action: () => void) {
  return render(
    <AuthGateProvider>
      <TestComponent action={action} />
    </AuthGateProvider>,
  )
}
