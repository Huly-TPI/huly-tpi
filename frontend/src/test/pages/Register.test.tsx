import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Register from '../../pages/Register/Register'
import { ApiError } from '../../api/apiError'
import { ThemeProvider } from '../../context/theme'
import { register } from '../../api/auth'
import { clickButton, clickCheckbox, typePlaceholder, verifyTextPresent, verifyTextNotPresent, verifyPlaceholderPresent, verifyButtonDisabled, verifyButtonEnabled, verifyTextPresentAsync, clearAllMocks, verifyValidationAlertsShown } from '../testHelpers'

// --- SIMULACIONES GLOBALES (MOCKS) ---
vi.mock('../../api/auth', () => ({
    register: vi.fn(),
}))

const mockLoginWithToken = vi.fn()
const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
    useAuth: () => mockUseAuth(),
}))

const mockedRegister = vi.mocked(register)

describe('Register', () => {
    let user: ReturnType<typeof userEvent.setup>

    beforeEach(() => {
        clearAllMocks()
        mockUseAuth.mockReturnValue({ loginWithToken: mockLoginWithToken, isAuthenticated: false, loading: false })
    })

    // --- CASOS DE PRUEBA (TEST SUITE) ---

    it('renderiza el formulario de registro', () => {
        renderRegisterForm()
        verifyRegisterFormIsVisible()
    })

    it('redirige al home si ya hay una sesión iniciada', () => {
        mockUseAuth.mockReturnValue({ loginWithToken: mockLoginWithToken, isAuthenticated: true, loading: false })
        renderRegisterForm()
        return verifyTextPresentAsync('Home')
    })

    it('no muestra el formulario mientras se está verificando la sesión', () => {
        mockUseAuth.mockReturnValue({ loginWithToken: mockLoginWithToken, isAuthenticated: false, loading: true })
        renderRegisterForm()
        verifyTextNotPresent('¡Creá tu cuenta!')
    })

    it('muestra errores de validación al enviar vacío', () => {
        renderRegisterForm()
        return clickTermsCheckbox()
            .then(() => submitRegisterForm())
            .then(() => {
                verifyValidationAlertsShown()
            })
    })

    it('registra exitosamente y redirige al home', () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: true })
        renderRegisterForm()
        return fillValidRegisterForm('mili', 'mili@mail.com')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyTextPresentAsync('Home')
            })
    })

    it('pasa el token de register a loginWithToken', () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: true })
        renderRegisterForm()
        return fillValidRegisterForm('mili', 'mili@mail.com')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyLoginWithTokenCalledWith('token-123')
            })
    })

    it('muestra error del backend cuando falla el registro', () => {
        mockedRegister.mockRejectedValueOnce(new Error('El email ya está registrado'))
        renderRegisterForm()
        return fillValidRegisterForm('mili', 'mili@mail.com')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyTextPresentAsync('El email ya está registrado')
            })
    })

    it('no llama a loginWithToken cuando falla el registro', () => {
        mockedRegister.mockRejectedValueOnce(new Error('Error'))
        renderRegisterForm()
        return fillValidRegisterForm('mili', 'mili@mail.com')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyTextPresentAsync('Error')
            })
            .then(() => {
                verifyLoginWithTokenWasNotCalled()
            })
    })

    it('navega a login al hacer click en Iniciá sesión', () => {
        renderRegisterForm()
        return clickNavigationButton('Iniciá sesión').then(() => {
            verifyTextPresent('Login')
        })
    })

    it('deshabilita el botón si no acepta términos', () => {
        renderRegisterForm()
        verifyButtonDisabled('Crear cuenta')
    })

    it('habilita el botón al aceptar términos', () => {
        renderRegisterForm()
        return clickTermsCheckbox().then(() => {
            verifyButtonEnabled('Crear cuenta')
        })
    })

    it('muestra error de edad mínima con fecha reciente', () => {
        renderRegisterForm()
        return fillValidRegisterForm('mili', 'mili@mail.com')
            .then(() => fillBirthDate('2020-01-01'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                verifyTextPresent('Debés tener al menos 13 años')
                verifyRegisterWasNotCalled()
            })
    })

    it('muestra errores por campo del backend', () => {
        mockedRegister.mockRejectedValueOnce(
            new ApiError('Error de validación', { birthDate: 'Edad mínima inválida' }),
        )
        renderRegisterForm()
        return fillValidRegisterForm('mili', 'mili@mail.com')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyTextPresentAsync('Edad mínima inválida')
            })
    })

    it('bloquea nombre con contenido XSS', () => {
        renderRegisterForm()
        return fillNameInput('<script>alert(1)</script>')
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                verifyTextPresent('El texto contiene caracteres no permitidos')
                verifyRegisterWasNotCalled()
            })
    })

    it('bloquea nombre con SQL injection', () => {
        renderRegisterForm()
        return fillNameInput("' OR 1=1 --")
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                verifyTextPresent('El texto contiene caracteres no permitidos')
                verifyRegisterWasNotCalled()
            })
    })

    it('bloquea nombre que supera el máximo de caracteres', () => {
        renderRegisterForm()
        return fillNameInput('a'.repeat(51))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                verifyTextPresent('Máximo 50 caracteres')
                verifyRegisterWasNotCalled()
            })
    })

    it('envía los valores con trim al backend', () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: true })
        renderRegisterForm()
        return fillValidRegisterForm('  Mili  ', '  mili@mail.com  ')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyRegisterCalledWithTrimmed('Mili', 'mili@mail.com')
            })
    })

    it('bloquea nombre compuesto solo por un punto', () => {
        renderRegisterForm()
        return fillNameInput('.')
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                verifyInvalidNameErrorIsShown()
                verifyRegisterWasNotCalled()
            })
    })

    it('bloquea nombre con números', () => {
        renderRegisterForm()
        return fillNameInput('Mili123')
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                verifyInvalidNameErrorIsShown()
                verifyRegisterWasNotCalled()
            })
    })

    it('bloquea nombre con menos de 3 letras', () => {
        renderRegisterForm()
        return fillNameInput('Lu')
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                verifyInvalidNameErrorIsShown()
                verifyRegisterWasNotCalled()
            })
    })

    it('permite nombre con varias palabras si solo contiene letras', () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Ana Maria', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: true })
        renderRegisterForm()
        return fillValidRegisterForm('Ana Maria', 'mili@mail.com')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyRegisterCalledWithName('Ana Maria')
            })
    })

    it('redirige al /onboarding cuando onBoardingCompleted es false', () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER', onBoardingCompleted: false })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: false })
        renderRegisterForm()
        return fillValidRegisterForm('mili', 'mili@mail.com')
            .then(() => fillBirthDate('2000-01-15'))
            .then(() => clickTermsCheckbox())
            .then(() => submitRegisterForm())
            .then(() => {
                return verifyTextPresentAsync('Onboarding')
            })
    })
    const renderRegisterForm = () => {
        user = userEvent.setup()
        render(
            <ThemeProvider>
                <MemoryRouter initialEntries={['/register']}>
                    <Routes>
                        <Route path="/register" element={<Register />} />
                        <Route path="/" element={<h1>Home</h1>} />
                        <Route path="/login" element={<h1>Login</h1>} />
                        <Route path="/onboarding" element={<h1>Onboarding</h1>} />
                    </Routes>
                </MemoryRouter>
            </ThemeProvider>,
        )
    }

    const fillValidRegisterForm = async (name: string, email: string) => {
        await typePlaceholder(user, 'Nombre', name)
        await typePlaceholder(user, 'Email', email)
        const passwordFields = screen.getAllByPlaceholderText(/contraseña/i)
        await user.type(passwordFields[0], '123456')
        await user.type(passwordFields[1], '123456')
    }

    const fillBirthDate = async (date: string) => {
        const dateInput = screen.getByPlaceholderText('Fecha de nacimiento')
        await user.click(dateInput)
        await user.type(dateInput, date)
    }

    const fillNameInput = async (name: string) => {
        await typePlaceholder(user, 'Nombre', name)
    }

    const clickTermsCheckbox = async () => {
        await clickCheckbox(user)
    }

    const submitRegisterForm = async () => {
        await clickButton(user, 'Crear cuenta')
    }

    const clickNavigationButton = async (name: string) => {
        await clickButton(user, name)
    }

    const verifyRegisterFormIsVisible = () => {
        verifyTextPresent('¡Creá tu cuenta!')
        verifyPlaceholderPresent('Nombre')
        verifyPlaceholderPresent('Email')
    }

    

    const verifyLoginWithTokenWasNotCalled = () => {
        expect(mockLoginWithToken).not.toHaveBeenCalled()
    }

    
    
    const verifyRegisterWasNotCalled = () => {
        expect(mockedRegister).not.toHaveBeenCalled()
    }

    const verifyInvalidNameErrorIsShown = () => {
        verifyTextPresent('El nombre debe tener al menos 3 letras. Solo puede contener letras y espacios')
    }

    
    
    const verifyLoginWithTokenCalledWith = async (token: string) => {
        await waitFor(() => {
            expect(mockLoginWithToken).toHaveBeenCalledWith(token)
        })
    }

    const verifyRegisterCalledWithTrimmed = async (name: string, email: string) => {
        await waitFor(() => {
            expect(mockedRegister).toHaveBeenCalledWith(
                expect.objectContaining({ name, email })
            )
        })
    }

    const verifyRegisterCalledWithName = async (name: string) => {
        await waitFor(() => {
            expect(mockedRegister).toHaveBeenCalledWith(
                expect.objectContaining({ name })
            )
        })
    }
})
