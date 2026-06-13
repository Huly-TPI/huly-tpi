import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import AntiScrollBackofficePage from '../../pages/Backoffice/AntiScrollBackofficePage'
import * as adminApi from '../../api/admin'

vi.mock('../../api/admin', () => ({
  getBackofficeUsers: vi.fn(),
  getAntiScrollDashboard: vi.fn(),
  getAntiScrollConfig: vi.fn(),
  saveAntiScrollConfig: vi.fn(),
}))

const mockedGetAntiScrollDashboard = vi.mocked(adminApi.getAntiScrollDashboard)
const mockedGetAntiScrollConfig = vi.mocked(adminApi.getAntiScrollConfig)

const defaultDashboard = {
  totalModalsShown: 100,
  totalRedirects: 40,
  totalUsersCount: 2,
  activeExtensionUsersCount: 1,
  dataSharingConsentUsersCount: 1,
  topUsedApps: [
    { domain: 'instagram.com', totalActiveSeconds: 3600 },
  ],
}

const defaultConfig = {
  defaultPauseIntervalMinutes: 20,
  termsAndConditions: 'El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir.',
}

describe('AntiScrollBackofficePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('muestra el estado cargando inicialmente', () => {
    mockedGetAntiScrollDashboard.mockReturnValue(new Promise(() => {}))
    mockedGetAntiScrollConfig.mockReturnValue(new Promise(() => {}))
    render(
      <MemoryRouter>
        <AntiScrollBackofficePage />
      </MemoryRouter>
    )
    expect(screen.getByText('Cargando datos de antiscroll...')).toBeInTheDocument()
  })

  it('renderiza el dashboard y sus componentes correctamente', async () => {
    mockedGetAntiScrollDashboard.mockResolvedValueOnce(defaultDashboard)
    mockedGetAntiScrollConfig.mockResolvedValueOnce(defaultConfig)

    render(
      <MemoryRouter>
        <AntiScrollBackofficePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.queryByText('Cargando datos de antiscroll...')).not.toBeInTheDocument()
    })

    expect(screen.getByText('Panel antiscroll')).toBeInTheDocument()
    expect(screen.getByText('40.0%')).toBeInTheDocument()
    expect(screen.getByText('instagram.com')).toBeInTheDocument()
    expect(screen.getAllByText('1')[0]).toBeInTheDocument() // Active extension users count check
    
    // Check configuration inputs
    expect(screen.getByLabelText('Tiempo de pausa por defecto (minutos)')).toBeInTheDocument()
    expect(screen.getByLabelText('Mensaje de consentimiento de datos')).toBeInTheDocument()
  })
})
