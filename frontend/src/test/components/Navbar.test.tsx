import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import Navbar from '../../components/Navbar'

const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

describe('Navbar', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      logout: vi.fn(),
    })
  })

  const renderWithRouter = () =>
    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>,
    )

  it('renderiza el logo de Huly', () => {
    renderWithRouter()
    expect(screen.getByAltText('Huly logo')).toBeInTheDocument()
  })

  it('renderiza todos los links de navegación', () => {
    renderWithRouter()
    expect(screen.getByText('Jardín')).toBeInTheDocument()
    expect(screen.getByText('Pendientes')).toBeInTheDocument()
    expect(screen.getByText('Minijuegos')).toBeInTheDocument()
    expect(screen.getByText('Diario')).toBeInTheDocument()
    expect(screen.getByText('Regar planta')).toBeInTheDocument()
  })

  it('cada link tiene el href correcto', () => {
    renderWithRouter()
    expect(screen.getByText('Jardín').closest('a')).toHaveAttribute('href', '/')
    expect(screen.getByText('Pendientes').closest('a')).toHaveAttribute('href', '/pending')
    expect(screen.getByText('Minijuegos').closest('a')).toHaveAttribute('href', '/minigames')
    expect(screen.getByText('Diario').closest('a')).toHaveAttribute('href', '/diary')
    expect(screen.getByText('Regar planta').closest('a')).toHaveAttribute('href', '/challenges')
  })

  describe('usuario deslogueado', () => {
    it('muestra los botones de iniciar sesión y registrarse', () => {
      renderWithRouter()
      expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toBeInTheDocument()
      expect(screen.getByRole('link', { name: 'Registrarse' })).toBeInTheDocument()
    })
  })

  describe('usuario logueado', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { id: 1, name: 'Mili', email: 'mili@huly.com', role: 'USER' },
        logout: vi.fn(),
      })
    })

    it('muestra el nombre del usuario', () => {
      renderWithRouter()
      expect(screen.getByText('Mili')).toBeInTheDocument()
    })

    it('no muestra los botones de auth', () => {
      renderWithRouter()
      expect(screen.queryByRole('link', { name: 'Iniciar sesión' })).not.toBeInTheDocument()
    })

    it('abre el dropdown al hacer click en el botón de usuario', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: /Mili/ }))

      expect(screen.getByRole('menuitem', { name: 'Mi perfil' })).toBeInTheDocument()
      expect(screen.getByRole('menuitem', { name: 'Cerrar sesión' })).toBeInTheDocument()
    })
  })

  describe('menú mobile', () => {
    it('abre el menú al hacer click en el hamburguesa', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: 'Abrir menú' }))

      expect(screen.getAllByText('Jardín').length).toBeGreaterThan(1)
    })

    it('muestra los botones de auth dentro del menú mobile cuando está deslogueado', async () => {
      const user = userEvent.setup()
      renderWithRouter()

      await user.click(screen.getByRole('button', { name: 'Abrir menú' }))

      expect(screen.getAllByRole('link', { name: 'Iniciar sesión' }).length).toBe(2)
      expect(screen.getAllByRole('link', { name: 'Registrarse' }).length).toBe(2)
    })
  })
})