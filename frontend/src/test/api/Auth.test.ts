import { describe, it, expect, vi, beforeEach } from 'vitest'
import { register, login } from '../../api/auth'
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
        mockedPost.mockResolvedValueOnce(undefined)

        await register(validRegisterRequest)

        expect(mockedPost).toHaveBeenCalledWith('/auth/register', validRegisterRequest)
    })

    it('no retorna accessToken tras registro', async () => {
        mockedPost.mockResolvedValueOnce(undefined)

        const res = await register(validRegisterRequest)

        expect(res).toBeUndefined()
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
        mockedPost.mockResolvedValueOnce({ role: 'USER' })

        await login(validLoginRequest)

        expect(mockedPost).toHaveBeenCalledWith('/auth/login', validLoginRequest)
    })

    it('retorna el role del backend', async () => {
        mockedPost.mockResolvedValueOnce({ role: 'USER' })

        const res = await login(validLoginRequest)

        expect(res.role).toBe('USER')
    })

    it('no retorna accessToken en la respuesta', async () => {
        mockedPost.mockResolvedValueOnce({ role: 'USER' })

        const res = await login(validLoginRequest)

        expect((res as Record<string, unknown>).accessToken).toBeUndefined()
    })

    it('propaga el error del backend', async () => {
        mockedPost.mockRejectedValueOnce(new Error('Invalid credentials'))

        await expect(login(validLoginRequest)).rejects.toThrow('Invalid credentials')
    })

    it('propaga errores por campo del backend', async () => {
        const apiError = new ApiError('Error de validación', { email: 'Email inválido' })
        mockedPost.mockRejectedValueOnce(apiError)

        await expect(login(validLoginRequest)).rejects.toThrow(apiError)
    })
})