import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Login from '../../pages/Login/Login'
import { ApiError } from '../../api/apiError'
import { ThemeProvider } from '../../context/theme'
import { forgotPassword, resetPassword } from '../../api/auth'

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
    beforeEach(() => {
        vi.clearAllMocks()
        mockForgotPassword.mockResolvedValue(undefined)
        mockResetPassword.mockResolvedValue(undefined)
    })

    const getSubmitButton = () => screen.getByRole('button', { name: 'Iniciar sesión' })

    const renderWithRouter = () => {
        const user = userEvent.setup()
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
        return { user }
    }

    const fillForm = async (user: ReturnType<typeof userEvent.setup>) => {
        await user.type(screen.getByPlaceholderText('Email'), 'mili@mail.com')
        await user.type(screen.getByPlaceholderText('Contraseña'), '123456')
    }

    it('renderiza el formulario de login', () => {
        renderWithRouter()

        expect(screen.getByText('¡Bienvenido!')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('Email')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('Contraseña')).toBeInTheDocument()
    })

    it('muestra errores de validación al enviar vacío', async () => {
        const { user } = renderWithRouter()

        await user.click(getSubmitButton())

        expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
        expect(mockLogin).not.toHaveBeenCalled()
    })

    it('muestra error con email inválido', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Email'), 'noesmail')
        await user.click(getSubmitButton())

        expect(screen.getByText('Email inválido')).toBeInTheDocument()
        expect(mockLogin).not.toHaveBeenCalled()
    })

    it('llama a login del contexto con las credenciales', async () => {
        mockLogin.mockResolvedValueOnce({ onBoardingCompleted: true })
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(mockLogin).toHaveBeenCalledWith({
                email: 'mili@mail.com',
                password: '123456',
            })
        })
    })

    it('inicia sesión exitosamente y redirige al home', async () => {
        mockLogin.mockResolvedValueOnce({ onBoardingCompleted: true })
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Home')).toBeInTheDocument()
        })
    })

    it('redirige a onboarding cuando el perfil emocional no está completo', async () => {
        mockLogin.mockResolvedValueOnce({ onBoardingCompleted: false })
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Onboarding')).toBeInTheDocument()
        })
    })

    it('muestra mensaje amigable para credenciales inválidas', async () => {
        mockLogin.mockRejectedValueOnce(new Error('Invalid credentials'))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Email o contraseña incorrectos')).toBeInTheDocument()
        })
    })

    it('muestra error genérico del backend', async () => {
        mockLogin.mockRejectedValueOnce(new Error('Error del servidor'))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Error del servidor')).toBeInTheDocument()
        })
    })

    it('muestra errores por campo del backend', async () => {
        mockLogin.mockRejectedValueOnce(
            new ApiError('Error de validación', { email: 'Email no registrado' }),
        )
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Email no registrado')).toBeInTheDocument()
        })
    })

    it('navega a registro al hacer click en Registrate', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('button', { name: 'Registrate' }))

        expect(screen.getByText('Register')).toBeInTheDocument()
    })

    it('deshabilita el botón mientras carga', async () => {
        mockLogin.mockImplementation(() => new Promise(() => {}))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(getSubmitButton())

        expect(screen.getByRole('button', { name: 'Ingresando...' })).toBeDisabled()
    })

    it('muestra el link de olvidaste tu contraseña en el paso login', () => {
        renderWithRouter()
        expect(screen.getByRole('button', { name: '¿Olvidaste tu contraseña?' })).toBeInTheDocument()
    })

    describe('paso forgot password', () => {
        const goToForgot = async (user: ReturnType<typeof userEvent.setup>) => {
            await user.click(screen.getByRole('button', { name: '¿Olvidaste tu contraseña?' }))
        }

        it('muestra el formulario de recupero al clickear olvidaste tu contraseña', async () => {
            const { user } = renderWithRouter()
            await goToForgot(user)
            expect(screen.getByText('Recuperar contraseña')).toBeInTheDocument()
            expect(screen.getByRole('button', { name: 'Enviar código' })).toBeInTheDocument()
        })

        it('muestra error de validación si el email está vacío', async () => {
            const { user } = renderWithRouter()
            await goToForgot(user)
            await user.click(screen.getByRole('button', { name: 'Enviar código' }))
            expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
            expect(mockForgotPassword).not.toHaveBeenCalled()
        })

        it('llama a forgotPassword con el email ingresado', async () => {
            const { user } = renderWithRouter()
            await goToForgot(user)
            await user.type(screen.getByPlaceholderText('Email'), 'user@huly.com')
            await user.click(screen.getByRole('button', { name: 'Enviar código' }))
            await waitFor(() => {
                expect(mockForgotPassword).toHaveBeenCalledWith('user@huly.com')
            })
        })

        it('avanza al paso reset al enviar el email correctamente', async () => {
            const { user } = renderWithRouter()
            await goToForgot(user)
            await user.type(screen.getByPlaceholderText('Email'), 'user@huly.com')
            await user.click(screen.getByRole('button', { name: 'Enviar código' }))
            await waitFor(() => {
                expect(screen.getByText('Nueva contraseña')).toBeInTheDocument()
            })
        })

        it('muestra error si el email no existe', async () => {
            mockForgotPassword.mockRejectedValueOnce(new Error('No existe una cuenta con ese email'))
            const { user } = renderWithRouter()
            await goToForgot(user)
            await user.type(screen.getByPlaceholderText('Email'), 'missing@huly.com')
            await user.click(screen.getByRole('button', { name: 'Enviar código' }))
            await waitFor(() => {
                expect(screen.getByText('No existe una cuenta con ese email')).toBeInTheDocument()
            })
        })

        it('vuelve al login al clickear Volver', async () => {
            const { user } = renderWithRouter()
            await goToForgot(user)
            await user.click(screen.getByRole('button', { name: 'Volver' }))
            expect(screen.getByText('¡Bienvenido!')).toBeInTheDocument()
        })
    })

    describe('paso reset password', () => {
        const goToReset = async (user: ReturnType<typeof userEvent.setup>) => {
            await user.click(screen.getByRole('button', { name: '¿Olvidaste tu contraseña?' }))
            await user.type(screen.getByPlaceholderText('Email'), 'user@huly.com')
            await user.click(screen.getByRole('button', { name: 'Enviar código' }))
            await screen.findByText('Nueva contraseña')
        }

        it('muestra el formulario de nueva contraseña', async () => {
            const { user } = renderWithRouter()
            await goToReset(user)
            expect(screen.getByPlaceholderText('Código recibido por email')).toBeInTheDocument()
            expect(screen.getByPlaceholderText('Nueva contraseña')).toBeInTheDocument()
        })

        it('llama a resetPassword con el token y la nueva contraseña', async () => {
            const { user } = renderWithRouter()
            await goToReset(user)
            await user.type(screen.getByPlaceholderText('Código recibido por email'), 'uuid-token-123')
            await user.type(screen.getByPlaceholderText('Nueva contraseña'), 'newPass123')
            await user.click(screen.getByRole('button', { name: 'Restablecer contraseña' }))
            await waitFor(() => {
                expect(mockResetPassword).toHaveBeenCalledWith('uuid-token-123', 'newPass123')
            })
        })

        it('vuelve al login con mensaje de éxito al restablecer la contraseña', async () => {
            const { user } = renderWithRouter()
            await goToReset(user)
            await user.type(screen.getByPlaceholderText('Código recibido por email'), 'uuid-token-123')
            await user.type(screen.getByPlaceholderText('Nueva contraseña'), 'newPass123')
            await user.click(screen.getByRole('button', { name: 'Restablecer contraseña' }))
            await waitFor(() => {
                expect(screen.getByText('¡Contraseña actualizada! Podés iniciar sesión.')).toBeInTheDocument()
            })
        })

        it('muestra error si el token es inválido o expiró', async () => {
            mockResetPassword.mockRejectedValueOnce(new Error('Token inválido o expirado'))
            const { user } = renderWithRouter()
            await goToReset(user)
            await user.type(screen.getByPlaceholderText('Código recibido por email'), 'bad-token')
            await user.type(screen.getByPlaceholderText('Nueva contraseña'), 'newPass123')
            await user.click(screen.getByRole('button', { name: 'Restablecer contraseña' }))
            await waitFor(() => {
                expect(screen.getByText('El código es inválido o ya expiró')).toBeInTheDocument()
            })
        })

        it('navega al paso forgot al clickear Reenviar', async () => {
            const { user } = renderWithRouter()
            await goToReset(user)
            await user.click(screen.getByRole('button', { name: 'Reenviar' }))
            expect(screen.getByText('Recuperar contraseña')).toBeInTheDocument()
        })
    })
})
