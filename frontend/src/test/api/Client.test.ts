import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  api,
  getToken,
  setToken,
  clearToken,
  tryRehydrateSession,
  SessionExpiredError,
} from '../../api/client'

function mockResponse(status: number, body: unknown = {}) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(body ? JSON.stringify(body) : ''),
  } as Response
}

describe('client', () => {
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
      setToken('abc')
      expect(getToken()).toBe('abc')
    })

    it('clearToken borra el token', () => {
      setToken('abc')
      clearToken()
      expect(getToken()).toBeNull()
    })

    it('getToken devuelve null si nunca se seteó', () => {
      expect(getToken()).toBeNull()
    })
  })

  describe('request', () => {
    it('incluye el header Authorization si hay token', async () => {
      setToken('my-token')
      const fetchMock = vi
        .spyOn(global, 'fetch')
        .mockResolvedValue(mockResponse(200, { ok: true }))

      await api.get('/test')

      const [, options] = fetchMock.mock.calls[0]
      expect((options?.headers as Record<string, string>).Authorization).toBe(
        'Bearer my-token',
      )
    })

    it('no incluye Authorization si no hay token', async () => {
      const fetchMock = vi
        .spyOn(global, 'fetch')
        .mockResolvedValue(mockResponse(200, {}))

      await api.get('/test')

      const [, options] = fetchMock.mock.calls[0]
      expect(
        (options?.headers as Record<string, string>).Authorization,
      ).toBeUndefined()
    })

    it('devuelve el body parseado en éxito', async () => {
      vi.spyOn(global, 'fetch').mockResolvedValue(
        mockResponse(200, { id: 1, name: 'Mili' }),
      )

      const result = await api.get<{ id: number; name: string }>('/test')

      expect(result).toEqual({ id: 1, name: 'Mili' })
    })

    it('lanza ApiError en respuesta no-ok sin retry', async () => {
      vi.spyOn(global, 'fetch').mockResolvedValue(
        mockResponse(400, { message: 'Bad request' }),
      )

      await expect(api.get('/test')).rejects.toThrow('Bad request')
    })
  })

  describe('retry de refresh en 401', () => {
    it('reintenta el request tras refrescar el token con éxito', async () => {
      setToken('expired-token')
      const fetchMock = vi
        .spyOn(global, 'fetch')
        .mockResolvedValueOnce(mockResponse(401, {}))
        .mockResolvedValueOnce(mockResponse(200, { accessToken: 'fresh-token' }))
        .mockResolvedValueOnce(mockResponse(200, { data: 'ok' }))

      const result = await api.get<{ data: string }>('/protected')

      expect(result).toEqual({ data: 'ok' })
      expect(getToken()).toBe('fresh-token')
      expect(fetchMock).toHaveBeenCalledTimes(3)
    })

    it('limpia el token, emite auth:expired y lanza SessionExpiredError si el refresh falla', async () => {
      setToken('expired-token')
      const expiredHandler = vi.fn()
      window.addEventListener('auth:expired', expiredHandler)

      vi.spyOn(global, 'fetch')
        .mockResolvedValueOnce(mockResponse(401, {}))
        .mockResolvedValueOnce(mockResponse(401, {}))

      await expect(api.get('/protected')).rejects.toBeInstanceOf(SessionExpiredError)
      expect(getToken()).toBeNull()
      expect(expiredHandler).toHaveBeenCalled()

      window.removeEventListener('auth:expired', expiredHandler)
    })

    it('no reintenta infinitamente (un solo refresh)', async () => {
      setToken('expired-token')
      const fetchMock = vi
        .spyOn(global, 'fetch')
        .mockResolvedValueOnce(mockResponse(401, {}))
        .mockResolvedValueOnce(mockResponse(200, { accessToken: 'fresh' }))
        .mockResolvedValueOnce(mockResponse(401, {}))

      await expect(api.get('/protected')).rejects.toThrow()
      expect(fetchMock).toHaveBeenCalledTimes(3)
    })
  })

  describe('tryRehydrateSession', () => {
    it('devuelve el accessToken y lo guarda en memoria si el refresh es exitoso', async () => {
      vi.spyOn(global, 'fetch').mockResolvedValue(
        mockResponse(200, { accessToken: 'rehydrated-token' }),
      )

      const result = await tryRehydrateSession()

      expect(result).toBe('rehydrated-token')
      expect(getToken()).toBe('rehydrated-token')
    })

    it('devuelve null y no setea token si el refresh responde no-ok', async () => {
      vi.spyOn(global, 'fetch').mockResolvedValue(mockResponse(401, {}))

      const result = await tryRehydrateSession()

      expect(result).toBeNull()
      expect(getToken()).toBeNull()
    })

    it('devuelve null si el body no trae accessToken', async () => {
      vi.spyOn(global, 'fetch').mockResolvedValue(mockResponse(200, {}))

      const result = await tryRehydrateSession()

      expect(result).toBeNull()
      expect(getToken()).toBeNull()
    })

    it('devuelve null si el fetch lanza una excepción', async () => {
      vi.spyOn(global, 'fetch').mockRejectedValue(new Error('Network down'))

      const result = await tryRehydrateSession()

      expect(result).toBeNull()
      expect(getToken()).toBeNull()
    })
  })
})