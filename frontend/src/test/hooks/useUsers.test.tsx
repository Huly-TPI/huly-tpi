import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useUsers } from '../../hooks/backoffice/useUsers'
import * as adminApi from '../../api/admin'
import React from 'react'

vi.mock('../../api/admin', () => ({
  getUsers: vi.fn(),
  getUserActivities: vi.fn(),
  getUserAiDiagnostics: vi.fn(),
  getUserFinancials: vi.fn(),
  getUserAntiScroll: vi.fn(),
}))

const mockedGetUsers = vi.mocked(adminApi.getUsers)
const mockedGetUserActivities = vi.mocked(adminApi.getUserActivities)
const mockedGetUserAiDiagnostics = vi.mocked(adminApi.getUserAiDiagnostics)
const mockedGetUserFinancials = vi.mocked(adminApi.getUserFinancials)
const mockedGetUserAntiScroll = vi.mocked(adminApi.getUserAntiScroll)

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
  },
]

const mockAntiScrollStats = {
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
}

describe('useUsers', () => {
  beforeEach(() => {
    clearAllMocks()
    mockedGetUsers.mockImplementation(async (search?: string) => {
      if (!search) return mockUsers
      return mockUsers.filter(
        (u) =>
          (u.name && u.name.toLowerCase().includes(search.toLowerCase())) ||
          (u.email && u.email.toLowerCase().includes(search.toLowerCase()))
      )
    })
    mockedGetUserActivities.mockResolvedValue({
      activitySessions: [],
      todayActivitiesCount: 0,
      favoriteActivity: null,
      averageSessionsText: 'Sin registros',
      activityDistribution: {},
      activityNames: {}
    })
    mockedGetUserAiDiagnostics.mockResolvedValue({
      aiMemories: [],
      emotionalEvents: [],
      preferredName: null,
      communicationStyle: null
    })
    mockedGetUserFinancials.mockResolvedValue({
      paymentEvents: [],
      totalEarnings: 0
    })
    mockedGetUserAntiScroll.mockResolvedValue(mockAntiScrollStats)
  })

  it('carga los usuarios al montar', () => {
    setupGetUsersResolved(mockUsers)
    setupHook()
    verifyLoading(true)
    return waitForLoadingFinished().then(() => {
      verifyUsers(mockUsers)
      verifyFilteredUsers(mockUsers)
    })
  })

  it('filtra los usuarios por búsqueda', () => {
    setupGetUsersResolved(mockUsers)
    setupHook()
    return waitForLoadingFinished()
      .then(() => {
        callSetSearch('smith')
        return waitForFilteredUsersLength(1)
      })
      .then(() => {
        verifyFilteredUserAtIndexName(0, 'Jane Smith')
      })
  })

  it('detecta el usuario seleccionado mediante params y carga estadísticas antiscroll reales', () => {
    setupGetUsersResolved(mockUsers)
    setupHookWithParams(['/backoffice/users/2'])
    return waitForLoadingFinished()
      .then(() => {
        verifySelectedUserName('John Doe')
        callSetActiveTab('antiscroll')
        return waitForAntiscrollLoadingFinished()
      })
      .then(() => {
        verifyHasUsageData(true)
        verifyDailyTime('1', 1000)
        verifyGetUserAntiScrollCalledWith(2, 'current', 'all')
      })
  })

  it('usa stats filtradas desde backend y arma la lista de dominios sin reescalar localmente', () => {
    setupGetUsersResolved(mockUsers)
    setupHookWithParams(['/backoffice/users/2'])
    return waitForLoadingFinished()
      .then(() => {
        callSetActiveTab('antiscroll')
        return waitForAntiscrollLoadingFinished()
      })
      .then(() => {
        verifyFilteredTotalTime(5000)
        verifyDomainList([
          { domain: 'instagram.com', seconds: 3600 },
          { domain: 'twitter.com', seconds: 1000 },
          { domain: 'Otros sitios', seconds: 400 },
        ])
        setupGetUserAntiScrollResolved({
          ...mockAntiScrollStats,
          totalScrollTimeSeconds: 1000,
          topApps: [
            { domain: 'reddit.com', totalActiveSeconds: 700 },
            { domain: 'facebook.com', totalActiveSeconds: 300 },
          ],
          mostUsedApp: 'reddit.com',
          mostUsedAppActiveSeconds: 700,
        })
        callSetSelectedDay('1')
        return waitForFilteredTotalTime(1000)
      })
      .then(() => {
        verifyDomainList([
          { domain: 'reddit.com', seconds: 700 },
          { domain: 'facebook.com', seconds: 300 },
        ])
      })
  })

  it('maneja errores en la API de carga de usuarios', () => {
    setupGetUsersRejected('API Error')
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyError('no se pudieron cargar los usuarios')
    })
  })

  it('realiza lazy loading de estadísticas según la pestaña activa', () => {
    setupGetUsersResolved(mockUsers)
    setupHookWithParams(['/backoffice/users/2'])
    return waitForLoadingFinished()
      .then(() => {
        verifyGetUserActivitiesCalledWith(2, 'total')
        verifyGetUserAiDiagnosticsNotCalled()
        verifyGetUserFinancialsNotCalled()
        verifyGetUserAntiScrollNotCalled()
        clearMocks()
        callSetActiveTab('ai')
        return waitForAiLoadingFinished()
      })
      .then(() => {
        verifyGetUserAiDiagnosticsCalledWith(2)
        clearMocks()
        callSetActiveTab('finance')
        return waitForFinancialsLoadingFinished()
      })
      .then(() => {
        verifyGetUserFinancialsCalledWith(2)
        clearMocks()
        callSetActiveTab('antiscroll')
        return waitForAntiscrollLoadingFinished()
      })
      .then(() => {
        verifyGetUserAntiScrollCalledWith(2, 'current', 'all')
        clearMocks()
        callSetActivityTimeframe('week')
        verifyGetUserActivitiesNotCalled()
        callSetActiveTab('usage')
        return waitForActivitiesLoadingFinished()
      })
      .then(() => {
        verifyGetUserActivitiesCalledWith(2, 'week')
      })
  })

  it('vuelve a consultar antiscroll al backend cuando cambia semana o día', () => {
    setupGetUsersResolved(mockUsers)
    setupHookWithParams(['/backoffice/users/2'])
    return waitForLoadingFinished()
      .then(() => {
        callSetActiveTab('antiscroll')
        return waitForAntiscrollLoadingFinished()
      })
      .then(() => {
        verifyGetUserAntiScrollLastCalledWith(2, 'current', 'all')
        callSetSelectedWeek('previous')
        return waitForGetUserAntiScrollLastCalledWith(2, 'previous', 'all')
      })
      .then(() => {
        callSetSelectedDay('3')
        return waitForGetUserAntiScrollLastCalledWith(2, 'previous', '3')
      })
  })
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useUsers>, { wrapper: any }>>

  /* helpers */

  const setupHook = () => {
    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter>{children}</MemoryRouter>
    )
    rendered = renderHook(() => useUsers(), { wrapper })
  }

  const setupHookWithParams = (initialEntries: string[]) => {
    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter initialEntries={initialEntries}>
        <Routes>
          <Route path="/backoffice/users/:id" element={children} />
          <Route path="*" element={children} />
        </Routes>
      </MemoryRouter>
    )
    rendered = renderHook(() => useUsers(), { wrapper })
  }

  const setupGetUsersResolved = (val: any) => {
    mockedGetUsers.mockResolvedValueOnce(val)
  }

  const setupGetUsersRejected = (msg: string) => {
    mockedGetUsers.mockRejectedValueOnce(new Error(msg))
  }

  const setupGetUserAntiScrollResolved = (val: any) => {
    mockedGetUserAntiScroll.mockResolvedValueOnce(val)
  }

  const clearMocks = () => {
    clearAllMocks()
  }

  const waitForLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.loading).toBe(false))
  }

  const waitForFilteredTotalTime = (val: number) => {
    return waitFor(() => expect(rendered.result.current.filteredTotalTime).toBe(val))
  }

  const waitForFilteredUsersLength = (len: number) => {
    return waitFor(() => expect(rendered.result.current.filteredUsers).toHaveLength(len))
  }

  const waitForAntiscrollLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.antiscrollLoading).toBe(false))
  }

  const waitForAiLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.aiLoading).toBe(false))
  }

  const waitForFinancialsLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.financialsLoading).toBe(false))
  }

  const waitForActivitiesLoadingFinished = () => {
    return waitFor(() => expect(rendered.result.current.activitiesLoading).toBe(false))
  }

  const waitForGetUserAntiScrollLastCalledWith = (id: number, week: string, day: string) => {
    return waitFor(() => expect(mockedGetUserAntiScroll).toHaveBeenLastCalledWith(id, week, day))
  }

  const callSetSearch = (val: string) => {
    act(() => {
      rendered.result.current.setSearch(val)
    })
  }

  const callSetActiveTab = (tab: 'antiscroll' | 'usage' | 'ai' | 'finance') => {
    act(() => {
      rendered.result.current.setActiveTab(tab)
    })
  }

  const callSetSelectedDay = (day: string) => {
    act(() => {
      rendered.result.current.setSelectedDay(day)
    })
  }

  const callSetSelectedWeek = (week: string) => {
    act(() => {
      rendered.result.current.setSelectedWeek(week)
    })
  }

  const callSetActivityTimeframe = (timeframe: any) => {
    act(() => {
      rendered.result.current.setActivityTimeframe(timeframe)
    })
  }

  const verifyLoading = (expected: boolean) => {
    expect(rendered.result.current.loading).toBe(expected)
  }

  const verifyUsers = (expected: any[]) => {
    expect(rendered.result.current.users).toEqual(expected)
  }

  const verifyFilteredUsers = (expected: any[]) => {
    expect(rendered.result.current.filteredUsers).toEqual(expected)
  }

  const verifyFilteredUserAtIndexName = (idx: number, name: string) => {
    expect(rendered.result.current.filteredUsers[idx].name).toBe(name)
  }

  const verifySelectedUserName = (name: string) => {
    expect(rendered.result.current.selectedUser?.name).toBe(name)
  }

  const verifyHasUsageData = (expected: boolean) => {
    expect(rendered.result.current.hasUsageData).toBe(expected)
  }

  const verifyDailyTime = (day: string, expectedSecs: number) => {
    expect(rendered.result.current.getDailyTime(day)).toBe(expectedSecs)
  }

  const verifyGetUserAntiScrollCalledWith = (id: number, week: string, day: string) => {
    expect(mockedGetUserAntiScroll).toHaveBeenCalledWith(id, week, day)
  }

  const verifyGetUserAntiScrollLastCalledWith = (id: number, week: string, day: string) => {
    expect(mockedGetUserAntiScroll).toHaveBeenLastCalledWith(id, week, day)
  }

  const verifyFilteredTotalTime = (val: number) => {
    expect(rendered.result.current.filteredTotalTime).toBe(val)
  }

  const verifyDomainList = (expected: any[]) => {
    expect(rendered.result.current.domainList).toEqual(expected)
  }

  const verifyError = (expectedMsg: string) => {
    expect(rendered.result.current.error).toBe(expectedMsg)
  }

  const verifyGetUserActivitiesCalledWith = (id: number, timeframe: string) => {
    expect(mockedGetUserActivities).toHaveBeenCalledWith(id, timeframe)
  }

  const verifyGetUserAiDiagnosticsNotCalled = () => {
    expect(mockedGetUserAiDiagnostics).not.toHaveBeenCalled()
  }

  const verifyGetUserFinancialsNotCalled = () => {
    expect(mockedGetUserFinancials).not.toHaveBeenCalled()
  }

  const verifyGetUserAntiScrollNotCalled = () => {
    expect(mockedGetUserAntiScroll).not.toHaveBeenCalled()
  }

  const verifyGetUserAiDiagnosticsCalledWith = (id: number) => {
    expect(mockedGetUserAiDiagnostics).toHaveBeenCalledWith(id)
  }

  const verifyGetUserFinancialsCalledWith = (id: number) => {
    expect(mockedGetUserFinancials).toHaveBeenCalledWith(id)
  }

  const verifyGetUserActivitiesNotCalled = () => {
    expect(mockedGetUserActivities).not.toHaveBeenCalled()
  }
})
