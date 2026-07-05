import { describe, it, expect, vi, beforeEach } from 'vitest'
import { register, login, logout, backofficeLogin, changePassword, getAccountSettings, updateAccountSettings } from '../../api/auth'
import type { RegisterRequest, LoginRequest } from '../../api/auth'
import { api } from '../../api/client'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/client', () => ({
    api: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
    },
}))

const mockedGet = vi.mocked(api.get)
const mockedPost = vi.mocked(api.post)
const mockedPut = vi.mocked(api.put)

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

        expect(mockedPost).toHaveBeenCalledWith('/auth/register', validRegisterRequest, { skipAuthRedirect: true })
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

        expect(mockedPost).toHaveBeenCalledWith('/auth/login', validLoginRequest, { skipAuthRedirect: true })
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

describe('backofficeLogin', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('llama a POST /auth/backoffice/login con los datos correctos y skipAuthRedirect', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123', role: 'ADMIN' })

        await backofficeLogin(validLoginRequest)

        expect(mockedPost).toHaveBeenCalledWith(
            '/auth/backoffice/login',
            validLoginRequest,
            { skipAuthRedirect: true },
        )
    })

    it('retorna accessToken y role ADMIN del backend', async () => {
        mockedPost.mockResolvedValueOnce({ accessToken: 'token-123', role: 'ADMIN' })

        const res = await backofficeLogin(validLoginRequest)

        expect(res.accessToken).toBe('token-123')
        expect(res.role).toBe('ADMIN')
    })

    it('propaga el error si las credenciales son inválidas', async () => {
        mockedPost.mockRejectedValueOnce(new Error('Invalid credentials'))

        await expect(backofficeLogin(validLoginRequest)).rejects.toThrow('Invalid credentials')
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

describe('changePassword', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('llama a PUT /users/me/password con la contraseña actual y la nueva', async () => {
        mockedPut.mockResolvedValueOnce(undefined)

        await changePassword('currentPass123', 'newPass456')

        expect(mockedPut).toHaveBeenCalledWith('/users/me/password', {
            currentPassword: 'currentPass123',
            newPassword: 'newPass456',
        })
    })

    it('resuelve sin valor cuando el cambio es exitoso', async () => {
        mockedPut.mockResolvedValueOnce(undefined)

        await expect(changePassword('currentPass123', 'newPass456')).resolves.toBeUndefined()
    })

    it('propaga el error si la contraseña actual es incorrecta', async () => {
        mockedPut.mockRejectedValueOnce(new Error('Current password is incorrect'))

        await expect(changePassword('wrongPass', 'newPass456')).rejects.toThrow('Current password is incorrect')
    })

    it('propaga el error ante fallas del servidor', async () => {
        mockedPut.mockRejectedValueOnce(new Error('Internal server error'))

        await expect(changePassword('currentPass123', 'newPass456')).rejects.toThrow('Internal server error')
    })
})

describe('accountSettings', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('getAccountSettings llama a GET /users/me/settings', async () => {
        mockedGet.mockResolvedValueOnce({
            name: 'Mili',
            email: 'mili@mail.com',
            birthDate: '2000-01-15',
        })

        const response = await getAccountSettings()

        expect(mockedGet).toHaveBeenCalledWith('/users/me/settings')
        expect(response.email).toBe('mili@mail.com')
    })

    it('updateAccountSettings llama a PUT /users/me/settings con los datos permitidos', async () => {
        const request = { name: 'Mili', birthDate: '2000-01-15' }
        mockedPut.mockResolvedValueOnce({
            ...request,
            email: 'mili@mail.com',
        })

        await updateAccountSettings(request)

        expect(mockedPut).toHaveBeenCalledWith('/users/me/settings', request)
    })
})
