import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChangePasswordModal from '../../components/Profile/ChangePasswordModal'
import { clickButton, typePlaceholder, verifyTextPresent, verifyPlaceholderPresent, verifyButtonDisabled, clearAllMocks } from '../testHelpers'

const mockChangePassword = vi.fn()
vi.mock('../../api/auth', () => ({
  changePassword: (...args: unknown[]) => mockChangePassword(...args),
}))

describe('ChangePasswordModal', () => {
  let onCloseMock: any

  beforeEach(() => {
    clearAllMocks()
    onCloseMock = vi.fn()
  })

  it('renderiza el dialog con el título correcto', () => {
    renderModal()
    verifyDialogAndHeadingPresent()
  })

  it('tiene atributos de accesibilidad de diálogo', () => {
    renderModal()
    verifyAccessibilityAttributes()
  })

  it('muestra los campos de contraseña actual y nueva', () => {
    renderModal()
    verifyFieldsPresent()
  })

  it('llama a onClose al hacer click en el botón de cerrar', () => {
    renderModal()
    return clickCloseButton().then(() => {
      verifyOnCloseCalled()
    })
  })

  it('muestra errores de validación si se envía el formulario vacío', () => {
    renderModal()
    return clickSaveButton().then(() => {
      verifyValidationErrorsForEmptyForm()
    })
  })

  it('muestra error si la nueva contraseña tiene menos de 6 caracteres', () => {
    renderModal()
    return fillFormFields('password123', '123')
      .then(() => clickSaveButton())
      .then(() => {
        verifyValidationErrorForShortPassword()
      })
  })

  it('llama a changePassword con los valores correctos al enviar', () => {
    setupChangePasswordResolved()
    renderModal()
    return fillFormFields('currentPass123', 'newPass456')
      .then(() => clickSaveButton())
      .then(() => verifyChangePasswordCalledWith('currentPass123', 'newPass456'))
  })

  it('muestra mensaje de éxito y limpia los campos tras el cambio', () => {
    setupChangePasswordResolved()
    renderModal()
    return fillFormFields('currentPass123', 'newPass456')
      .then(() => clickSaveButton())
      .then(() => verifySuccessMessageAndClearedFields())
  })

  it('muestra error de contraseña incorrecta cuando el backend retorna 401', () => {
    setupChangePasswordRejected(401)
    renderModal()
    return fillFormFields('wrongPassword', 'newPass456')
      .then(() => clickSaveButton())
      .then(() => verifyErrorMessagePresent('La contraseña actual es incorrecta.'))
  })

  it('muestra error de contraseña incorrecta cuando el backend retorna 400', () => {
    setupChangePasswordRejected(400)
    renderModal()
    return fillFormFields('wrongPassword', 'newPass456')
      .then(() => clickSaveButton())
      .then(() => verifyErrorMessagePresent('La contraseña actual es incorrecta.'))
  })

  it('muestra error genérico ante errores inesperados del servidor', () => {
    setupChangePasswordRejected(500)
    renderModal()
    return fillFormFields('currentPass123', 'newPass456')
      .then(() => clickSaveButton())
      .then(() => verifyErrorMessagePresent('Ocurrió un error. Intentá de nuevo.'))
  })

  it('deshabilita el botón y muestra "Guardando..." mientras se envía', () => {
    setupChangePasswordPromise()
    renderModal()
    return fillFormFields('currentPass123', 'newPass456')
      .then(() => clickSaveButton())
      .then(() => {
        verifySavingState()
      })
  })

  /* helpers */

  const renderModal = () => {
    render(<ChangePasswordModal onClose={onCloseMock} />)
  }

  const verifyDialogAndHeadingPresent = () => {
    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Cambiar contraseña' })).toBeInTheDocument()
  }

  const verifyAccessibilityAttributes = () => {
    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-modal', 'true')
  }

  const verifyFieldsPresent = () => {
    verifyPlaceholderPresent('Contraseña actual')
    verifyPlaceholderPresent('Nueva contraseña')
  }

  const clickCloseButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Cerrar modal')
  }

  const verifyOnCloseCalled = () => {
    expect(onCloseMock).toHaveBeenCalledOnce()
  }

  const clickSaveButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Guardar')
  }

  const verifyValidationErrorsForEmptyForm = () => {
    verifyTextPresent('Ingresá tu contraseña actual')
    verifyTextPresent('Ingresá la nueva contraseña')
    expect(mockChangePassword).not.toHaveBeenCalled()
  }

  const fillFormFields = (current: string, newPass: string) => {
    const user = userEvent.setup()
    return typePlaceholder(user, 'Contraseña actual', current)
      .then(() => typePlaceholder(user, 'Nueva contraseña', newPass))
  }

  const verifyValidationErrorForShortPassword = () => {
    verifyTextPresent('La contraseña debe tener al menos 6 caracteres')
    expect(mockChangePassword).not.toHaveBeenCalled()
  }

  const setupChangePasswordResolved = () => {
    mockChangePassword.mockResolvedValueOnce(undefined)
  }

  const setupChangePasswordPromise = () => {
    mockChangePassword.mockImplementation(() => new Promise(() => {}))
  }

  const setupChangePasswordRejected = (status: number) => {
    mockChangePassword.mockRejectedValueOnce({ response: { status } })
  }

  const verifyChangePasswordCalledWith = (current: string, newPass: string) => {
    return waitFor(() => {
      expect(mockChangePassword).toHaveBeenCalledWith(current, newPass)
    })
  }

  const verifySuccessMessageAndClearedFields = () => {
    return waitFor(() => {
      verifyTextPresent('¡Contraseña actualizada con éxito!')
    }).then(() => {
      expect(screen.getByPlaceholderText('Contraseña actual')).toHaveValue('')
      expect(screen.getByPlaceholderText('Nueva contraseña')).toHaveValue('')
    })
  }

  const verifyErrorMessagePresent = (message: string) => {
    return waitFor(() => {
      verifyTextPresent(message)
    })
  }

  const verifySavingState = () => {
    verifyButtonDisabled('Guardando...')
  }
})
