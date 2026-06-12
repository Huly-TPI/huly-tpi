import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Login from '../../pages/Login/Login'
import { ApiError } from '../../api/apiError'
import { ThemeProvider } from '../../context/theme'

const mockLogin = vi.fn()
vi.mock('../../context/auth', () => ({
    useAuth: () => ({ login: mockLogin }),
}))

describe('Login', () => {
    beforeEach(() => {
        vi.clearAllMocks()
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
})
