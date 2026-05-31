import { describe, it, expect, vi, beforeEach } from 'vitest'
import { register } from '../../api/auth'
import type { RegisterRequest } from '../../api/auth'
import { api } from '../../api/client'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/client', () => ({
    api: {
        post: vi.fn(),
    },
}))

const mockedPost = vi.mocked(api.post)

const validRequest: RegisterRequest = {
    name: 'Mili',
    email: 'mili@mail.com',
    password: '123456',
    birthDate: '2000-01-15',
}

describe('register', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('llama a POST /auth/register con los datos correctos', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123' })

        await register(validRequest)

        expect(mockedPost).toHaveBeenCalledWith('/auth/register', validRequest)
    })

    it('retorna el accessToken del backend', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123' })

        const res = await register(validRequest)

        expect(res.accessToken).toBe('token-123')
    })

    it('propaga el error del backend', async () => {
        mockedPost.mockRejectedValueOnce(new Error('El email ya está registrado'))

        await expect(register(validRequest)).rejects.toThrow('El email ya está registrado')
    })

    it('propaga errores por campo del backend', async () => {
        const apiError = new ApiError('Error de validación', { email: 'Email ya registrado' })
        mockedPost.mockRejectedValueOnce(apiError)

        await expect(register(validRequest)).rejects.toThrow(apiError)
    })
})