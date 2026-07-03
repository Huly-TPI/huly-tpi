import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChangePasswordModal from '../../components/Profile/ChangePasswordModal'

const mockChangePassword = vi.fn()
vi.mock('../../api/auth', () => ({
  changePassword: (...args: unknown[]) => mockChangePassword(...args),
}))

const defaultProps = {
  onClose: vi.fn(),
}

describe('ChangePasswordModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const fillForm = async (
    user: ReturnType<typeof userEvent.setup>,
    currentPassword: string,
    newPassword: string,
  ) => {
    await user.type(screen.getByPlaceholderText('Contraseña actual'), currentPassword)
    await user.type(screen.getByPlaceholderText('Nueva contraseña'), newPassword)
  }

  it('renderiza el dialog con el título correcto', () => {
    render(<ChangePasswordModal {...defaultProps} />)

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Cambiar contraseña' })).toBeInTheDocument()
  })

  it('tiene atributos de accesibilidad de diálogo', () => {
    render(<ChangePasswordModal {...defaultProps} />)

    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-modal', 'true')
  })

  it('muestra los campos de contraseña actual y nueva', () => {
    render(<ChangePasswordModal {...defaultProps} />)

    expect(screen.getByPlaceholderText('Contraseña actual')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Nueva contraseña')).toBeInTheDocument()
  })

  it('llama a onClose al hacer click en el botón de cerrar', async () => {
    const onClose = vi.fn()
    const user = userEvent.setup()
    render(<ChangePasswordModal onClose={onClose} />)

    await user.click(screen.getByRole('button', { name: 'Cerrar modal' }))

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('muestra errores de validación si se envía el formulario vacío', async () => {
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(screen.getByText('Ingresá tu contraseña actual')).toBeInTheDocument()
    expect(screen.getByText('Ingresá la nueva contraseña')).toBeInTheDocument()
    expect(mockChangePassword).not.toHaveBeenCalled()
  })

  it('muestra error si la nueva contraseña tiene menos de 6 caracteres', async () => {
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await user.type(screen.getByPlaceholderText('Contraseña actual'), 'password123')
    await user.type(screen.getByPlaceholderText('Nueva contraseña'), '123')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(screen.getByText('La contraseña debe tener al menos 6 caracteres')).toBeInTheDocument()
    expect(mockChangePassword).not.toHaveBeenCalled()
  })

  it('llama a changePassword con los valores correctos al enviar', async () => {
    mockChangePassword.mockResolvedValueOnce(undefined)
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await fillForm(user, 'currentPass123', 'newPass456')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    await waitFor(() => {
      expect(mockChangePassword).toHaveBeenCalledWith('currentPass123', 'newPass456')
    })
  })

  it('muestra mensaje de éxito y limpia los campos tras el cambio', async () => {
    mockChangePassword.mockResolvedValueOnce(undefined)
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await fillForm(user, 'currentPass123', 'newPass456')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    await waitFor(() => {
      expect(screen.getByText('¡Contraseña actualizada con éxito!')).toBeInTheDocument()
    })
    expect(screen.getByPlaceholderText('Contraseña actual')).toHaveValue('')
    expect(screen.getByPlaceholderText('Nueva contraseña')).toHaveValue('')
  })

  it('muestra error de contraseña incorrecta cuando el backend retorna 401', async () => {
    mockChangePassword.mockRejectedValueOnce({ response: { status: 401 } })
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await fillForm(user, 'wrongPassword', 'newPass456')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    await waitFor(() => {
      expect(screen.getByText('La contraseña actual es incorrecta.')).toBeInTheDocument()
    })
  })

  it('muestra error de contraseña incorrecta cuando el backend retorna 400', async () => {
    mockChangePassword.mockRejectedValueOnce({ response: { status: 400 } })
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await fillForm(user, 'wrongPassword', 'newPass456')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    await waitFor(() => {
      expect(screen.getByText('La contraseña actual es incorrecta.')).toBeInTheDocument()
    })
  })

  it('muestra error genérico ante errores inesperados del servidor', async () => {
    mockChangePassword.mockRejectedValueOnce({ response: { status: 500 } })
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await fillForm(user, 'currentPass123', 'newPass456')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    await waitFor(() => {
      expect(screen.getByText('Ocurrió un error. Intentá de nuevo.')).toBeInTheDocument()
    })
  })

  it('deshabilita el botón y muestra "Guardando..." mientras se envía', async () => {
    mockChangePassword.mockImplementation(() => new Promise(() => {}))
    const user = userEvent.setup()
    render(<ChangePasswordModal {...defaultProps} />)

    await fillForm(user, 'currentPass123', 'newPass456')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(screen.getByRole('button', { name: 'Guardando...' })).toBeDisabled()
  })
})
