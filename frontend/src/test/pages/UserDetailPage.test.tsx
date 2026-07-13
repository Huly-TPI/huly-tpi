import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import UserDetailPage from '../../pages/Backoffice/UserDetailPage'
import { useUsers } from '../../hooks/backoffice/useUsers'
import { verifyTextPresent, clearAllMocks } from '../testHelpers'

vi.mock('../../hooks/backoffice/useUsers', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../hooks/backoffice/useUsers')>()
  return {
    ...actual,
    useUsers: vi.fn(),
  }
})

const mockedUseUsers = vi.mocked(useUsers)

const mockUser = {
  id: 2,
  name: 'John Doe',
  email: 'john@example.com',
  role: 'USER',
  status: 'ACTIVE',
  antiScrollEnabled: true,
  dataSharingConsent: true,
}

describe('UserDetailPage', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('muestra spinner de carga cuando no hay usuario seleccionado', () => {
    setupHookWithSelectedUser(null)
    renderUserDetailPage()
    verifyLoadingSpinnerVisible()
  })

  it('renderiza la información del perfil del usuario en el sidebar', () => {
    setupHookWithSelectedUserAndTab(mockUser, 'usage')
    renderUserDetailPage()
    verifyUserProfileInfoInSidebar('John Doe', 'john@example.com', 'Activo')
  })

  it('muestra la pestaña de Uso y Actividades con estadísticas', () => {
    setupHookWithUsageTab(mockUser)
    renderUserDetailPage()
    verifyUsageAndActivitiesStats()
  })

  it('muestra la pestaña de IA y Decisiones con diagnósticos', () => {
    setupHookWithAITab(mockUser)
    renderUserDetailPage()
    verifyAIDiagnosticsVisible()
  })

  it('muestra la pestaña de Métricas Financieras con ingresos', () => {
    setupHookWithFinanceTab(mockUser)
    renderUserDetailPage()
    verifyFinancialMetricsVisible()
  })

  it('muestra la pestaña de Antiscroll con estadísticas de navegación', () => {
    setupHookWithAntiScrollTab(mockUser)
    renderUserDetailPage()
    verifyAntiScrollStatsVisible()
  })

  it('muestra el selector de días e indicador de sin estadísticas cuando no hay datos en Antiscroll', () => {
    setupHookWithAntiScrollTabNoData(mockUser)
    renderUserDetailPage()
    verifyAntiScrollNoStatsButDaysVisible()
  })

  /* helpers */

  const renderUserDetailPage = () => {
    render(<UserDetailPage />)
  }

  const setupHookWithSelectedUser = (user: any) => {
    mockedUseUsers.mockReturnValue({
      selectedUser: user,
    } as any)
  }

  const setupHookWithSelectedUserAndTab = (user: any, tab: string) => {
    mockedUseUsers.mockReturnValue({
      selectedUser: user,
      activeTab: tab,
      setActiveTab: vi.fn(),
      activitiesLoading: false,
      activities: null,
    } as any)
  }

  const setupHookWithUsageTab = (user: any) => {
    mockedUseUsers.mockReturnValue({
      selectedUser: user,
      activeTab: 'usage',
      setActiveTab: vi.fn(),
      activitiesLoading: false,
      activitiesError: null,
      activityTimeframe: 'total',
      setActivityTimeframe: vi.fn(),
      activities: {
        todayActivitiesCount: 5,
        favoriteActivity: 'BUBBLE',
        averageSessionsText: '12 sesiones/semana',
        activityDistribution: {
          BUBBLE: 8,
          BREATHING: 4,
        },
        activityNames: {
          BUBBLE: 'Burbujas relajantes',
          BREATHING: 'Respiración guiada',
        },
        activitySessions: [
          { id: 1, activityType: 'BUBBLE', activityName: 'Burbujas relajantes', createdAt: '2026-06-15T00:00:00Z' },
        ],
      },
    } as any)
  }

  const setupHookWithAITab = (user: any) => {
    mockedUseUsers.mockReturnValue({
      selectedUser: user,
      activeTab: 'ai',
      setActiveTab: vi.fn(),
      aiLoading: false,
      aiError: null,
      aiDiagnostics: {
        personalitySummary: 'Usuario centrado y reflexivo',
        topicsDetected: ['Estrés laboral o académico'],
        copingStrategies: ['Meditación y Respiración'],
        receptivityScore: 80,
        receptivityLabel: 'Alta receptividad',
        acceptedActivities: ['Respiración Guiada'],
        ignoredActivities: ['Retos Diarios'],
        dominantEmotion: 'FELIZ',
        emotionDistribution: {
          FELIZ: 10,
          TRISTE: 2,
        },
      },
    } as any)
  }

  const setupHookWithFinanceTab = (user: any) => {
    mockedUseUsers.mockReturnValue({
      selectedUser: user,
      activeTab: 'finance',
      setActiveTab: vi.fn(),
      financialsLoading: false,
      financialsError: null,
      financials: {
        totalEarnings: 49.99,
        paymentEvents: [
          {
            id: 1,
            productId: 10,
            productName: 'Premium Plan',
            productPrice: 49.99,
            status: 'APPROVED',
            createdAt: '2026-06-15T00:00:00Z',
          },
        ],
      },
    } as any)
  }

  const setupHookWithAntiScrollTab = (user: any) => {
    mockedUseUsers.mockReturnValue({
      selectedUser: user,
      activeTab: 'antiscroll',
      setActiveTab: vi.fn(),
      antiscrollLoading: false,
      antiscrollError: null,
      antiscrollStats: {
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
        },
        topApps: [
          { domain: 'instagram.com', totalActiveSeconds: 3600 },
          { domain: 'twitter.com', totalActiveSeconds: 1000 },
        ],
      },
      DAYS: [
        { key: '0', label: 'Lunes' },
        { key: '1', label: 'Martes' },
      ],
      selectedWeek: 'current',
      setSelectedWeek: vi.fn(),
      selectedDay: 'all',
      setSelectedDay: vi.fn(),
      getDailyTime: (dayKey: string) => (dayKey === '0' ? 0 : 1000),
      domainList: [
        { domain: 'instagram.com', seconds: 3600 },
        { domain: 'twitter.com', seconds: 1000 },
        { domain: 'Otros sitios', seconds: 400 },
      ],
      maxDomainTime: 3600,
      filteredTotalTime: 5000,
      hasUsageData: true,
    } as any)
  }

  const setupHookWithAntiScrollTabNoData = (user: any) => {
    mockedUseUsers.mockReturnValue({
      selectedUser: user,
      activeTab: 'antiscroll',
      setActiveTab: vi.fn(),
      antiscrollLoading: false,
      antiscrollError: null,
      antiscrollStats: {
        antiScrollEnabled: true,
        dataSharingConsent: true,
        mostUsedApp: null,
        mostUsedAppActiveSeconds: 0,
        totalScrollTimeSeconds: 0,
        dailyScrollTimeSeconds: {
          current_0: 0,
          current_1: 0,
        },
        topApps: [],
      },
      DAYS: [
        { key: '0', label: 'Lunes' },
        { key: '1', label: 'Martes' },
      ],
      selectedWeek: 'current',
      setSelectedWeek: vi.fn(),
      selectedDay: 'all',
      setSelectedDay: vi.fn(),
      getDailyTime: () => 0,
      domainList: [],
      maxDomainTime: 1,
      filteredTotalTime: 0,
      hasUsageData: false,
    } as any)
  }

  const verifyLoadingSpinnerVisible = () => {
    verifyTextPresent('Cargando información del usuario...')
  }

  const verifyUserProfileInfoInSidebar = (name: string, email: string, status: string) => {
    verifyTextPresent(name)
    verifyTextPresent(email)
    verifyTextPresent(status)
  }

  const verifyUsageAndActivitiesStats = () => {
    verifyTextPresent('Sesiones de hoy')
    verifyTextPresent('5')
    verifyTextPresent('Módulo favorito')
    expect(screen.getAllByText('Burbujas relajantes').length).toBeGreaterThanOrEqual(1)
    verifyTextPresent('12')
    verifyTextPresent('sesiones/semana')
    verifyTextPresent('Distribución de Uso de Funcionalidades')
    verifyTextPresent('8 sesiones')
    verifyTextPresent('Historial de uso reciente')
  }

  const verifyAIDiagnosticsVisible = () => {
    verifyTextPresent('Usuario centrado y reflexivo')
    verifyTextPresent('Foco: Estrés laboral o académico')
    verifyTextPresent('Preferencia: Meditación y Respiración')
    verifyTextPresent('80%')
    verifyTextPresent('Alta receptividad')
    verifyTextPresent('Respiración Guiada')
    verifyTextPresent('Retos Diarios')
  }

  const verifyFinancialMetricsVisible = () => {
    verifyTextPresent('Total ganancias generadas')
    expect(screen.getAllByText('$49,99').length).toBeGreaterThanOrEqual(2)
    verifyTextPresent('Historial de Transacciones')
    verifyTextPresent('Premium Plan')
    verifyTextPresent('APROBADO')
  }

  const verifyAntiScrollStatsVisible = () => {
    verifyTextPresent('Tiempo scrolleando por día')
    verifyTextPresent('Tiempo en cada dominio')
    verifyTextPresent('instagram.com')
    verifyTextPresent('1 h')
  }

  const verifyAntiScrollNoStatsButDaysVisible = () => {
    verifyTextPresent('Tiempo scrolleando por día')
    verifyTextPresent('Lunes')
    verifyTextPresent('Martes')
    verifyTextPresent('Tiempo en cada dominio')
    verifyTextPresent('Sin estadísticas de uso')
  }
})
