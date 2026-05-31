import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import Register from '../../pages/Register/Register'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/auth', () => ({
    register: vi.fn(),
    login: vi.fn(),
}))

import { register, login } from '../../api/auth'

const mockedRegister = vi.mocked(register)
const mockedLogin = vi.mocked(login)

describe('Register', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        window.localStorage.clear()
    })

    const renderWithRouter = () => {
        const user = userEvent.setup()
        render(
            <MemoryRouter initialEntries={['/register']}>
                <Routes>
                    <Route path="/register" element={<Register />} />
                    <Route path="/" element={<h1>Home</h1>} />
                    <Route path="/login" element={<h1>Login</h1>} />
                </Routes>
            </MemoryRouter>,
        )
        return { user }
    }

    const fillForm = async (user: ReturnType<typeof userEvent.setup>) => {
        await user.type(screen.getByPlaceholderText('Nombre'), 'Luka')
        await user.type(screen.getByPlaceholderText('Email'), 'luka@mail.com')

        const passwordFields = screen.getAllByPlaceholderText(/contraseña/i)
        await user.type(passwordFields[0], '123456')
        await user.type(passwordFields[1], '123456')
    }

    const fillFormWithDate = async (user: ReturnType<typeof userEvent.setup>) => {
        await fillForm(user)

        const dateInput = screen.getByPlaceholderText('Fecha de nacimiento')
        await user.click(dateInput)
        await user.type(dateInput, '2000-01-15')

        await user.click(screen.getByRole('checkbox'))
    }

    it('renderiza el formulario de registro', () => {
        renderWithRouter()

        expect(screen.getByText('¡Crea tu cuenta!')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('Nombre')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('Email')).toBeInTheDocument()
    })

    it('muestra errores de validación al enviar vacío', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('checkbox'))
        await user.click(screen.getByRole('button', { name: '🌱 Crear cuenta' }))

        expect(screen.getAllByRole('alert').length).toBeGreaterThan(0)
    })

    it('registra, auto-loguea y redirige al home', async () => {
        mockedRegister.mockResolvedValueOnce(undefined)
        mockedLogin.mockResolvedValueOnce({ role: 'USER' })
        const { user } = renderWithRouter()

        await fillFormWithDate(user)
        await user.click(screen.getByRole('button', { name: '🌱 Crear cuenta' }))

        await waitFor(() => {
            expect(screen.getByText('Home')).toBeInTheDocument()
        })

        expect(mockedRegister).toHaveBeenCalledOnce()
        expect(mockedLogin).toHaveBeenCalledWith({
            email: 'luka@mail.com',
            password: '123456',
        })
    })

    it('guarda el role en localStorage tras registro exitoso', async () => {
        mockedRegister.mockResolvedValueOnce(undefined)
        mockedLogin.mockResolvedValueOnce({ role: 'USER' })
        const { user } = renderWithRouter()

        await fillFormWithDate(user)
        await user.click(screen.getByRole('button', { name: '🌱 Crear cuenta' }))

        await waitFor(() => {
            expect(localStorage.getItem('role')).toBe('USER')
        })
    })

    it('muestra error si el registro falla', async () => {
        mockedRegister.mockRejectedValueOnce(new Error('El email ya está registrado'))
        const { user } = renderWithRouter()

        await fillFormWithDate(user)
        await user.click(screen.getByRole('button', { name: '🌱 Crear cuenta' }))

        await waitFor(() => {
            expect(screen.getByText('El email ya está registrado')).toBeInTheDocument()
        })

        expect(mockedLogin).not.toHaveBeenCalled()
    })

    it('muestra error si el auto-login falla', async () => {
        mockedRegister.mockResolvedValueOnce(undefined)
        mockedLogin.mockRejectedValueOnce(new Error('Error inesperado'))
        const { user } = renderWithRouter()

        await fillFormWithDate(user)
        await user.click(screen.getByRole('button', { name: '🌱 Crear cuenta' }))

        await waitFor(() => {
            expect(screen.getByText('Error inesperado')).toBeInTheDocument()
        })
    })

    it('navega a login al hacer click en Iniciá sesión', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('button', { name: 'Iniciá sesión' }))

        expect(screen.getByText('Login')).toBeInTheDocument()
    })

    it('deshabilita el botón si no acepta términos', () => {
        renderWithRouter()

        expect(screen.getByRole('button', { name: '🌱 Crear cuenta' })).toBeDisabled()
    })

    it('habilita el botón al aceptar términos', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('checkbox'))

        expect(screen.getByRole('button', { name: '🌱 Crear cuenta' })).toBeEnabled()
    })

    it('muestra error de edad mínima con fecha reciente', async () => {
        const { user } = renderWithRouter()

        await fillForm(user)

        const dateInput = screen.getByPlaceholderText('Fecha de nacimiento')
        await user.click(dateInput)
        await user.type(dateInput, '2020-01-01')

        await user.click(screen.getByRole('checkbox'))
        await user.click(screen.getByRole('button', { name: '🌱 Crear cuenta' }))

        expect(screen.getByText('Debés tener al menos 13 años')).toBeInTheDocument()
        expect(mockedRegister).not.toHaveBeenCalled()
    })

    it('muestra errores por campo del backend', async () => {
        mockedRegister.mockRejectedValueOnce(
            new ApiError('Error de validación', { birthDate: 'Edad mínima inválida' }),
        )
        const { user } = renderWithRouter()

        await fillFormWithDate(user)
        await user.click(screen.getByRole('button', { name: '🌱 Crear cuenta' }))

        await waitFor(() => {
            expect(screen.getByText('Edad mínima inválida')).toBeInTheDocument()
        })
    })
})