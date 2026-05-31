import { describe, it, expect, vi, beforeEach } from 'vitest'
import { register, login, logout } from '../../api/auth'
import type { RegisterRequest, LoginRequest } from '../../api/auth'
import { api } from '../../api/client'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/client', () => ({
    api: {
        post: vi.fn(),
    },
}))

const mockedPost = vi.mocked(api.post)

const validRegisterRequest: RegisterRequest = {
    name: 'Mili',
    email: 'mili@mail.com',
    password: '123456',
    birthDate: '2000-01-15',
}

const validLoginRequest: LoginRequest = {
    email: 'mili@mail.com',
    password: '123456',
}

describe('register', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('llama a POST /auth/register con los datos correctos', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123' })

        await register(validRegisterRequest)

        expect(mockedPost).toHaveBeenCalledWith('/auth/register', validRegisterRequest)
    })

    it('retorna el accessToken del backend', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123' })

        const res = await register(validRegisterRequest)

        expect(res.accessToken).toBe('token-123')
    })

    it('propaga el error del backend', async () => {
        mockedPost.mockRejectedValueOnce(new Error('El email ya está registrado'))

        await expect(register(validRegisterRequest)).rejects.toThrow('El email ya está registrado')
    })

    it('propaga errores por campo del backend', async () => {
        const apiError = new ApiError('Error de validación', { email: 'Email ya registrado' })
        mockedPost.mockRejectedValueOnce(apiError)

        await expect(register(validRegisterRequest)).rejects.toThrow(apiError)
    })
})

describe('login', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('llama a POST /auth/login con los datos correctos', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })

        await login(validLoginRequest)

        expect(mockedPost).toHaveBeenCalledWith('/auth/login', validLoginRequest)
    })

    it('retorna accessToken y role del backend', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })

        const res = await login(validLoginRequest)

        expect(res.accessToken).toBe('token-123')
        expect(res.role).toBe('USER')
    })

    it('propaga el error si las credenciales son inválidas', async () => {
        mockedPost.mockRejectedValueOnce(new Error('Credenciales inválidas'))

        await expect(login(validLoginRequest)).rejects.toThrow('Credenciales inválidas')
    })

    it('propaga errores por campo del backend', async () => {
        const apiError = new ApiError('Error de validación', { email: 'Email no registrado' })
        mockedPost.mockRejectedValueOnce(apiError)

        await expect(login(validLoginRequest)).rejects.toThrow(apiError)
    })
})

describe('logout', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('llama a POST /auth/logout', async () => {
        mockedPost.mockResolvedValueOnce(null)

        await logout()

        expect(mockedPost).toHaveBeenCalledWith('/auth/logout', {})
    })

    it('propaga el error si falla', async () => {
        mockedPost.mockRejectedValueOnce(new Error('Sin conexión'))

        await expect(logout()).rejects.toThrow('Sin conexión')
    })
})