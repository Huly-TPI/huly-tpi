import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  api,
  getToken,
  setToken,
  clearToken,
  tryRehydrateSession,
  SessionExpiredError,
} from '../../api/client'



describe('client', () => {
  let authExpiredSpy: any

  beforeEach(() => {
    clearToken()
    vi.restoreAllMocks()
  })

  afterEach(() => {
    clearToken()
    vi.restoreAllMocks()
  })

  describe('manejo del token', () => {
    it('setToken y getToken funcionan', () => {
      return callSetTokenAndVerifyGetToken('abc')
    })

    it('clearToken borra el token', () => {
      return callSetTokenClearAndVerifyNull('abc')
    })

    it('getToken devuelve null si nunca se seteó', () => {
      return verifyGetTokenIsNull()
    })
  })

  describe('request', () => {
    it('incluye el header Authorization si hay token', () => {
      setupToken('my-token')
      setupSpyFetchResponse(200, { ok: true })
      return callApiGet('/test').then(() => {
        verifyFetchHeadersAuthorization('Bearer my-token')
      })
    })

    it('no incluye Authorization si no hay token', () => {
      setupSpyFetchResponse(200, {})
      return callApiGet('/test').then(() => {
        verifyFetchHeadersAuthorizationUndefined()
      })
    })

    it('devuelve el body parseado en éxito', () => {
      setupSpyFetchResponse(200, { id: 1, name: 'Mili' })
      return callApiGetAndVerifyResponse('/test', { id: 1, name: 'Mili' })
    })

    it('lanza ApiError en respuesta no-ok sin retry', () => {
      setupSpyFetchResponse(400, { message: 'Bad request' })
      return verifyApiGetThrowsError('/test', 'Bad request')
    })
  })

  describe('retry de refresh en 401', () => {
    it('reintenta el request tras refrescar el token con éxito', () => {
      setupToken('expired-token')
      setupSpyFetchResponses([
        mockResponse(401, {}),
        mockResponse(200, { accessToken: 'fresh-token' }),
        mockResponse(200, { data: 'ok' }),
      ])
      return callApiGetAndVerifyResponse('/protected', { data: 'ok' }).then(() => {
        verifyGetTokenEquals('fresh-token')
        verifyFetchCalledTimes(3)
      })
    })

    it('limpia el token, emite auth:expired y lanza SessionExpiredError si el refresh falla', () => {
      setupToken('expired-token')
      setupAuthExpiredListener()
      setupSpyFetchResponses([
        mockResponse(401, {}),
        mockResponse(401, {}),
      ])
      return verifyApiGetThrowsSessionExpiredError('/protected').then(() => {
        verifyGetTokenIsNull()
        verifyAuthExpiredListenerCalled()
        cleanupAuthExpiredListener()
      })
    })

    it('no reintenta infinitamente (un solo refresh)', () => {
      setupToken('expired-token')
      setupSpyFetchResponses([
        mockResponse(401, {}),
        mockResponse(200, { accessToken: 'fresh' }),
        mockResponse(401, {}),
      ])
      return verifyApiGetThrowsAnyError('/protected').then(() => {
        verifyFetchCalledTimes(3)
      })
    })
  })

  describe('tryRehydrateSession', () => {
    it('devuelve el accessToken y lo guarda en memoria si el refresh es exitoso', () => {
      setupSpyFetchResponse(200, { accessToken: 'rehydrated-token' })
      return callTryRehydrateSessionAndVerify('rehydrated-token').then(() => {
        verifyGetTokenEquals('rehydrated-token')
      })
    })

    it('devuelve null y no setea token si el refresh responde no-ok', () => {
      setupSpyFetchResponse(401, {})
      return callTryRehydrateSessionAndVerify(null).then(() => {
        verifyGetTokenIsNull()
      })
    })

    it('devuelve null si el body no trae accessToken', () => {
      setupSpyFetchResponse(200, {})
      return callTryRehydrateSessionAndVerify(null).then(() => {
        verifyGetTokenIsNull()
      })
    })

    it('devuelve null si el fetch lanza una excepción', () => {
      setupSpyFetchRejectedError(new Error('Network down'))
      return callTryRehydrateSessionAndVerify(null).then(() => {
        verifyGetTokenIsNull()
      })
    })
  })

  /* helpers */

  const setupToken = (token: string) => {
    setToken(token)
  }

  const callSetTokenAndVerifyGetToken = (token: string) => {
    setToken(token)
    expect(getToken()).toBe(token)
  }

  const callSetTokenClearAndVerifyNull = (token: string) => {
    setToken(token)
    clearToken()
    expect(getToken()).toBeNull()
  }

  const verifyGetTokenIsNull = () => {
    expect(getToken()).toBeNull()
  }

  const verifyGetTokenEquals = (expected: string) => {
    expect(getToken()).toBe(expected)
  }

  const setupSpyFetchResponse = (status: number, body: any) => {
    vi.spyOn(global, 'fetch').mockResolvedValue(mockResponse(status, body))
  }

  const setupSpyFetchResponses = (responses: Response[]) => {
    const fetchSpy = vi.spyOn(global, 'fetch')
    responses.forEach((res) => {
      fetchSpy.mockResolvedValueOnce(res)
    })
  }

  const setupSpyFetchRejectedError = (error: Error) => {
    vi.spyOn(global, 'fetch').mockRejectedValue(error)
  }

  const callApiGet = (url: string) => {
    return api.get(url)
  }

  const callApiGetAndVerifyResponse = (url: string, expectedBody: any) => {
    return api.get(url).then((res) => {
      expect(res).toEqual(expectedBody)
    })
  }

  const verifyApiGetThrowsError = (url: string, expectedMsg: string) => {
    return expect(api.get(url)).rejects.toThrow(expectedMsg)
  }

  const verifyApiGetThrowsSessionExpiredError = (url: string) => {
    return expect(api.get(url)).rejects.toBeInstanceOf(SessionExpiredError)
  }

  const verifyApiGetThrowsAnyError = (url: string) => {
    return expect(api.get(url)).rejects.toThrow()
  }

  const callTryRehydrateSessionAndVerify = (expectedResult: string | null) => {
    return tryRehydrateSession().then((res) => {
      expect(res).toBe(expectedResult)
    })
  }

  const verifyFetchHeadersAuthorization = (expectedAuth: string) => {
    const fetchMock = vi.mocked(global.fetch)
    const [, options] = fetchMock.mock.calls[0]
    expect((options?.headers as Record<string, string>).Authorization).toBe(expectedAuth)
  }

  const verifyFetchHeadersAuthorizationUndefined = () => {
    const fetchMock = vi.mocked(global.fetch)
    const [, options] = fetchMock.mock.calls[0]
    expect((options?.headers as Record<string, string>).Authorization).toBeUndefined()
  }

  const verifyFetchCalledTimes = (times: number) => {
    expect(global.fetch).toHaveBeenCalledTimes(times)
  }

  const setupAuthExpiredListener = () => {
    authExpiredSpy = vi.fn()
    window.addEventListener('auth:expired', authExpiredSpy)
  }

  const verifyAuthExpiredListenerCalled = () => {
    expect(authExpiredSpy).toHaveBeenCalled()
  }

  const cleanupAuthExpiredListener = () => {
    window.removeEventListener('auth:expired', authExpiredSpy)
  }
})

function mockResponse(status: number, body: unknown = {}) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(body ? JSON.stringify(body) : ''),
  } as Response
}
