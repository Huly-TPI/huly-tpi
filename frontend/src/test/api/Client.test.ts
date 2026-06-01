import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { api, getToken, setToken, clearToken } from '../../api/client'

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
    localStorage.clear()
    vi.restoreAllMocks()
  })

  afterEach(() => {
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
        // 1er intento: 401
        .mockResolvedValueOnce(mockResponse(401, {}))
        // refresh: ok con token nuevo
        .mockResolvedValueOnce(mockResponse(200, { accessToken: 'fresh-token' }))
        // reintento: ok
        .mockResolvedValueOnce(mockResponse(200, { data: 'ok' }))

      const result = await api.get<{ data: string }>('/protected')

      expect(result).toEqual({ data: 'ok' })
      expect(getToken()).toBe('fresh-token')
      expect(fetchMock).toHaveBeenCalledTimes(3)
    })

    it('limpia el token y emite auth:expired si el refresh falla', async () => {
      setToken('expired-token')
      const expiredHandler = vi.fn()
      window.addEventListener('auth:expired', expiredHandler)

      vi.spyOn(global, 'fetch')
        .mockResolvedValueOnce(mockResponse(401, {}))
        .mockResolvedValueOnce(mockResponse(401, {}))

      await expect(api.get('/protected')).rejects.toThrow()
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
})