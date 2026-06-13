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
    vi.clearAllMocks()
    vi.restoreAllMocks()
  })

  it('cargará los datos del dashboard y configuración al montar', async () => {
    const mockDashboard = {
      totalUsersCount: 10,
      activeExtensionUsersCount: 5,
      dataSharingConsentUsersCount: 3,
      totalModalsShown: 100,
      totalRedirects: 40,
      topUsedApps: [
        { domain: 'instagram.com', totalActiveSeconds: 1200 },
        { domain: 'tiktok.com', totalActiveSeconds: 800 },
      ],
    }

    const mockConfig = {
      defaultPauseIntervalMinutes: 30,
      termsAndConditions: 'Términos y condiciones aceptados',
    }

    mockedGetAntiScrollDashboard.mockResolvedValueOnce(mockDashboard)
    mockedGetAntiScrollConfig.mockResolvedValueOnce(mockConfig)

    const { result } = renderHook(() => useAntiScrollDashboard())

    expect(result.current.loading).toBe(true)
    expect(result.current.configLoading).toBe(true)

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.configLoading).toBe(false)
    expect(result.current.dashboard).toEqual(mockDashboard)
    expect(result.current.defaultTime).toBe(30)
    expect(result.current.terms).toBe('Términos y condiciones aceptados')

    expect(result.current.totalModals).toBe(100)
    expect(result.current.totalRedirects).toBe(40)
    expect(result.current.totalIgnore).toBe(60)
    expect(result.current.redirectRate).toBe(40)
    expect(result.current.ignoreRate).toBe(60)
    expect(result.current.maxAppTime).toBe(1200)
  })

  it('manejará errores si la carga falla', async () => {
    mockedGetAntiScrollDashboard.mockRejectedValueOnce(new Error('Dashboard error'))
    mockedGetAntiScrollConfig.mockRejectedValueOnce(new Error('Config error'))

    const { result } = renderHook(() => useAntiScrollDashboard())

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.error).toBe('No se pudieron cargar los datos de antiscroll')
    expect(result.current.dashboard).toBeNull()
  })

  it('permitirá guardar la configuración correctamente', async () => {
    const mockDashboard = {
      totalUsersCount: 10,
      activeExtensionUsersCount: 5,
      dataSharingConsentUsersCount: 3,
      totalModalsShown: 0,
      totalRedirects: 0,
      topUsedApps: [],
    }

    const mockConfig = {
      defaultPauseIntervalMinutes: 20,
      termsAndConditions: 'Términos',
    }

    mockedGetAntiScrollDashboard.mockResolvedValueOnce(mockDashboard)
    mockedGetAntiScrollConfig.mockResolvedValueOnce(mockConfig)
    mockedSaveAntiScrollConfig.mockResolvedValueOnce(undefined)

    let timerCallback: (() => void) | undefined
    const originalSetTimeout = global.setTimeout
    vi.spyOn(global, 'setTimeout').mockImplementation((cb, ms, ...args) => {
      if (ms === 3000) {
        timerCallback = cb as () => void
        return 123 as any
      }
      return originalSetTimeout(cb, ms, ...args)
    })

    const { result } = renderHook(() => useAntiScrollDashboard())

    await waitFor(() => expect(result.current.loading).toBe(false))

    act(() => {
      result.current.setDefaultTime(25)
      result.current.setTerms('Nuevos Términos')
    })

    let submitPromise: Promise<void>
    act(() => {
      submitPromise = result.current.handleSaveConfig({
        preventDefault: vi.fn(),
      } as unknown as React.FormEvent)
    })

    expect(result.current.configSaving).toBe(true)

    await act(async () => {
      await submitPromise
    })

    expect(result.current.configSaving).toBe(false)
    expect(result.current.saveSuccess).toBe(true)
    expect(mockedSaveAntiScrollConfig).toHaveBeenCalledWith({
      defaultPauseIntervalMinutes: 25,
      termsAndConditions: 'Nuevos Términos',
    })

    expect(timerCallback).toBeDefined()
    act(() => {
      timerCallback?.()
    })
    expect(result.current.saveSuccess).toBe(false)
  })

  it('manejará errores al intentar guardar la configuración', async () => {
    const mockDashboard = {
      totalUsersCount: 10,
      activeExtensionUsersCount: 5,
      dataSharingConsentUsersCount: 3,
      totalModalsShown: 0,
      totalRedirects: 0,
      topUsedApps: [],
    }

    const mockConfig = {
      defaultPauseIntervalMinutes: 20,
      termsAndConditions: 'Términos',
    }

    mockedGetAntiScrollDashboard.mockResolvedValueOnce(mockDashboard)
    mockedGetAntiScrollConfig.mockResolvedValueOnce(mockConfig)
    mockedSaveAntiScrollConfig.mockRejectedValueOnce(new Error('Save error'))

    const { result } = renderHook(() => useAntiScrollDashboard())

    await waitFor(() => expect(result.current.loading).toBe(false))

    let submitPromise: Promise<void>
    act(() => {
      submitPromise = result.current.handleSaveConfig({
        preventDefault: vi.fn(),
      } as unknown as React.FormEvent)
    })

    await act(async () => {
      await submitPromise
    })

    expect(result.current.configSaving).toBe(false)
    expect(result.current.saveSuccess).toBe(false)
    expect(result.current.configError).toBe('No se pudo guardar la configuración')
  })
})
