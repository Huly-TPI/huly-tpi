import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthGateProvider, useAuthGate } from '../../context/authGate'

const mockNavigate = vi.fn()
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

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

describe('useAuthGate', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('ejecuta la acción si el usuario está autenticado', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })
    const action = vi.fn()
    const user = userEvent.setup()
    renderWithGate(action)

    await user.click(screen.getByRole('button', { name: 'Hacer acción' }))

    expect(action).toHaveBeenCalledOnce()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('abre el modal si el usuario NO está autenticado', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })
    const action = vi.fn()
    const user = userEvent.setup()
    renderWithGate(action)

    await user.click(screen.getByRole('button', { name: 'Hacer acción' }))

    expect(action).not.toHaveBeenCalled()
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('navega a /login desde el modal', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })
    const user = userEvent.setup()
    renderWithGate(vi.fn())

    await user.click(screen.getByRole('button', { name: 'Hacer acción' }))
    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    expect(mockNavigate).toHaveBeenCalledWith('/login')
  })

  it('navega a /register desde el modal', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })
    const user = userEvent.setup()
    renderWithGate(vi.fn())

    await user.click(screen.getByRole('button', { name: 'Hacer acción' }))
    await user.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(mockNavigate).toHaveBeenCalledWith('/register')
  })

  it('cierra el modal con "ahora no" sin navegar', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })
    const user = userEvent.setup()
    renderWithGate(vi.fn())

    await user.click(screen.getByRole('button', { name: 'Hacer acción' }))
    await user.click(screen.getByRole('button', { name: /ahora no/i }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})