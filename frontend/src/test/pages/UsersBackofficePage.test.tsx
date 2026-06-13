import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import UsersBackofficePage from '../../pages/Backoffice/UsersBackofficePage'
import * as adminApi from '../../api/admin'

vi.mock('../../api/admin', () => ({
  getBackofficeUsers: vi.fn(),
  getAntiScrollDashboard: vi.fn(),
}))

const mockedGetBackofficeUsers = vi.mocked(adminApi.getBackofficeUsers)

describe('UsersBackofficePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('muestra el estado cargando inicialmente', () => {
    mockedGetBackofficeUsers.mockReturnValue(new Promise(() => {}))
    render(
      <MemoryRouter>
        <UsersBackofficePage />
      </MemoryRouter>
    )
    expect(screen.getByText('Cargando usuarios...')).toBeInTheDocument()
  })

  it('renderiza la lista de usuarios correctamente', async () => {
    mockedGetBackofficeUsers.mockResolvedValueOnce([
      {
        id: 2,
        name: 'John Doe',
        email: 'john@example.com',
        role: 'USER',
        status: 'ACTIVE',
        birthDate: '2000-01-01',
        antiScrollEnabled: true,
        dataSharingConsent: true,
        mostUsedApp: 'instagram.com',
        mostUsedAppActiveSeconds: 3600,
        totalScrollTimeSeconds: 5000,
      },
      {
        id: 3,
        name: 'Jane Doe',
        email: 'jane@example.com',
        role: 'USER',
        status: 'ACTIVE',
        birthDate: null,
        antiScrollEnabled: false,
        dataSharingConsent: false,
        mostUsedApp: null,
        mostUsedAppActiveSeconds: 0,
        totalScrollTimeSeconds: 0,
      },
    ])

    render(
      <MemoryRouter>
        <UsersBackofficePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.queryByText('Cargando usuarios...')).not.toBeInTheDocument()
    })

    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.getByText('john@example.com')).toBeInTheDocument()
    expect(screen.getByText('Jane Doe')).toBeInTheDocument()
    expect(screen.getByText('jane@example.com')).toBeInTheDocument()
  })

  it('filtra usuarios por busqueda', async () => {
    const user = userEvent.setup()
    mockedGetBackofficeUsers.mockResolvedValueOnce([
      {
        id: 2,
        name: 'John Doe',
        email: 'john@example.com',
        role: 'USER',
        status: 'ACTIVE',
        birthDate: '2000-01-01',
        antiScrollEnabled: true,
        dataSharingConsent: true,
        mostUsedApp: 'instagram.com',
        mostUsedAppActiveSeconds: 3600,
        totalScrollTimeSeconds: 5000,
      },
      {
        id: 3,
        name: 'Jane Smith',
        email: 'jane@example.com',
        role: 'USER',
        status: 'ACTIVE',
        birthDate: null,
        antiScrollEnabled: false,
        dataSharingConsent: false,
        mostUsedApp: null,
        mostUsedAppActiveSeconds: 0,
        totalScrollTimeSeconds: 0,
      },
    ])

    render(
      <MemoryRouter>
        <UsersBackofficePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.queryByText('Cargando usuarios...')).not.toBeInTheDocument()
    })

    const searchInput = screen.getByPlaceholderText('Buscar por nombre o email...')
    await user.type(searchInput, 'Smith')

    expect(screen.queryByText('John Doe')).not.toBeInTheDocument()
    expect(screen.getByText('Jane Smith')).toBeInTheDocument()
  })

  it('navega a detalle al hacer click en el ojo y regresa', async () => {
    const user = userEvent.setup()
    mockedGetBackofficeUsers.mockResolvedValueOnce([
      {
        id: 2,
        name: 'John Doe',
        email: 'john@example.com',
        role: 'USER',
        status: 'ACTIVE',
        birthDate: '2000-01-01',
        antiScrollEnabled: true,
        dataSharingConsent: true,
        mostUsedApp: 'instagram.com',
        mostUsedAppActiveSeconds: 3600,
        totalScrollTimeSeconds: 5000,
      },
    ])

    render(
      <MemoryRouter initialEntries={['/backoffice/usuarios']}>
        <Routes>
          <Route path="/backoffice/usuarios" element={<UsersBackofficePage />} />
          <Route path="/backoffice/usuarios/:id" element={<UsersBackofficePage />} />
        </Routes>
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.queryByText('Cargando usuarios...')).not.toBeInTheDocument()
    })

    const detailBtn = screen.getByLabelText('Ver detalles de John Doe')
    await user.click(detailBtn)

    expect(screen.getByText('Detalle de usuario')).toBeInTheDocument()
    expect(screen.getByText('john@example.com')).toBeInTheDocument()
    expect(screen.getAllByText('instagram.com')[0]).toBeInTheDocument()

    const backBtn = screen.getByLabelText('volver')
    await user.click(backBtn)

    expect(screen.queryByText('Detalle de usuario')).not.toBeInTheDocument()
    expect(screen.getByText('John Doe')).toBeInTheDocument()
  })
})
