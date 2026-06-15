import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useUsers } from '../../hooks/backoffice/useUsers'
import * as adminApi from '../../api/admin'
import React from 'react'

vi.mock('../../api/admin', () => ({
  getUsers: vi.fn(),
}))

const mockedGetUsers = vi.mocked(adminApi.getUsers)

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
    dailyScrollTimeSeconds: {
      current_0: 0,
      current_1: 1000,
      current_2: 1000,
      current_3: 1000,
      current_4: 1000,
      current_5: 500,
      current_6: 500,
      previous_0: 0,
      previous_1: 1000,
      previous_2: 1000,
      previous_3: 1000,
      previous_4: 1000,
      previous_5: 500,
      previous_6: 500,
    },
    topApps: [
      { domain: 'instagram.com', totalActiveSeconds: 3600 },
      { domain: 'twitter.com', totalActiveSeconds: 1000 },
    ],
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

describe('useUsers', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('carga los usuarios al montar', async () => {
    mockedGetUsers.mockResolvedValueOnce(mockUsers)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter>
        {children}
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsers(), { wrapper })

    expect(result.current.loading).toBe(true)
    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.users).toEqual(mockUsers)
    expect(result.current.filteredUsers).toEqual(mockUsers)
  })

  it('filtra los usuarios por búsqueda', async () => {
    mockedGetUsers.mockResolvedValueOnce(mockUsers)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter>
        {children}
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsers(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    act(() => {
      result.current.setSearch('smith')
    })

    expect(result.current.filteredUsers).toHaveLength(1)
    expect(result.current.filteredUsers[0].name).toBe('Jane Smith')
  })

  it('detecta el usuario seleccionado mediante params y calcula estadísticas', async () => {
    mockedGetUsers.mockResolvedValueOnce(mockUsers)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter initialEntries={['/backoffice/usuarios/2']}>
        <Routes>
          <Route path="/backoffice/usuarios/:id" element={children} />
        </Routes>
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsers(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.selectedUser).toBeDefined()
    expect(result.current.selectedUser?.name).toBe('John Doe')
    expect(result.current.hasUsageData).toBe(true)

    expect(result.current.getDailyTime('0')).toBe(0)

    act(() => {
      result.current.setSelectedWeek('previous')
    })
    expect(result.current.getDailyTime('0')).toBe(0)

    act(() => {
      result.current.setSelectedDay('0')
    })
    expect(result.current.filteredTotalTime).toBe(0)
  })

  it('calcula los factores y la lista de dominios correctamente según el filtro de semana y día', async () => {
    mockedGetUsers.mockResolvedValueOnce(mockUsers)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter initialEntries={['/backoffice/usuarios/2']}>
        <Routes>
          <Route path="/backoffice/usuarios/:id" element={children} />
        </Routes>
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsers(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.filteredTotalTime).toBe(5000)
    expect(result.current.domainList).toHaveLength(3)
    expect(result.current.domainList[0]).toEqual({ domain: 'instagram.com', seconds: 3600 })
    expect(result.current.domainList[1]).toEqual({ domain: 'twitter.com', seconds: 1000 })
    expect(result.current.domainList[2]).toEqual({ domain: 'Otros sitios', seconds: 400 })

    act(() => {
      result.current.setSelectedDay('1')
    })

    expect(result.current.filteredTotalTime).toBe(1000)
    expect(result.current.domainList).toHaveLength(3)
    expect(result.current.domainList[0]).toEqual({ domain: 'instagram.com', seconds: 720 })
    expect(result.current.domainList[1]).toEqual({ domain: 'twitter.com', seconds: 200 })
    expect(result.current.domainList[2]).toEqual({ domain: 'Otros sitios', seconds: 80 })
  })

  it('maneja errores en la API de carga de usuarios', async () => {
    mockedGetUsers.mockRejectedValueOnce(new Error('API Error'))

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter>
        {children}
      </MemoryRouter>
    )

    const { result } = renderHook(() => useUsers(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.error).toBe('no se pudieron cargar los usuarios')
  })
})
