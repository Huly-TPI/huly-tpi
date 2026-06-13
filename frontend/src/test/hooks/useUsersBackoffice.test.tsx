import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useUsersBackoffice } from '../../hooks/backoffice/useUsersBackoffice'
import * as adminApi from '../../api/admin'
import React from 'react'

vi.mock('../../api/admin', () => ({
  getBackofficeUsers: vi.fn(),
}))

const mockedGetBackofficeUsers = vi.mocked(adminApi.getBackofficeUsers)

const mockUsers = [
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
]

describe('useUsersBackoffice', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('carga los usuarios al montar', async () => {
    mockedGetBackofficeUsers.mockResolvedValueOnce(mockUsers)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter>
        {children}
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsersBackoffice(), { wrapper })

    expect(result.current.loading).toBe(true)
    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.users).toEqual(mockUsers)
    expect(result.current.filteredUsers).toEqual(mockUsers)
  })

  it('filtra los usuarios por búsqueda', async () => {
    mockedGetBackofficeUsers.mockResolvedValueOnce(mockUsers)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter>
        {children}
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsersBackoffice(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    act(() => {
      result.current.setSearch('smith')
    })

    expect(result.current.filteredUsers).toHaveLength(1)
    expect(result.current.filteredUsers[0].name).toBe('Jane Smith')
  })

  it('detecta el usuario seleccionado mediante params y calcula estadísticas', async () => {
    mockedGetBackofficeUsers.mockResolvedValueOnce(mockUsers)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter initialEntries={['/backoffice/usuarios/2']}>
        <Routes>
          <Route path="/backoffice/usuarios/:id" element={children} />
        </Routes>
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsersBackoffice(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.selectedUser).toBeDefined()
    expect(result.current.selectedUser?.name).toBe('John Doe')
    expect(result.current.hasUsageData).toBe(true)

    // Verificar cálculos de distribución diaria (ej: lunes con factor 0.15)
    // 5000 * 1.0 * 0.15 = 750
    expect(result.current.getDailyTime('0')).toBe(750)

    // Cambiar semana para aplicar factor 0.85
    act(() => {
      result.current.setSelectedWeek('previous')
    })
    // 5000 * 0.85 * 0.15 = 638
    expect(result.current.getDailyTime('0')).toBe(638)

    // Cambiar día seleccionado
    act(() => {
      result.current.setSelectedDay('0')
    })
    expect(result.current.filteredTotalTime).toBe(638)
    expect(result.current.domainList[0].domain).toBe('instagram.com')
  })

  it('maneja errores en la API de carga de usuarios', async () => {
    mockedGetBackofficeUsers.mockRejectedValueOnce(new Error('API Error'))

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter>
        {children}
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsersBackoffice(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.error).toBe('no se pudieron cargar los usuarios')
  })
})
