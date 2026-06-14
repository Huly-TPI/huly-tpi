import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import UsersPage from '../../pages/Backoffice/UsersPage'
import * as adminApi from '../../api/admin'

vi.mock('../../api/admin', () => ({
  getUsers: vi.fn(),
  getAntiScrollDashboard: vi.fn(),
}))

const mockedGetUsers = vi.mocked(adminApi.getUsers)

describe('UsersPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('muestra el estado cargando inicialmente', () => {
    mockedGetUsers.mockReturnValue(new Promise(() => {}))
    render(
      <MemoryRouter>
        <UsersPage />
      </MemoryRouter>
    )
    expect(screen.getByText('Cargando usuarios...')).toBeInTheDocument()
  })

  it('renderiza la lista de usuarios correctamente', async () => {
    mockedGetUsers.mockResolvedValueOnce([
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
        <UsersPage />
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
    mockedGetUsers.mockResolvedValueOnce([
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
        <UsersPage />
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
    mockedGetUsers.mockResolvedValueOnce([
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
        dailyScrollTimeSeconds: {
          current_0: 1000,
          current_1: 1000,
          current_2: 1000,
          current_3: 1000,
          current_4: 1000,
          current_5: 0,
          current_6: 0,
          previous_0: 1000,
          previous_1: 1000,
          previous_2: 1000,
          previous_3: 1000,
          previous_4: 1000,
          previous_5: 0,
          previous_6: 0,
        },
      },
    ])

    render(
      <MemoryRouter initialEntries={['/backoffice/usuarios']}>
        <Routes>
          <Route path="/backoffice/usuarios" element={<UsersPage />} />
          <Route path="/backoffice/usuarios/:id" element={<UsersPage />} />
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

  it('no muestra las estadísticas si el usuario no tiene habilitado antiscroll', async () => {
    mockedGetUsers.mockResolvedValueOnce([
      {
        id: 2,
        name: 'John Doe',
        email: 'john@example.com',
        role: 'USER',
        status: 'ACTIVE',
        birthDate: '2000-01-01',
        antiScrollEnabled: false,
        dataSharingConsent: true,
        mostUsedApp: 'instagram.com',
        mostUsedAppActiveSeconds: 3600,
        totalScrollTimeSeconds: 5000,
      },
    ])

    render(
      <MemoryRouter initialEntries={['/backoffice/usuarios/2']}>
        <Routes>
          <Route path="/backoffice/usuarios/:id" element={<UsersPage />} />
        </Routes>
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.queryByText('Cargando usuarios...')).not.toBeInTheDocument()
    })

    expect(screen.getByText('Detalle de usuario')).toBeInTheDocument()
    expect(screen.queryByText('Tiempo scrolleando por día')).not.toBeInTheDocument()
    expect(screen.queryByText('Tiempo en cada dominio')).not.toBeInTheDocument()
  })
})
