import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import AntiScrollPage from '../../pages/Backoffice/AntiScrollPage'
import * as adminApi from '../../api/admin'
import { verifyTextPresent, verifyTextNotPresent, clearAllMocks } from '../testHelpers'

vi.mock('../../api/admin', () => ({
  getUsers: vi.fn(),
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

describe('AntiScrollPage', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('muestra el estado cargando inicialmente', () => {
    setupDashboardPromisePending()
    setupConfigPromisePending()
    renderAntiScrollPage()
    verifyLoadingStateVisible()
  })

  it('renderiza el dashboard y sus componentes correctamente', () => {
    setupDashboardResponse(defaultDashboard)
    setupConfigResponse(defaultConfig)
    renderAntiScrollPage()
    return waitForLoadingStateToDisappear().then(() => {
      verifyDashboardComponentsRendered()
    })
  })

  /* helpers */

  const renderAntiScrollPage = () => {
    render(
      <MemoryRouter>
        <AntiScrollPage />
      </MemoryRouter>
    )
  }

  const setupDashboardPromisePending = () => {
    mockedGetAntiScrollDashboard.mockReturnValue(new Promise(() => {}))
  }

  const setupConfigPromisePending = () => {
    mockedGetAntiScrollConfig.mockReturnValue(new Promise(() => {}))
  }

  const setupDashboardResponse = (dashboard: any) => {
    mockedGetAntiScrollDashboard.mockResolvedValueOnce(dashboard)
  }

  const setupConfigResponse = (config: any) => {
    mockedGetAntiScrollConfig.mockResolvedValueOnce(config)
  }

  const verifyLoadingStateVisible = () => {
    verifyTextPresent('Cargando datos de antiscroll...')
  }

  const waitForLoadingStateToDisappear = () => {
    return waitFor(() => {
      verifyTextNotPresent('Cargando datos de antiscroll...')
    })
  }

  const verifyDashboardComponentsRendered = () => {
    verifyTextPresent('Panel antiscroll')
    verifyTextPresent('40.0%')
    verifyTextPresent('instagram.com')
    expect(screen.getAllByText('1')[0]).toBeInTheDocument()
    expect(screen.getByLabelText('Tiempo de pausa por defecto (minutos)')).toBeInTheDocument()
    expect(screen.getByLabelText('Mensaje de consentimiento de datos')).toBeInTheDocument()
  }
})
