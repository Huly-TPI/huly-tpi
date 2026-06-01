import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import BackofficeLogin from '../../pages/Backoffice/BackofficeLogin'
import { ApiError } from '../../api/apiError'

vi.mock('../../api/auth', () => ({
    backofficeLogin: vi.fn(),
}))

vi.mock('../../assets/brand/color-logo.webp', () => ({ default: 'color-logo.webp' }))
vi.mock('../../assets/backoffice/hojita.webp', () => ({ default: 'hojita.webp' }))

import { backofficeLogin } from '../../api/auth'

const mockedBackofficeLogin = vi.mocked(backofficeLogin)

describe('BackofficeLogin', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        window.localStorage.clear()
    })

    const renderWithRouter = () => {
        const user = userEvent.setup()
        render(
            <MemoryRouter initialEntries={['/backoffice/login']}>
                <Routes>
                    <Route path="/backoffice/login" element={<BackofficeLogin />} />
                    <Route path="/backoffice" element={<h1>Backoffice</h1>} />
                </Routes>
            </MemoryRouter>,
        )
        return { user }
    }

    const fillForm = async (user: ReturnType<typeof userEvent.setup>) => {
        await user.type(screen.getByPlaceholderText('Correo electrónico'), 'admin@huly.com')
        await user.type(screen.getByPlaceholderText('••••••••'), 'password123')
    }

    it('renderiza el formulario de login', () => {
        renderWithRouter()

        expect(screen.getByPlaceholderText('Correo electrónico')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Iniciar sesión' })).toBeInTheDocument()
    })

    it('muestra error de validación si el email está vacío', async () => {
        const { user } = renderWithRouter()

        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        expect(screen.getAllByText('Campo requerido').length).toBeGreaterThan(0)
        expect(mockedBackofficeLogin).not.toHaveBeenCalled()
    })

    it('muestra error de validación si la contraseña está vacía', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Correo electrónico'), 'admin@huly.com')
        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        expect(screen.getByText('Campo requerido')).toBeInTheDocument()
        expect(mockedBackofficeLogin).not.toHaveBeenCalled()
    })

    it('muestra error de validación si el email tiene formato inválido', async () => {
        const { user } = renderWithRouter()

        await user.type(screen.getByPlaceholderText('Correo electrónico'), 'noesmail')
        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        expect(screen.getByText('Email inválido')).toBeInTheDocument()
        expect(mockedBackofficeLogin).not.toHaveBeenCalled()
    })

    it('inicia sesión exitosamente y redirige al backoffice', async () => {
        mockedBackofficeLogin.mockResolvedValueOnce({ accessToken: 'token-123', role: 'ADMIN' })
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Backoffice')).toBeInTheDocument()
        })
    })

    it('guarda token y role ADMIN en localStorage al iniciar sesión', async () => {
        mockedBackofficeLogin.mockResolvedValueOnce({ accessToken: 'token-123', role: 'ADMIN' })
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        await waitFor(() => {
            expect(localStorage.getItem('token')).toBe('token-123')
            expect(localStorage.getItem('role')).toBe('ADMIN')
        })
    })

    it('muestra mensaje amigable para credenciales inválidas', async () => {
        mockedBackofficeLogin.mockRejectedValueOnce(new Error('Invalid credentials'))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Credenciales incorrectas')).toBeInTheDocument()
        })
    })

    it('muestra error genérico del backend', async () => {
        mockedBackofficeLogin.mockRejectedValueOnce(new ApiError('Error del servidor', {}))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        await waitFor(() => {
            expect(screen.getByText('Error del servidor')).toBeInTheDocument()
        })
    })

    it('deshabilita el botón mientras carga', async () => {
        mockedBackofficeLogin.mockImplementation(() => new Promise(() => {}))
        const { user } = renderWithRouter()

        await fillForm(user)
        await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

        expect(screen.getByRole('button', { name: 'Ingresando...' })).toBeDisabled()
    })
})
