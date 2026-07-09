import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import UsersPage from '../../pages/Backoffice/UsersPage'
import UserDetailPage from '../../pages/Backoffice/UserDetailPage'
import * as adminApi from '../../api/admin'
import { verifyTextPresent, verifyTextNotPresent, clearAllMocks } from '../testHelpers'

vi.mock('../../api/admin', () => ({
  getUsers: vi.fn(),
  getAdminDashboard: vi.fn(),
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

const mockUsersList = [
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
    coins: 100,
    plan: 'PREMIUM_PLAN',
    dominantEmotion: 'JOY',
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
    coins: 0,
    plan: 'Gratuito',
    dominantEmotion: 'NEUTRAL',
  },
]

describe('UsersPage', () => {
  let user: ReturnType<typeof userEvent.setup>

  beforeEach(() => {
    clearAllMocks()
    mockedGetUsers.mockImplementation(async (search?: string) => {
      if (!search) return mockUsersList
      return mockUsersList.filter(
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
    mockedGetUserAntiScroll.mockResolvedValue({
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
    })
  })

  it('muestra el estado cargando inicialmente', () => {
    setupGetUsersPending()
    renderUsersPage()
    verifyLoadingUsersMessageVisible()
  })

  it('renderiza la lista de usuarios correctamente', () => {
    setupGetUsersResolved(mockUsersList)
    renderUsersPage()
    return waitForLoadingUsersToDisappear().then(() => {
      verifyUsersListVisible()
    })
  })

  it('filtra usuarios por busqueda', () => {
    renderUsersPage()
    return waitForLoadingUsersToDisappear().then(() => {
      return typeSearchTermAndSubmit('Smith').then(() => {
        verifyOnlySmithUserVisible()
      })
    })
  })

  it('navega a detalle al hacer click en el ojo y regresa', () => {
    setupSingleUserDetailMockResponse()
    renderUsersWithRoutes()
    return waitForLoadingUsersToDisappear().then(() => {
      return clickDetailButtonFor('John Doe').then(() => {
        return waitForUserDetailHeaderVisible().then(() => {
          verifyEmailVisibleInDetail('john@example.com')
          return clickAntiscrollTab().then(() => {
            return waitForDomainVisible('instagram.com').then(() => {
              return clickBackLink().then(() => {
                verifyBackToUsersList('John Doe')
              })
            })
          })
        })
      })
    })
  })

  it('no muestra las estadísticas si el usuario no tiene habilitado antiscroll', () => {
    setupGetUsersResolved([
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
    renderUserDetailPageOnly(2)
    return waitForUserDetailHeaderVisible().then(() => {
      verifyAntiScrollStatsNotVisible()
    })
  })

  /* helpers */

  const renderUsersPage = () => {
    user = userEvent.setup()
    render(
      <MemoryRouter>
        <UsersPage />
      </MemoryRouter>
    )
  }

  const renderUsersWithRoutes = () => {
    user = userEvent.setup()
    render(
      <MemoryRouter initialEntries={['/backoffice/usuarios']}>
        <Routes>
          <Route path="/backoffice/usuarios" element={<UsersPage />} />
          <Route path="/backoffice/usuarios/:id" element={<UserDetailPage />} />
        </Routes>
      </MemoryRouter>
    )
  }

  const renderUserDetailPageOnly = (userId: number) => {
    user = userEvent.setup()
    render(
      <MemoryRouter initialEntries={[`/backoffice/usuarios/${userId}`]}>
        <Routes>
          <Route path="/backoffice/usuarios/:id" element={<UserDetailPage />} />
        </Routes>
      </MemoryRouter>
    )
  }

  const setupGetUsersPending = () => {
    mockedGetUsers.mockReturnValue(new Promise(() => {}))
  }

  const setupGetUsersResolved = (list: any[]) => {
    mockedGetUsers.mockResolvedValue(list)
  }

  const setupSingleUserDetailMockResponse = () => {
    mockedGetUsers.mockResolvedValue([
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
  }

  const verifyLoadingUsersMessageVisible = () => {
    verifyTextPresent('Cargando usuarios...')
  }

  const waitForLoadingUsersToDisappear = () => {
    return waitFor(() => {
      verifyTextNotPresent('Cargando usuarios...')
    })
  }

  const verifyUsersListVisible = () => {
    expect(screen.getAllByText('John Doe')[0]).toBeInTheDocument()
    expect(screen.getAllByText('john@example.com')[0]).toBeInTheDocument()
    expect(screen.getAllByText('Jane Smith')[0]).toBeInTheDocument()
    expect(screen.getAllByText('jane@example.com')[0]).toBeInTheDocument()
  }

  const typeSearchTermAndSubmit = (term: string) => {
    const searchInput = screen.getByPlaceholderText('Buscar por nombre o email...')
    return user.type(searchInput, `${term}{enter}`)
  }

  const verifyOnlySmithUserVisible = () => {
    expect(screen.queryAllByText('John Doe')).toHaveLength(0)
    expect(screen.getAllByText('Jane Smith')[0]).toBeInTheDocument()
  }

  const clickDetailButtonFor = (name: string) => {
    const detailBtn = screen.getAllByLabelText(`Ver detalles de ${name}`)[0]
    return user.click(detailBtn)
  }

  const waitForUserDetailHeaderVisible = () => {
    return screen.findByText('Detalle de usuario').then(el => {
      expect(el).toBeInTheDocument()
    })
  }

  const verifyEmailVisibleInDetail = (email: string) => {
    expect(screen.getAllByText(email)[0]).toBeInTheDocument()
  }

  const clickAntiscrollTab = () => {
    const antiscrollTab = screen.getByRole('button', { name: 'Antiscroll' })
    return user.click(antiscrollTab)
  }

  const waitForDomainVisible = (domain: string) => {
    return screen.findByText(domain).then(el => {
      expect(el).toBeInTheDocument()
    })
  }

  const clickBackLink = () => {
    const backBtn = screen.getByLabelText('volver')
    return user.click(backBtn)
  }

  const verifyBackToUsersList = (name: string) => {
    verifyTextNotPresent('Detalle de usuario')
    expect(screen.getAllByText(name)[0]).toBeInTheDocument()
  }

  const verifyAntiScrollStatsNotVisible = () => {
    verifyTextNotPresent('Tiempo scrolleando por día')
    verifyTextNotPresent('Tiempo en cada dominio')
  }
})
