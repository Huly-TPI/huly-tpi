import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import BackofficeLogin from '../../pages/Backoffice/BackofficeLogin'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/auth', () => ({
    backofficeLogin: vi.fn(),
}))

const mockLoginWithToken = vi.fn().mockResolvedValue(undefined)
vi.mock('../../context/auth', () => ({
    useAuth: () => {
        const hasAdminRole = window.localStorage.getItem('role') === 'ADMIN'
        return {
            loginWithToken: mockLoginWithToken,
            isAuthenticated: hasAdminRole,
            loading: false,
            user: hasAdminRole ? { role: 'ADMIN' } : null,
        }
    },
}))

const mockSetToken = vi.fn()
vi.mock('../../api/client', () => ({
    setToken: (t: string) => mockSetToken(t),
    getToken: vi.fn(),
    clearToken: vi.fn(),
    tryRehydrateSession: vi.fn(),
    api: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        patch: vi.fn(),
        delete: vi.fn(),
    },
}))

vi.mock('../../assets/brand/color-logo.webp', () => ({ default: 'color-logo.webp' }))

import { backofficeLogin } from '../../api/auth'
import { clickButton, typePlaceholder, verifyTextPresent, verifyPlaceholderPresent, verifyButtonDisabled, clearAllMocks } from '../testHelpers'

const mockedBackofficeLogin = vi.mocked(backofficeLogin)

describe('BackofficeLogin', () => {
    let user: ReturnType<typeof userEvent.setup>

    beforeEach(() => {
        clearAllMocks()
        window.localStorage.clear()
    })

    it('renderiza el formulario de login', () => {
        setupRouter()
        verifyLoginFormFields()
    })

    it('muestra error de validación si el email está vacío', () => {
        setupRouter()
        return clickLoginButton().then(() => {
            verifyRequiredFieldErrorShown()
            verifyBackofficeLoginNotCalled()
        })
    })

    it('muestra error de validación si la contraseña está vacía', () => {
        setupRouter()
        return fillEmailAndClickLogin('admin@huly.com').then(() => {
            verifyRequiredFieldErrorShown()
            verifyBackofficeLoginNotCalled()
        })
    })

    it('muestra error de validación si el email tiene formato inválido', () => {
        setupRouter()
        return fillEmailAndClickLogin('noesmail').then(() => {
            verifyInvalidEmailErrorShown()
            verifyBackofficeLoginNotCalled()
        })
    })

    it('inicia sesión exitosamente y redirige al backoffice', () => {
        setupLoginResponseSuccess('token-123', 'ADMIN')
        setupRouter()
        return fillFormAndClickLogin('admin@huly.com', 'password123').then(() => {
            return waitForBackofficeRedirect()
        })
    })

    it('guarda el token en memoria y el role en localStorage al iniciar sesión', () => {
        setupLoginResponseSuccess('token-123', 'ADMIN')
        setupRouter()
        return fillFormAndClickLogin('admin@huly.com', 'password123').then(() => {
            return waitForStorageAndTokenVerification('token-123', 'ADMIN')
        })
    })

    it('muestra mensaje amigable para credenciales inválidas', () => {
        setupLoginResponseError(new Error('Invalid credentials'))
        setupRouter()
        return fillFormAndClickLogin('admin@huly.com', 'password123').then(() => {
            return waitForTextToAppear('Credenciales incorrectas')
        })
    })

    it('muestra error genérico del backend', () => {
        setupLoginResponseError(new ApiError('Error del servidor', {}))
        setupRouter()
        return fillFormAndClickLogin('admin@huly.com', 'password123').then(() => {
            return waitForTextToAppear('Error del servidor')
        })
    })

    it('deshabilita el botón mientras carga', () => {
        setupLoginResponsePending()
        setupRouter()
        return fillFormAndClickLogin('admin@huly.com', 'password123').then(() => {
            verifyButtonIsDisabledWithText('Ingresando...')
        })
    })

    it('redirige al backoffice si ya tiene el rol ADMIN en localStorage', () => {
        setupLocalStorageRole('ADMIN')
        renderRouterWithRoleAdmin()
        verifyRedirectedToBackofficeDirectly()
    })

    it('alterna la visibilidad de la contraseña al hacer clic en el ojo', () => {
        setupRouter()
        verifyPasswordInputType('password')
        return togglePasswordVisibility().then(() => {
            verifyPasswordInputType('text')
            verifyHidePasswordIconVisible()
            return togglePasswordVisibility().then(() => {
                verifyPasswordInputType('password')
            })
        })
    })
    const setupRouter = () => {
        user = userEvent.setup()
        render(
            <MemoryRouter initialEntries={['/backoffice/login']}>
                <Routes>
                    <Route path="/backoffice/login" element={<BackofficeLogin />} />
                    <Route path="/backoffice" element={<h1>Backoffice</h1>} />
                </Routes>
            </MemoryRouter>,
        )
    }

    const renderRouterWithRoleAdmin = () => {
        render(
            <MemoryRouter initialEntries={['/backoffice/login']}>
                <Routes>
                    <Route path="/backoffice/login" element={<BackofficeLogin />} />
                    <Route path="/backoffice" element={<h1>Backoffice</h1>} />
                </Routes>
            </MemoryRouter>
        )
    }

    const setupLocalStorageRole = (role: string) => {
        localStorage.setItem('role', role)
    }

    const setupLoginResponseSuccess = (accessToken: string, role: string) => {
        mockedBackofficeLogin.mockResolvedValueOnce({ accessToken, role })
    }

    const setupLoginResponseError = (error: Error) => {
        mockedBackofficeLogin.mockRejectedValueOnce(error)
    }

    const setupLoginResponsePending = () => {
        mockedBackofficeLogin.mockImplementation(() => new Promise(() => {}))
    }

    const fillEmail = (email: string) => {
        return typePlaceholder(user, 'Correo electrónico', email)
    }

    const clickLoginButton = () => {
        return clickButton(user, 'Iniciar sesión')
    }

    const fillEmailAndClickLogin = (email: string) => {
        return fillEmail(email).then(() => clickLoginButton())
    }

    const fillFormAndClickLogin = (email: string, password: string) => {
        return typePlaceholder(user, 'Correo electrónico', email).then(() => {
            return typePlaceholder(user, '••••••••', password).then(() => {
                return clickLoginButton()
            })
        })
    }

    const verifyLoginFormFields = () => {
        verifyPlaceholderPresent('Correo electrónico')
        verifyPlaceholderPresent('••••••••')
        expect(screen.getByRole('button', { name: 'Iniciar sesión' })).toBeInTheDocument()
    }

    const verifyRequiredFieldErrorShown = () => {
        expect(screen.getAllByText('Campo requerido').length).toBeGreaterThan(0)
    }

    const verifyInvalidEmailErrorShown = () => {
        verifyTextPresent('Email inválido')
    }

    const verifyBackofficeLoginNotCalled = () => {
        expect(mockedBackofficeLogin).not.toHaveBeenCalled()
    }

    const waitForBackofficeRedirect = () => {
        return waitFor(() => {
            verifyTextPresent('Backoffice')
        })
    }

    const waitForStorageAndTokenVerification = (token: string, role: string) => {
        return waitFor(() => {
            expect(mockLoginWithToken).toHaveBeenCalledWith(token)
            expect(localStorage.getItem('role')).toBe(role)
        })
    }

    const waitForTextToAppear = (text: string) => {
        return waitFor(() => {
            verifyTextPresent(text)
        })
    }

    const verifyButtonIsDisabledWithText = (text: string) => {
        verifyButtonDisabled(text)
    }

    const verifyRedirectedToBackofficeDirectly = () => {
        verifyTextPresent('Backoffice')
        expect(screen.queryByPlaceholderText('Correo electrónico')).not.toBeInTheDocument()
    }

    const verifyPasswordInputType = (type: string) => {
        const passwordInput = screen.getByPlaceholderText('••••••••') as HTMLInputElement
        expect(passwordInput.type).toBe(type)
    }

    const togglePasswordVisibility = () => {
        const toggleButton = screen.queryByLabelText('Mostrar contraseña') || screen.queryByLabelText('Ocultar contraseña')
        return user.click(toggleButton!)
    }

    const verifyHidePasswordIconVisible = () => {
        expect(screen.getByLabelText('Ocultar contraseña')).toBeInTheDocument()
    }
})
