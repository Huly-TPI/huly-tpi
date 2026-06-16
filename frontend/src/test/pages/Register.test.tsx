import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Register from '../../pages/Register/Register'
import { ApiError } from '../../api/apiError'
import { ThemeProvider } from '../../context/theme'

vi.mock('../../api/auth', () => ({
    register: vi.fn(),
}))

const mockLoginWithToken = vi.fn()
vi.mock('../../context/auth', () => ({
    useAuth: () => ({ loginWithToken: mockLoginWithToken }),
}))

import { register } from '../../api/auth'

const mockedRegister = vi.mocked(register)

describe('Register', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    const getSubmitButton = () => screen.getByRole('button', { name: 'Crear cuenta' })

    const renderWithRouter = () => {
        const user = userEvent.setup()
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
        return { user }
    }

    const fillForm = async (user: ReturnType<typeof userEvent.setup>) => {
        await user.type(screen.getByPlaceholderText('Nombre'), 'mili')
        await user.type(screen.getByPlaceholderText('Email'), 'mili@mail.com')

        const passwordFields = screen.getAllByPlaceholderText(/contraseña/i)
        await user.type(passwordFields[0], '123456')
        await user.type(passwordFields[1], '123456')
    }

    const fillDate = async (
        user: ReturnType<typeof userEvent.setup>,
        date: string,
    ) => {
        const dateInput = screen.getByPlaceholderText('Fecha de nacimiento')
        await user.click(dateInput)
        await user.type(dateInput, date)
    }

    it('renderiza el formulario de registro', () => {
        renderWithRouter()

        expect(screen.getByText('¡Creá tu cuenta!')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('Nombre')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('Email')).toBeInTheDocument()
    })

    it('muestra errores de validación al enviar vacío', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
    })

    it('registra exitosamente y redirige al home', async () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: true })
        const { user } = renderWithRouter()

        await fillForm(user)
        await fillDate(user, '2000-01-15')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Home')).toBeInTheDocument()
        })
    })

    it('pasa el token de register a loginWithToken', async () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: true })
        const { user } = renderWithRouter()

        await fillForm(user)
        await fillDate(user, '2000-01-15')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(mockLoginWithToken).toHaveBeenCalledWith('token-123')
        })
    })

    it('muestra error del backend cuando falla el registro', async () => {
        mockedRegister.mockRejectedValueOnce(new Error('El email ya está registrado'))
        const { user } = renderWithRouter()

        await fillForm(user)
        await fillDate(user, '2000-01-15')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('El email ya está registrado')).toBeInTheDocument()
        })
    })

    it('no llama a loginWithToken cuando falla el registro', async () => {
        mockedRegister.mockRejectedValueOnce(new Error('Error'))
        const { user } = renderWithRouter()

        await fillForm(user)
        await fillDate(user, '2000-01-15')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Error')).toBeInTheDocument()
        })
        expect(mockLoginWithToken).not.toHaveBeenCalled()
    })

    it('navega a login al hacer click en Iniciá sesión', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('button', { name: 'Iniciá sesión' }))

        expect(screen.getByText('Login')).toBeInTheDocument()
    })

    it('deshabilita el botón si no acepta términos', () => {
        renderWithRouter()

        expect(getSubmitButton()).toBeDisabled()
    })

    it('habilita el botón al aceptar términos', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('checkbox'))

        expect(getSubmitButton()).toBeEnabled()
    })

    it('muestra error de edad mínima con fecha reciente', async () => {
        const { user } = renderWithRouter()

        await fillForm(user)
        await fillDate(user, '2020-01-01')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        expect(screen.getByText('Debés tener al menos 13 años')).toBeInTheDocument()
        expect(mockedRegister).not.toHaveBeenCalled()
    })

    it('muestra errores por campo del backend', async () => {
        mockedRegister.mockRejectedValueOnce(
            new ApiError('Error de validación', { birthDate: 'Edad mínima inválida' }),
        )
        const { user } = renderWithRouter()

        await fillForm(user)
        await fillDate(user, '2000-01-15')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Edad mínima inválida')).toBeInTheDocument()
        })
    })

    it('bloquea nombre con contenido XSS', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Nombre'), '<script>alert(1)</script>')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        expect(screen.getByText('El texto contiene caracteres no permitidos')).toBeInTheDocument()
        expect(mockedRegister).not.toHaveBeenCalled()
    })

    it('bloquea nombre con SQL injection', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Nombre'), "' OR 1=1 --")
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        expect(screen.getByText('El texto contiene caracteres no permitidos')).toBeInTheDocument()
        expect(mockedRegister).not.toHaveBeenCalled()
    })

    it('bloquea nombre que supera el máximo de caracteres', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Nombre'), 'a'.repeat(51))
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        expect(screen.getByText('Máximo 50 caracteres')).toBeInTheDocument()
        expect(mockedRegister).not.toHaveBeenCalled()
    })

    it('envía los valores con trim al backend', async () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: true })
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Nombre'), '  Mili  ')
        await user.type(screen.getByPlaceholderText('Email'), '  mili@mail.com  ')

        const passwordFields = screen.getAllByPlaceholderText(/contraseña/i)
        await user.type(passwordFields[0], '123456')
        await user.type(passwordFields[1], '123456')

        await fillDate(user, '2000-01-15')
        await user.click(screen.getByRole('checkbox'))
        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(mockedRegister).toHaveBeenCalledWith(
                expect.objectContaining({
                    name: 'Mili',
                    email: 'mili@mail.com',
                }),
            )
        })
    })

    it('redirige al /onboarding cuando onBoardingCompleted es false', async () => {
        mockedRegister.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER', onBoardingCompleted: false })
         mockLoginWithToken.mockResolvedValueOnce({ id: 1, name: 'Mili', email: 'mili@mail.com', role: 'USER', onBoardingCompleted: false })
        const { user } = renderWithRouter()

        await fillForm(user)
        const dateInput = screen.getByPlaceholderText('Fecha de nacimiento')
        await user.click(dateInput)
        await user.type(dateInput, '2000-01-15')

        const checkbox = screen.getByRole('checkbox')
        await user.click(checkbox)

        await user.click(getSubmitButton())

        await waitFor(() => {
            expect(screen.getByText('Onboarding')).toBeInTheDocument()
        })
    })

})
