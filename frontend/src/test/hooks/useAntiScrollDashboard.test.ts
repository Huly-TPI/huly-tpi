import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useAntiScrollDashboard } from '../../hooks/backoffice/useAntiScrollDashboard'
import * as adminApi from '../../api/admin'

vi.mock('../../api/admin', () => ({
  getAntiScrollDashboard: vi.fn(),
  getAntiScrollConfig: vi.fn(),
  saveAntiScrollConfig: vi.fn(),
}))

const mockedGetAntiScrollDashboard = vi.mocked(adminApi.getAntiScrollDashboard)
const mockedGetAntiScrollConfig = vi.mocked(adminApi.getAntiScrollConfig)
const mockedSaveAntiScrollConfig = vi.mocked(adminApi.saveAntiScrollConfig)

describe('useAntiScrollDashboard', () => {
  beforeEach(() => {
    clearAllMocks()
    vi.restoreAllMocks()
  })

  it('cargará los datos del dashboard y configuración al montar', () => {
    setupDashboardResolved(getMockDashboard())
    setupConfigResolved(getMockConfig())
    setupHook()
    verifyLoadingStates(true, true)
    return waitForLoadingFinished().then(() => {
      verifyLoadingStates(false, false)
      verifyDashboardData(getMockDashboard())
      verifyConfigFields(30, 'Términos y condiciones aceptados')
      verifyCalculatedMetrics(100, 40, 60, 40, 60, 1200)
    })
  })

  it('manejará errores si la carga falla', () => {
    setupDashboardRejected('Dashboard error')
    setupConfigRejected('Config error')
    setupHook()
    return waitForLoadingFinished().then(() => {
      verifyErrorMessage('No se pudieron cargar los datos de antiscroll')
      verifyDashboardIsNull()
    })
  })

  it('permitirá guardar la configuración correctamente', () => {
    setupDashboardResolved(getMockEmptyDashboard())
    setupConfigResolved({ defaultPauseIntervalMinutes: 20, termsAndConditions: 'Términos' })
    setupSaveConfigResolved()
    setupTimeoutMock()
    setupHook()
    return waitForLoadingFinished().then(() => {
      updateConfigInputs(25, 'Nuevos Términos')
      callSaveConfig()
      verifyConfigSaving(true)
      return resolveSubmitPromise().then(() => {
        verifyConfigSaving(false)
        verifySaveSuccess(true)
        verifySaveConfigCalledWith({ defaultPauseIntervalMinutes: 25, termsAndConditions: 'Nuevos Términos' })
        verifyTimerCallbackDefined()
        triggerTimerCallback()
        verifySaveSuccess(false)
      })
    })
  })

  it('manejará errores al intentar guardar la configuración', () => {
    setupDashboardResolved(getMockEmptyDashboard())
    setupConfigResolved({ defaultPauseIntervalMinutes: 20, termsAndConditions: 'Términos' })
    setupSaveConfigRejected('Save error')
    setupHook()
    return waitForLoadingFinished().then(() => {
      callSaveConfig()
      return resolveSubmitPromise().then(() => {
        verifyConfigSaving(false)
        verifySaveSuccess(false)
        verifyConfigError('No se pudo guardar la configuración')
      })
    })
  })
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useAntiScrollDashboard>, undefined>>
  let submitPromise: Promise<void>
  let timerCallback: (() => void) | undefined

  /* helpers */

  const getMockDashboard = () => ({
    totalUsersCount: 10,
    activeExtensionUsersCount: 5,
    dataSharingConsentUsersCount: 3,
    totalModalsShown: 100,
    totalRedirects: 40,
    topUsedApps: [
      { domain: 'instagram.com', totalActiveSeconds: 1200 },
      { domain: 'tiktok.com', totalActiveSeconds: 800 },
    ],
  })

  const getMockConfig = () => ({
    defaultPauseIntervalMinutes: 30,
    termsAndConditions: 'Términos y condiciones aceptados',
  })

  const getMockEmptyDashboard = () => ({
    totalUsersCount: 10,
    activeExtensionUsersCount: 5,
    dataSharingConsentUsersCount: 3,
    totalModalsShown: 0,
    totalRedirects: 0,
    topUsedApps: [],
  })

  const setupHook = () => {
    rendered = renderHook(() => useAntiScrollDashboard())
  }

  const setupDashboardResolved = (val: any) => {
    mockedGetAntiScrollDashboard.mockResolvedValueOnce(val)
  }

  const setupConfigResolved = (val: any) => {
    mockedGetAntiScrollConfig.mockResolvedValueOnce(val)
  }

  const setupDashboardRejected = (msg: string) => {
    mockedGetAntiScrollDashboard.mockRejectedValueOnce(new Error(msg))
  }

  const setupConfigRejected = (msg: string) => {
    mockedGetAntiScrollConfig.mockRejectedValueOnce(new Error(msg))
  }

  const setupSaveConfigResolved = () => {
    mockedSaveAntiScrollConfig.mockResolvedValueOnce(undefined)
  }

  const setupSaveConfigRejected = (msg: string) => {
    mockedSaveAntiScrollConfig.mockRejectedValueOnce(new Error(msg))
  }

  const setupTimeoutMock = () => {
    timerCallback = undefined
    const originalSetTimeout = global.setTimeout
    vi.spyOn(global, 'setTimeout').mockImplementation((cb, ms, ...args) => {
      if (ms === 3000) {
        timerCallback = cb as () => void
        return 123 as any
      }
      return originalSetTimeout(cb, ms, ...args)
    })
  }

  const waitForLoadingFinished = () => {
    return waitFor(() => {
      expect(rendered.result.current.loading).toBe(false)
    })
  }

  const updateConfigInputs = (defaultTime: number, terms: string) => {
    act(() => {
      rendered.result.current.setDefaultTime(defaultTime)
      rendered.result.current.setTerms(terms)
    })
  }

  const callSaveConfig = () => {
    act(() => {
      submitPromise = rendered.result.current.handleSaveConfig({
        preventDefault: vi.fn(),
      } as unknown as React.FormEvent)
    })
  }

  const resolveSubmitPromise = async () => {
    await act(async () => {
      await submitPromise
    })
  }

  const triggerTimerCallback = () => {
    act(() => {
      timerCallback?.()
    })
  }

  const verifyLoadingStates = (loading: boolean, configLoading: boolean) => {
    expect(rendered.result.current.loading).toBe(loading)
    expect(rendered.result.current.configLoading).toBe(configLoading)
  }

  const verifyDashboardData = (expected: any) => {
    expect(rendered.result.current.dashboard).toEqual(expected)
  }

  const verifyConfigFields = (defaultTime: number, terms: string) => {
    expect(rendered.result.current.defaultTime).toBe(defaultTime)
    expect(rendered.result.current.terms).toBe(terms)
  }

  const verifyCalculatedMetrics = (
    totalModals: number,
    totalRedirects: number,
    totalIgnore: number,
    redirectRate: number,
    ignoreRate: number,
    maxAppTime: number
  ) => {
    expect(rendered.result.current.totalModals).toBe(totalModals)
    expect(rendered.result.current.totalRedirects).toBe(totalRedirects)
    expect(rendered.result.current.totalIgnore).toBe(totalIgnore)
    expect(rendered.result.current.redirectRate).toBe(redirectRate)
    expect(rendered.result.current.ignoreRate).toBe(ignoreRate)
    expect(rendered.result.current.maxAppTime).toBe(maxAppTime)
  }

  const verifyErrorMessage = (expected: string) => {
    expect(rendered.result.current.error).toBe(expected)
  }

  const verifyDashboardIsNull = () => {
    expect(rendered.result.current.dashboard).toBeNull()
  }

  const verifyConfigSaving = (expected: boolean) => {
    expect(rendered.result.current.configSaving).toBe(expected)
  }

  const verifySaveSuccess = (expected: boolean) => {
    expect(rendered.result.current.saveSuccess).toBe(expected)
  }

  const verifyConfigError = (expected: string) => {
    expect(rendered.result.current.configError).toBe(expected)
  }

  const verifySaveConfigCalledWith = (expected: any) => {
    expect(mockedSaveAntiScrollConfig).toHaveBeenCalledWith(expected)
  }

  const verifyTimerCallbackDefined = () => {
    expect(timerCallback).toBeDefined()
  }
})
