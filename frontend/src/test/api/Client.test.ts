import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ApiError } from '../../api/apiError'

const mockFetch = vi.fn()
vi.stubGlobal('fetch', mockFetch)

const mockLocation = { href: '' }
vi.stubGlobal('location', mockLocation)

function mockResponse(status: number, body: unknown) {
    return {
        ok: status >= 200 && status < 300,
        status,
        json: () => Promise.resolve(body),
        text: () => Promise.resolve(JSON.stringify(body)),
    }
}

describe('client', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        localStorage.clear()
        mockLocation.href = ''
    })

    it('manda el token del localStorage en el header Authorization', async () => {
        localStorage.setItem('token', 'my-token')
        mockFetch.mockResolvedValueOnce(mockResponse(200, { data: 'ok' }))

        const { api } = await import('../../api/client')
        await api.get('/some-endpoint')

        expect(mockFetch).toHaveBeenCalledWith(
            expect.stringContaining('/some-endpoint'),
            expect.objectContaining({
                headers: expect.objectContaining({
                    Authorization: 'Bearer my-token',
                }),
            }),
        )
    })

    it('no manda Authorization si no hay token', async () => {
        mockFetch.mockResolvedValueOnce(mockResponse(200, { data: 'ok' }))

        const { api } = await import('../../api/client')
        await api.get('/some-endpoint')

        const headers = mockFetch.mock.calls[0][1].headers
        expect(headers).not.toHaveProperty('Authorization')
    })

    it('lanza ApiError cuando la respuesta no es ok', async () => {
        mockFetch.mockResolvedValueOnce(mockResponse(400, { message: 'Bad request' }))

        const { api } = await import('../../api/client')

        await expect(api.get('/some-endpoint')).rejects.toBeInstanceOf(ApiError)
    })

    it('usa el mensaje del body en el ApiError', async () => {
        mockFetch.mockResolvedValueOnce(mockResponse(400, { message: 'Campo inválido' }))

        const { api } = await import('../../api/client')

        await expect(api.get('/some-endpoint')).rejects.toThrow('Campo inválido')
    })

    it('intenta el refresh cuando recibe 401', async () => {
        localStorage.setItem('token', 'expired-token')
        mockFetch
            .mockResolvedValueOnce(mockResponse(401, { message: 'Unauthorized' }))
            .mockResolvedValueOnce(mockResponse(200, { accessToken: 'new-token' }))
            .mockResolvedValueOnce(mockResponse(200, { data: 'ok' }))

        const { api } = await import('../../api/client')
        await api.get('/protected')

        expect(mockFetch).toHaveBeenCalledTimes(3)
        expect(localStorage.getItem('token')).toBe('new-token')
    })

    it('redirige a /login si el refresh falla', async () => {
        localStorage.setItem('token', 'expired-token')
        mockFetch
            .mockResolvedValueOnce(mockResponse(401, { message: 'Unauthorized' }))
            .mockResolvedValueOnce(mockResponse(401, { message: 'Refresh inválido' }))

        const { api } = await import('../../api/client')

        await expect(api.get('/protected')).rejects.toBeInstanceOf(ApiError)
        expect(mockLocation.href).toBe('/login')
    })

    it('limpia localStorage si el refresh falla', async () => {
        localStorage.setItem('token', 'expired-token')
        localStorage.setItem('role', 'USER')
        mockFetch
            .mockResolvedValueOnce(mockResponse(401, { message: 'Unauthorized' }))
            .mockResolvedValueOnce(mockResponse(401, { message: 'Refresh inválido' }))

        const { api } = await import('../../api/client')

        await expect(api.get('/protected')).rejects.toBeInstanceOf(ApiError)
        expect(localStorage.getItem('token')).toBeNull()
        expect(localStorage.getItem('role')).toBeNull()
    })

    it('no reintenta el refresh en el segundo 401', async () => {
        localStorage.setItem('token', 'expired-token')
        mockFetch
            .mockResolvedValueOnce(mockResponse(401, { message: 'Unauthorized' }))
            .mockResolvedValueOnce(mockResponse(200, { accessToken: 'new-token' }))
            .mockResolvedValueOnce(mockResponse(401, { message: 'Unauthorized again' }))

        const { api } = await import('../../api/client')

        await expect(api.get('/protected')).rejects.toBeInstanceOf(ApiError)
        expect(mockFetch).toHaveBeenCalledTimes(3)
    })

    it('retorna null cuando la respuesta no tiene body', async () => {
        mockFetch.mockResolvedValueOnce({
            ok: true,
            status: 204,
            text: () => Promise.resolve(''),
            json: () => Promise.resolve(null),
        })

        const { api } = await import('../../api/client')
        const result = await api.delete('/some-endpoint')

        expect(result).toBeNull()
    })
})