import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Login from '../../pages/Login/Login'
import { ApiError } from '../../api/apiError'
import { ThemeProvider } from '../../context/theme'
import { forgotPassword, resetPassword } from '../../api/auth'
import { clickButton, typePlaceholder, verifyTextPresent, verifyPlaceholderPresent, verifyButtonDisabled, verifyButtonEnabled, verifyButtonPresent, verifyTextPresentAsync, verifyHeadingPresentAsync, clearAllMocks, verifyValidationAlertsShown } from '../testHelpers'

// --- SIMULACIONES GLOBALES (MOCKS) ---
const mockLogin = vi.fn()
vi.mock('../../context/auth', () => ({
    useAuth: () => ({ login: mockLogin }),
}))

vi.mock('../../api/auth', () => ({
    forgotPassword: vi.fn(),
    resetPassword: vi.fn(),
}))

const mockForgotPassword = vi.mocked(forgotPassword)
const mockResetPassword = vi.mocked(resetPassword)

describe('Login', () => {
    let user: ReturnType<typeof userEvent.setup>

    beforeEach(() => {
        clearAllMocks()
        mockForgotPassword.mockResolvedValue(undefined)
        mockResetPassword.mockResolvedValue(undefined)
    })

    // --- CASOS DE PRUEBA (TEST SUITE) ---

    it('renderiza el formulario de login', () => {
        renderLoginForm()
        verifyLoginFormIsVisible()
    })

    it('muestra errores de validación al enviar vacío', () => {
        renderLoginForm()
        return submitLoginForm('', '').then(() => {
            verifyValidationAlertsShown()
            verifyLoginWasNotCalled()
        })
    })

    it('muestra error con email inválido', () => {
        renderLoginForm()
        return submitLoginForm('noesmail', '').then(() => {
            verifyTextPresent('Email inválido')
            verifyLoginWasNotCalled()
        })
    })

    it('llama a login del contexto con las credenciales', () => {
        mockLogin.mockResolvedValueOnce({ onBoardingCompleted: true })
        renderLoginForm()
        return submitLoginForm('mili@mail.com', '123456').then(() => {
            return verifyLoginCalledWith('mili@mail.com', '123456')
        })
    })

    it('inicia sesión exitosamente y redirige al home', () => {
        mockLogin.mockResolvedValueOnce({ onBoardingCompleted: true })
        renderLoginForm()
        return submitLoginForm('mili@mail.com', '123456').then(() => {
            return verifyTextPresentAsync('Home')
        })
    })

    it('redirige a onboarding cuando el perfil emocional no está completo', () => {
        mockLogin.mockResolvedValueOnce({ onBoardingCompleted: false })
        renderLoginForm()
        return submitLoginForm('mili@mail.com', '123456').then(() => {
            return verifyTextPresentAsync('Onboarding')
        })
    })

    it('muestra mensaje amigable para credenciales inválidas', () => {
        mockLogin.mockRejectedValueOnce(new Error('Invalid credentials'))
        renderLoginForm()
        return submitLoginForm('mili@mail.com', '123456').then(() => {
            return verifyTextPresentAsync('Email o contraseña incorrectos')
        })
    })

    it('muestra error genérico del backend', () => {
        mockLogin.mockRejectedValueOnce(new Error('Error del servidor'))
        renderLoginForm()
        return submitLoginForm('mili@mail.com', '123456').then(() => {
            return verifyTextPresentAsync('Error del servidor')
        })
    })

    it('muestra errores por campo del backend', () => {
        mockLogin.mockRejectedValueOnce(
            new ApiError('Error de validación', { email: 'Email no registrado' }),
        )
        renderLoginForm()
        return submitLoginForm('mili@mail.com', '123456').then(() => {
            return verifyTextPresentAsync('Email no registrado')
        })
    })

    it('navega a registro al hacer click en Registrate', () => {
        renderLoginForm()
        return clickNavigationButton('Registrate').then(() => {
            verifyTextPresent('Register')
        })
    })

    it('deshabilita el botón mientras carga', () => {
        mockLogin.mockImplementation(() => new Promise(() => {}))
        renderLoginForm()
        return submitLoginForm('mili@mail.com', '123456').then(() => {
            verifyButtonDisabled('Ingresando...')
        })
    })

    it('muestra el link de olvidaste tu contraseña en el paso login', () => {
        renderLoginForm()
        verifyButtonPresent('¿Olvidaste tu contraseña?')
    })

    describe('paso forgot password', () => {
        it('muestra el formulario de recupero al clickear olvidaste tu contraseña', () => {
            renderLoginForm()
            return clickNavigationButton('¿Olvidaste tu contraseña?').then(() => {
                verifyTextPresent('Recuperar contraseña')
                verifyButtonPresent('Enviar código')
            })
        })

        it('muestra error de validación si el email está vacío', () => {
            renderLoginForm()
            return clickNavigationButton('¿Olvidaste tu contraseña?')
                .then(() => submitForgotPasswordForm(''))
                .then(() => {
                    verifyValidationAlertsShown()
                    verifyForgotPasswordWasNotCalled()
                })
        })

        it('llama a forgotPassword y pasa al paso reset', () => {
            renderLoginForm()
            return clickNavigationButton('¿Olvidaste tu contraseña?')
                .then(() => submitForgotPasswordForm('user@huly.com'))
                .then(() => {
                    return verifyHeadingPresentAsync('Nueva contraseña')
                })
        })

        it('muestra error si el email no existe', () => {
            mockForgotPassword.mockRejectedValueOnce(new Error('No existe una cuenta con ese email'))
            renderLoginForm()
            return clickNavigationButton('¿Olvidaste tu contraseña?')
                .then(() => submitForgotPasswordForm('missing@huly.com'))
                .then(() => {
                    return verifyTextPresentAsync('No existe una cuenta con ese email')
                })
        })

        it('vuelve al login al clickear Volver', () => {
            renderLoginForm()
            return clickNavigationButton('¿Olvidaste tu contraseña?')
                .then(() => clickNavigationButton('Volver'))
                .then(() => {
                    verifyTextPresent('¡Bienvenido!')
                })
        })
    })

    describe('paso reset password', () => {
        it('muestra el formulario de nueva contraseña', () => {
            renderLoginForm()
            return enterResetPasswordStep().then(() => {
                verifyResetPasswordFormIsVisible()
            })
        })

        it('llama a resetPassword con el token y la nueva contraseña', () => {
            renderLoginForm()
            return enterResetPasswordStep()
                .then(() => submitNewPasswordForm('uuid-token-123', 'newPass123'))
                .then(() => {
                    return verifyResetPasswordWasCalled('uuid-token-123', 'newPass123')
                })
        })

        it('vuelve al login con mensaje de éxito al restablecer la contraseña', () => {
            renderLoginForm()
            return enterResetPasswordStep()
                .then(() => submitNewPasswordForm('uuid-token-123', 'newPass123'))
                .then(() => {
                    return verifyTextPresentAsync('¡Contraseña actualizada! Podés iniciar sesión.')
                })
        })

        it('muestra error si el token es inválido o expiró', () => {
            mockResetPassword.mockRejectedValueOnce(new Error('Token inválido o expirado'))
            renderLoginForm()
            return enterResetPasswordStep()
                .then(() => submitNewPasswordForm('bad-token', 'newPass123'))
                .then(() => {
                    return verifyTextPresentAsync('El código es inválido o ya expiró')
                })
        })

        it('navega al paso forgot al clickear Reenviar', () => {
            renderLoginForm()
            return enterResetPasswordStep()
                .then(() => clickNavigationButton('Reenviar'))
                .then(() => {
                    verifyTextPresent('Recuperar contraseña')
                })
        })

        it('el botón de restablecer está habilitado al entrar al paso reset', () => {
            renderLoginForm()
            return enterResetPasswordStep().then(() => {
                verifyButtonEnabled('Restablecer contraseña')
            })
        })

        it('muestra errores de validación al enviar el formulario vacío', () => {
            renderLoginForm()
            return enterResetPasswordStep()
                .then(() => submitNewPasswordForm('', ''))
                .then(() => {
                    verifyValidationAlertsShown()
                    verifyResetPasswordWasNotCalled()
                })
        })
    })
    const renderLoginForm = () => {
        user = userEvent.setup()
        render(
            <ThemeProvider>
                <MemoryRouter initialEntries={['/login']}>
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route path="/" element={<h1>Home</h1>} />
                        <Route path="/onboarding" element={<h1>Onboarding</h1>} />
                        <Route path="/register" element={<h1>Register</h1>} />
                    </Routes>
                </MemoryRouter>
            </ThemeProvider>,
        )
    }

    const submitLoginForm = async (email: string, pass: string) => {
        if (email) await typePlaceholder(user, 'Email', email)
        if (pass) await typePlaceholder(user, 'Contraseña', pass)
        await clickButton(user, 'Iniciar sesión')
    }

    const clickNavigationButton = async (name: string) => {
        await clickButton(user, name)
    }

    const submitForgotPasswordForm = async (email: string) => {
        if (email) await typePlaceholder(user, 'Email', email)
        await clickButton(user, 'Enviar código')
    }

    const submitNewPasswordForm = async (code: string, pass: string) => {
        if (code) await typePlaceholder(user, 'Código recibido por email', code)
        if (pass) await typePlaceholder(user, 'Nueva contraseña', pass)
        await clickButton(user, 'Restablecer contraseña')
    }

    const enterResetPasswordStep = async () => {
        await clickButton(user, '¿Olvidaste tu contraseña?')
        await typePlaceholder(user, 'Email', 'user@huly.com')
        await clickButton(user, 'Enviar código')
        await screen.findByRole('heading', { name: 'Nueva contraseña' })
    }

    const verifyLoginFormIsVisible = () => {
        verifyTextPresent('¡Bienvenido!')
        verifyPlaceholderPresent('Email')
        verifyPlaceholderPresent('Contraseña')
    }

    const verifyResetPasswordFormIsVisible = () => {
        verifyPlaceholderPresent('Código recibido por email')
        verifyPlaceholderPresent('Nueva contraseña')
    }

    

    const verifyLoginWasNotCalled = () => {
        expect(mockLogin).not.toHaveBeenCalled()
    }

    const verifyForgotPasswordWasNotCalled = () => {
        expect(mockForgotPassword).not.toHaveBeenCalled()
    }

    const verifyResetPasswordWasNotCalled = () => {
        expect(mockResetPassword).not.toHaveBeenCalled()
    }

    
    
    
    
    
    
    const verifyLoginCalledWith = async (email: string, pass: string) => {
        await waitFor(() => {
            expect(mockLogin).toHaveBeenCalledWith({ email, password: pass })
        })
    }

    const verifyResetPasswordWasCalled = async (code: string, pass: string) => {
        await waitFor(() => {
            expect(mockResetPassword).toHaveBeenCalledWith(code, pass)
        })
    }
})
