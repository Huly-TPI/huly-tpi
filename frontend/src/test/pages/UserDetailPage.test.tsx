import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import UserDetailPage from '../../pages/Backoffice/UserDetailPage'
import { useUsers } from '../../hooks/backoffice/useUsers'

vi.mock('../../hooks/backoffice/useUsers', () => ({
  useUsers: vi.fn(),
}))

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
    vi.clearAllMocks()
  })

  it('muestra spinner de carga cuando no hay usuario seleccionado', () => {
    mockedUseUsers.mockReturnValue({
      selectedUser: null,
    } as any)

    render(<UserDetailPage />)
    expect(screen.getByText('Cargando información del usuario...')).toBeInTheDocument()
  })

  it('renderiza la información del perfil del usuario en el sidebar', () => {
    mockedUseUsers.mockReturnValue({
      selectedUser: mockUser,
      activeTab: 'usage',
      setActiveTab: vi.fn(),
      activitiesLoading: false,
      activities: null,
    } as any)

    render(<UserDetailPage />)
    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.getByText('john@example.com')).toBeInTheDocument()
    expect(screen.getByText('USER')).toBeInTheDocument()
    expect(screen.getByText('ACTIVE')).toBeInTheDocument()
  })

  it('muestra la pestaña de Uso y Actividades con estadísticas', () => {
    mockedUseUsers.mockReturnValue({
      selectedUser: mockUser,
      activeTab: 'usage',
      setActiveTab: vi.fn(),
      activitiesLoading: false,
      activitiesError: null,
      activityTimeframe: 'total',
      setActivityTimeframe: vi.fn(),
      activities: {
        todayActivitiesCount: 5,
        favoriteActivity: 'BURBUJA',
        averageSessionsText: '12 sesiones/semana',
        activityDistribution: {
          BURBUJA: 8,
          RESPIRACION: 4,
        },
        activitySessions: [
          { id: 1, activityType: 'BURBUJA', createdAt: '2026-06-15T00:00:00Z' },
        ],
      },
    } as any)

    render(<UserDetailPage />)

    expect(screen.getByText('Sesiones de hoy')).toBeInTheDocument()
    expect(screen.getByText('5')).toBeInTheDocument()
    expect(screen.getByText('Módulo favorito')).toBeInTheDocument()
    expect(screen.getAllByText('Reventar Burbujas').length).toBeGreaterThanOrEqual(1)
    expect(screen.getByText('12')).toBeInTheDocument()
    expect(screen.getByText('sesiones/semana')).toBeInTheDocument()
    expect(screen.getByText('Distribución de Uso de Funcionalidades')).toBeInTheDocument()
    expect(screen.getByText('8 veces')).toBeInTheDocument()
    expect(screen.getByText('Historial de uso reciente')).toBeInTheDocument()
  })

  it('muestra la pestaña de IA y Decisiones con diagnósticos', () => {
    mockedUseUsers.mockReturnValue({
      selectedUser: mockUser,
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

    render(<UserDetailPage />)

    expect(screen.getByText('Usuario centrado y reflexivo')).toBeInTheDocument()
    expect(screen.getByText('Foco: Estrés laboral o académico')).toBeInTheDocument()
    expect(screen.getByText('Preferencia: Meditación y Respiración')).toBeInTheDocument()
    expect(screen.getByText('80%')).toBeInTheDocument()
    expect(screen.getByText('Alta receptividad')).toBeInTheDocument()
    expect(screen.getByText('Respiración Guiada')).toBeInTheDocument()
    expect(screen.getByText('Retos Diarios')).toBeInTheDocument()
  })

  it('muestra la pestaña de Métricas Financieras con ingresos', () => {
    mockedUseUsers.mockReturnValue({
      selectedUser: mockUser,
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

    render(<UserDetailPage />)

    expect(screen.getByText('Total ganancias generadas')).toBeInTheDocument()
    expect(screen.getAllByText('$49,99').length).toBeGreaterThanOrEqual(2)
    expect(screen.getByText('Historial de Transacciones')).toBeInTheDocument()
    expect(screen.getByText('Premium Plan')).toBeInTheDocument()
    expect(screen.getByText('APPROVED')).toBeInTheDocument()
  })

  it('muestra la pestaña de Antiscroll con estadísticas de navegación', () => {
    mockedUseUsers.mockReturnValue({
      selectedUser: mockUser,
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

    render(<UserDetailPage />)

    expect(screen.getByText('Tiempo scrolleando por día')).toBeInTheDocument()
    expect(screen.getByText('Tiempo en cada dominio')).toBeInTheDocument()
    expect(screen.getByText('instagram.com')).toBeInTheDocument()
    expect(screen.getByText('1:00 h')).toBeInTheDocument()
  })
})

