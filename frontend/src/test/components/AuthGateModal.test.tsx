import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AuthGateModal from '../../components/AuthGateModal/AuthGateModal'

const defaultProps = {
  open: true,
  onClose: vi.fn(),
  onLogin: vi.fn(),
  onRegister: vi.fn(),
}

describe('AuthGateModal', () => {
  it('no renderiza nada cuando open es false', () => {
    render(<AuthGateModal {...defaultProps} open={false} />)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('renderiza el modal cuando open es true', () => {
    render(<AuthGateModal {...defaultProps} />)
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

 it('muestra el título y el mensaje', () => {
  render(<AuthGateModal {...defaultProps} />)

  expect(
    screen.getByText('¡Necesitás una cuenta!')
  ).toBeInTheDocument()

  expect(
    screen.getByText(
      /iniciá sesión o registrate para disfrutar de todas las funcionalidades de huly/i
    )
  ).toBeInTheDocument()
})

  it('llama a onLogin al hacer click en iniciar sesión', async () => {
    const onLogin = vi.fn()
    const user = userEvent.setup()
    render(<AuthGateModal {...defaultProps} onLogin={onLogin} />)

    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    expect(onLogin).toHaveBeenCalledOnce()
  })

  it('llama a onRegister al hacer click en registrarse', async () => {
    const onRegister = vi.fn()
    const user = userEvent.setup()
    render(<AuthGateModal {...defaultProps} onRegister={onRegister} />)

    await user.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(onRegister).toHaveBeenCalledOnce()
  })

  it('llama a onClose al hacer click en "ahora no"', async () => {
    const onClose = vi.fn()
    const user = userEvent.setup()
    render(<AuthGateModal {...defaultProps} onClose={onClose} />)

    await user.click(screen.getByRole('button', { name: /ahora no/i }))

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('llama a onClose al presionar Escape', async () => {
    const onClose = vi.fn()
    const user = userEvent.setup()
    render(<AuthGateModal {...defaultProps} onClose={onClose} />)

    await user.keyboard('{Escape}')

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('tiene atributos de accesibilidad de diálogo', () => {
    render(<AuthGateModal {...defaultProps} />)
    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-modal', 'true')
  })
})