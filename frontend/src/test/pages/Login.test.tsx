import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Login from '../../pages/Login/Login'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/auth', () => ({
    login: vi.fn(),
}))

import { login } from '../../api/auth'

const mockedLogin = vi.mocked(login)

describe('Login', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        window.localStorage.clear()
    })

    const renderWithRouter = () => {
        const user = userEvent.setup()
        render(
            <MemoryRouter initialEntries={['/login']}>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/" element={<h1>Home</h1>} />
                    <Route path="/register" element={<h1>Register</h1>} />
                </Routes>
            </MemoryRouter>,
        )
        return { user }
    }

    const fillForm = async (user: ReturnType<typeof userEvent.setup>) => {
        await user.type(screen.getByPlaceholderText('Email'), 'luka@mail.com')
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

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
    })

    it('inicia sesión exitosamente y redirige al home', async () => {
        mockedLogin.mockResolvedValueOnce({ role: 'USER' })
        const { user } = renderWithRouter()

        await fillForm(user)

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Home')).toBeInTheDocument()
        })
    })

    it('guarda el role en localStorage tras login exitoso', async () => {
        mockedLogin.mockResolvedValueOnce({ role: 'USER' })
        const { user } = renderWithRouter()

        await fillForm(user)

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(localStorage.getItem('role')).toBe('USER')
        })
    })

    it('no guarda token en localStorage tras login exitoso', async () => {
        mockedLogin.mockResolvedValueOnce({ role: 'USER' })
        const { user } = renderWithRouter()

        await fillForm(user)

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Home')).toBeInTheDocument()
        })

        expect(localStorage.getItem('token')).toBeNull()
    })

    it('muestra error del backend cuando falla el login', async () => {
        mockedLogin.mockRejectedValueOnce(new Error('Invalid credentials'))
        const { user } = renderWithRouter()

        await fillForm(user)

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
        })
    })

    it('muestra errores por campo del backend', async () => {
        mockedLogin.mockRejectedValueOnce(
            new ApiError('Error de validación', { email: 'Email inválido' }),
        )
        const { user } = renderWithRouter()

        await fillForm(user)

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Email inválido')).toBeInTheDocument()
        })
    })

    it('muestra error con email inválido sin llamar a la API', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Email'), 'no-es-email')
        await user.type(screen.getByPlaceholderText('Contraseña'), '123456')

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        expect(screen.getByText('Email inválido')).toBeInTheDocument()
        expect(mockedLogin).not.toHaveBeenCalled()
    })

    it('muestra error con contraseña muy corta sin llamar a la API', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Email'), 'luka@mail.com')
        await user.type(screen.getByPlaceholderText('Contraseña'), 'abc')

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        expect(screen.getByText('Mínimo 6 caracteres')).toBeInTheDocument()
        expect(mockedLogin).not.toHaveBeenCalled()
    })

    it('navega a register al hacer click en Registrate', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('button', { name: 'Registrate' }))

        expect(screen.getByText('Register')).toBeInTheDocument()
    })
})