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

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
    })

    it('muestra error con email inválido', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Email'), 'noesmail')
        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        expect(screen.getByText('Email inválido')).toBeInTheDocument()
        expect(mockedLogin).not.toHaveBeenCalled()
    })

    it('no valida longitud mínima de contraseña', async () => {
        mockedLogin.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Email'), 'mili@mail.com')
        await user.type(screen.getByPlaceholderText('Contraseña'), '123')

        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(mockedLogin).toHaveBeenCalledOnce()
        })
    })

    it('inicia sesión exitosamente y redirige al home', async () => {
        mockedLogin.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Home')).toBeInTheDocument()
        })
    })

    it('guarda token y role en localStorage al iniciar sesión', async () => {
        mockedLogin.mockResolvedValueOnce({ accessToken: 'token-123', role: 'USER' })
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(localStorage.getItem('token')).toBe('token-123')
            expect(localStorage.getItem('role')).toBe('USER')
        })
    })

    it('muestra mensaje amigable para credenciales inválidas', async () => {
        mockedLogin.mockRejectedValueOnce(new Error('Invalid credentials'))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Email o contraseña incorrectos')).toBeInTheDocument()
        })
    })

    it('muestra error genérico del backend', async () => {
        mockedLogin.mockRejectedValueOnce(new Error('Error del servidor'))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Error del servidor')).toBeInTheDocument()
        })
    })

    it('muestra errores por campo del backend', async () => {
        mockedLogin.mockRejectedValueOnce(
            new ApiError('Error de validación', { email: 'Email no registrado' }),
        )
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

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
        mockedLogin.mockImplementation(() => new Promise(() => { }))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: '🌿 Iniciar sesión' }))

        expect(screen.getByRole('button', { name: 'Ingresando...' })).toBeDisabled()
    })
})