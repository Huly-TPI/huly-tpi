import { clearAllMocks, setupMockedPostResponse, setupMockedPostError, setupMockedPutResponse, setupMockedPutError } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { register, login, logout, backofficeLogin, changePassword } from '../../api/auth'
import type { RegisterRequest, LoginRequest } from '../../api/auth'
import { api } from '../../api/client'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/client', () => ({
    api: {
        post: vi.fn(),
        put: vi.fn(),
    },
}))

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

describe('API de Autenticación', () => {
    beforeEach(() => {
        clearAllMocks()
    })

    describe('register', () => {
        it('llama a POST /auth/register con los datos correctos', () => {
            setupMockedPostResponse({ accessToken: 'token-123' })
            return callRegister(validRegisterRequest).then(() => {
                verifyPostCalledWith('/auth/register', validRegisterRequest, { skipAuthRedirect: true })
            })
        })

        it('retorna el accessToken del backend', () => {
            setupMockedPostResponse({ accessToken: 'token-123' })
            return callRegisterAndVerifyAccessToken(validRegisterRequest, 'token-123')
        })

        it('propaga el error del backend', () => {
            setupMockedPostError(new Error('El email ya está registrado'))
            return verifyRegisterThrowsError(validRegisterRequest, 'El email ya está registrado')
        })

        it('propaga errores por campo del backend', () => {
            const apiError = new ApiError('Error de validación', { email: 'Email ya registrado' })
            setupMockedPostError(apiError)
            return verifyRegisterThrowsApiError(validRegisterRequest, apiError)
        })
    })

    describe('login', () => {
        it('llama a POST /auth/login con los datos correctos', () => {
            setupMockedPostResponse({ accessToken: 'token-123', role: 'USER' })
            return callLogin(validLoginRequest).then(() => {
                verifyPostCalledWith('/auth/login', validLoginRequest, { skipAuthRedirect: true })
            })
        })

        it('retorna accessToken y role del backend', () => {
            setupMockedPostResponse({ accessToken: 'token-123', role: 'USER' })
            return callLoginAndVerifyResponse(validLoginRequest, 'token-123', 'USER')
        })

        it('propaga el error si las credenciales son inválidas', () => {
            setupMockedPostError(new Error('Credenciales inválidas'))
            return verifyLoginThrowsError(validLoginRequest, 'Credenciales inválidas')
        })

        it('propaga errores por campo del backend', () => {
            const apiError = new ApiError('Error de validación', { email: 'Email no registrado' })
            setupMockedPostError(apiError)
            return verifyLoginThrowsApiError(validLoginRequest, apiError)
        })
    })

    describe('backofficeLogin', () => {
        it('llama a POST /auth/backoffice/login con los datos correctos y skipAuthRedirect', () => {
            setupMockedPostResponse({ accessToken: 'token-123', role: 'ADMIN' })
            return callBackofficeLogin(validLoginRequest).then(() => {
                verifyPostCalledWith('/auth/backoffice/login', validLoginRequest, { skipAuthRedirect: true })
            })
        })

        it('retorna accessToken y role ADMIN del backend', () => {
            setupMockedPostResponse({ accessToken: 'token-123', role: 'ADMIN' })
            return callBackofficeLoginAndVerifyResponse(validLoginRequest, 'token-123', 'ADMIN')
        })

        it('propaga el error si las credenciales son inválidas', () => {
            setupMockedPostError(new Error('Invalid credentials'))
            return verifyBackofficeLoginThrowsError(validLoginRequest, 'Invalid credentials')
        })
    })

    describe('logout', () => {
        it('llama a POST /auth/logout', () => {
            setupMockedPostResponse(null)
            return callLogout().then(() => {
                verifyPostCalledWith('/auth/logout', {})
            })
        })

        it('propaga el error si falla', () => {
            setupMockedPostError(new Error('Sin conexión'))
            return verifyLogoutThrowsError('Sin conexión')
        })
    })

    describe('changePassword', () => {
        it('llama a PUT /users/me/password con la contraseña actual y la nueva', () => {
            setupMockedPutResponse(undefined)
            return callChangePassword('currentPass123', 'newPass456').then(() => {
                verifyPutCalledWith('/users/me/password', {
                    currentPassword: 'currentPass123',
                    newPassword: 'newPass456',
                })
            })
        })

        it('resuelve sin valor cuando el cambio es exitoso', () => {
            setupMockedPutResponse(undefined)
            return verifyChangePasswordResolves('currentPass123', 'newPass456')
        })

        it('propaga el error si la contraseña actual es incorrecta', () => {
            setupMockedPutError(new Error('Current password is incorrect'))
            return verifyChangePasswordThrowsError('wrongPass', 'newPass456', 'Current password is incorrect')
        })

        it('propaga el error ante fallas del servidor', () => {
            setupMockedPutError(new Error('Internal server error'))
            return verifyChangePasswordThrowsError('currentPass123', 'newPass456', 'Internal server error')
        })
    })
    

    

    

    

    const callRegister = (req: RegisterRequest) => {
        return register(req)
    }

    const callRegisterAndVerifyAccessToken = (req: RegisterRequest, expectedToken: string) => {
        return register(req).then((res) => {
            expect(res.accessToken).toBe(expectedToken)
        })
    }

    const verifyRegisterThrowsError = (req: RegisterRequest, expectedMessage: string) => {
        return expect(register(req)).rejects.toThrow(expectedMessage)
    }

    const verifyRegisterThrowsApiError = (req: RegisterRequest, expectedError: ApiError) => {
        return expect(register(req)).rejects.toThrow(expectedError)
    }

    const callLogin = (req: LoginRequest) => {
        return login(req)
    }

    const callLoginAndVerifyResponse = (req: LoginRequest, expectedToken: string, expectedRole: string) => {
        return login(req).then((res) => {
            expect(res.accessToken).toBe(expectedToken)
            expect(res.role).toBe(expectedRole)
        })
    }

    const verifyLoginThrowsError = (req: LoginRequest, expectedMessage: string) => {
        return expect(login(req)).rejects.toThrow(expectedMessage)
    }

    const verifyLoginThrowsApiError = (req: LoginRequest, expectedError: ApiError) => {
        return expect(login(req)).rejects.toThrow(expectedError)
    }

    const callBackofficeLogin = (req: LoginRequest) => {
        return backofficeLogin(req)
    }

    const callBackofficeLoginAndVerifyResponse = (req: LoginRequest, expectedToken: string, expectedRole: string) => {
        return backofficeLogin(req).then((res) => {
            expect(res.accessToken).toBe(expectedToken)
            expect(res.role).toBe(expectedRole)
        })
    }

    const verifyBackofficeLoginThrowsError = (req: LoginRequest, expectedMessage: string) => {
        return expect(backofficeLogin(req)).rejects.toThrow(expectedMessage)
    }

    const callLogout = () => {
        return logout()
    }

    const verifyLogoutThrowsError = (expectedMessage: string) => {
        return expect(logout()).rejects.toThrow(expectedMessage)
    }

    const callChangePassword = (currentPass: string, newPass: string) => {
        return changePassword(currentPass, newPass)
    }

    const verifyChangePasswordResolves = (currentPass: string, newPass: string) => {
        return expect(changePassword(currentPass, newPass)).resolves.toBeUndefined()
    }

    const verifyChangePasswordThrowsError = (currentPass: string, newPass: string, expectedMessage: string) => {
        return expect(changePassword(currentPass, newPass)).rejects.toThrow(expectedMessage)
    }

    const verifyPostCalledWith = (url: string, body: any, options?: any) => {
        if (options) {
            expect(mockedPost).toHaveBeenCalledWith(url, body, options)
        } else {
            expect(mockedPost).toHaveBeenCalledWith(url, body)
        }
    }

    const verifyPutCalledWith = (url: string, body: any) => {
        expect(mockedPut).toHaveBeenCalledWith(url, body)
    }
})