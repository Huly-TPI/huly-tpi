import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AuthGateModal from '../../components/AuthGateModal/AuthGateModal'
import { clickButton } from '../testHelpers'

describe('AuthGateModal', () => {
  let onCloseMock: any
  let onLoginMock: any
  let onRegisterMock: any

  beforeEach(() => {
    onCloseMock = vi.fn()
    onLoginMock = vi.fn()
    onRegisterMock = vi.fn()
  })

  it('no renderiza nada cuando open es false', () => {
    renderModal(false)
    verifyDialogNotPresent()
  })

  it('renderiza el modal cuando open es true', () => {
    renderModal(true)
    verifyDialogPresent()
  })

  it('muestra el título y el mensaje', () => {
    renderModal(true)
    verifyTitleAndMessagePresent()
  })

  it('llama a onLogin al hacer click en iniciar sesión', () => {
    renderModal(true)
    return clickLoginButton().then(() => {
      verifyLoginCalled()
    })
  })

  it('llama a onRegister al hacer click en registrarse', () => {
    renderModal(true)
    return clickRegisterButton().then(() => {
      verifyRegisterCalled()
    })
  })

  it('llama a onClose al hacer click en "ahora no"', () => {
    renderModal(true)
    return clickCancelButton().then(() => {
      verifyCloseCalled()
    })
  })

  it('llama a onClose al presionar Escape', () => {
    renderModal(true)
    return pressEscapeKey().then(() => {
      verifyCloseCalled()
    })
  })

  it('tiene atributos de accesibilidad de diálogo', () => {
    renderModal(true)
    verifyDialogAccessibilityAttributes()
  })

  /* helpers */

  const renderModal = (isOpen: boolean = true) => {
    render(
      <AuthGateModal
        open={isOpen}
        onClose={onCloseMock}
        onLogin={onLoginMock}
        onRegister={onRegisterMock}
      />
    )
  }

  const verifyDialogNotPresent = () => {
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  }

  const verifyDialogPresent = () => {
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  }

  const verifyTitleAndMessagePresent = () => {
    expect(screen.getByText('¡Necesitás una cuenta!')).toBeInTheDocument()
    expect(
      screen.getByText(/iniciá sesión o registrate para disfrutar de todas las funcionalidades de huly/i)
    ).toBeInTheDocument()
  }

  const clickLoginButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Iniciar sesión')
  }

  const verifyLoginCalled = () => {
    expect(onLoginMock).toHaveBeenCalledOnce()
  }

  const clickRegisterButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Registrarse')
  }

  const verifyRegisterCalled = () => {
    expect(onRegisterMock).toHaveBeenCalledOnce()
  }

  const clickCancelButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Ahora no')
  }

  const verifyCloseCalled = () => {
    expect(onCloseMock).toHaveBeenCalledOnce()
  }

  const pressEscapeKey = () => {
    const user = userEvent.setup()
    return user.keyboard('{Escape}')
  }

  const verifyDialogAccessibilityAttributes = () => {
    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-modal', 'true')
  }
})