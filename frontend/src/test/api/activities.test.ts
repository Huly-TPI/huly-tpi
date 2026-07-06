import { clearAllMocks } from '../testHelpers'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ActivityType, registerActivitySession } from '../../api/activities'
import * as clientModule from '../../api/client'

vi.mock('../../api/client', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/client')>()
  return {
    ...actual,
    getToken: vi.fn(),
    tryRehydrateSession: vi.fn(),
    clearToken: vi.fn(),
  }
})

const mockedGetToken = vi.mocked(clientModule.getToken)
const mockedTryRehydrateSession = vi.mocked(clientModule.tryRehydrateSession)

describe('registerActivitySession', () => {
  beforeEach(() => {
    clearAllMocks()
    vi.stubGlobal('fetch', vi.fn())
    mockedGetToken.mockReturnValue('token-de-prueba')
    mockedTryRehydrateSession.mockResolvedValue(null)
  })

  it('llama a fetch con la ruta y los datos correctos', () => {
    setupMockedFetchResponse(200)
    return callRegisterActivitySession({ activityType: ActivityType.BREATHING }).then(() => {
      verifyFetchCalledWithCorrectData(ActivityType.BREATHING)
    })
  })

  it('pasa keepalive cuando se indica', () => {
    setupMockedFetchResponse(200)
    return callRegisterActivitySession(
      { activityType: ActivityType.BREATHING },
      { keepalive: true },
    ).then(() => {
      verifyFetchCalledWithKeepalive()
    })
  })

  it('propaga errores del backend', () => {
    setupMockedFetchResponse(401, { message: 'No autorizado' })
    return verifyRegisterActivitySessionThrowsError(
      { activityType: ActivityType.BUBBLE },
      'Session expired',
    )
  })

  /* helpers */

  const setupMockedFetchResponse = (status: number, bodyObj?: any) => {
    const responseBody = bodyObj ? JSON.stringify(bodyObj) : null
    const headers = bodyObj ? { 'Content-Type': 'application/json' } : undefined
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(responseBody, { status, headers }),
    )
  }

  const callRegisterActivitySession = (
    data: { activityType: ActivityType },
    options?: { keepalive?: boolean },
  ) => {
    return registerActivitySession(data, options)
  }

  const verifyFetchCalledWithCorrectData = (activityType: ActivityType) => {
    expect(fetch).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/activities\/sessions$/),
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify({ activityType }),
      }),
    )
  }

  const verifyFetchCalledWithKeepalive = () => {
    expect(fetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        keepalive: true,
      }),
    )
  }

  const verifyRegisterActivitySessionThrowsError = (
    data: { activityType: ActivityType },
    expectedErrorMsg: string,
  ) => {
    return expect(registerActivitySession(data)).rejects.toThrow(expectedErrorMsg)
  }
})
