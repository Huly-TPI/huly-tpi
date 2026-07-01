import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AuthForm from '../../components/AuthForm/AuthForm'
import type { AuthFormField } from '../../components/AuthForm/AuthForm'

const FIELDS: AuthFormField[] = [
    { name: 'email', type: 'email', placeholder: 'Email' },
    { name: 'password', type: 'password', placeholder: 'Contraseña' },
]

const defaultProps = {
    title: 'Test',
    fields: FIELDS,
    values: { email: '', password: '' },
    errors: {},
    onChange: vi.fn(),
    onSubmit: vi.fn(),
    loading: false,
    submitLabel: 'Enviar',
    loadingLabel: 'Enviando...',
    apiError: null,
}

describe('AuthForm', () => {
    it('renderiza el título', () => {
        render(<AuthForm {...defaultProps} title="¡Crea tu cuenta!" />)
        expect(screen.getByText('¡Crea tu cuenta!')).toBeInTheDocument()
    })

    it('renderiza el subtítulo cuando se pasa', () => {
        render(<AuthForm {...defaultProps} subtitle="Comenzá tu aventura" />)
        expect(screen.getByText('Comenzá tu aventura')).toBeInTheDocument()
    })

    it('no renderiza subtítulo si no se pasa', () => {
        render(<AuthForm {...defaultProps} />)
        expect(screen.queryByText('Comenzá tu aventura')).not.toBeInTheDocument()
    })

    it('renderiza todos los campos', () => {
        render(<AuthForm {...defaultProps} />)
        expect(screen.getByPlaceholderText('Email')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('Contraseña')).toBeInTheDocument()
    })

    it('renderiza labels accesibles para cada campo', () => {
        render(<AuthForm {...defaultProps} />)
        expect(screen.getByLabelText('Email')).toBeInTheDocument()
        expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
    })

    it('muestra el botón con el label correcto', () => {
        render(<AuthForm {...defaultProps} submitLabel="Registrarse" />)
        expect(screen.getByRole('button', { name: 'Registrarse' })).toBeInTheDocument()
    })

    it('muestra el label de carga cuando loading es true', () => {
        render(<AuthForm {...defaultProps} loading={true} loadingLabel="Cargando..." />)
        expect(screen.getByRole('button', { name: 'Cargando...' })).toBeDisabled()
    })

    it('muestra errores por campo', () => {
        const errors = { email: 'Email inválido' }
        render(<AuthForm {...defaultProps} errors={errors} />)
        expect(screen.getByText('Email inválido')).toBeInTheDocument()
        expect(screen.getByText('Email inválido')).toHaveAttribute('role', 'alert')
    })

    it('muestra error de API', () => {
        render(<AuthForm {...defaultProps} apiError="Error del servidor" />)
        expect(screen.getByText('Error del servidor')).toBeInTheDocument()
        expect(screen.getByText('Error del servidor')).toHaveAttribute('role', 'alert')
    })

    it('marca el input con aria-invalid cuando tiene error', () => {
        const errors = { email: 'Requerido' }
        render(<AuthForm {...defaultProps} errors={errors} />)
        expect(screen.getByPlaceholderText('Email')).toHaveAttribute('aria-invalid', 'true')
    })

    it('llama a onChange al escribir en un campo', async () => {
        const onChange = vi.fn()
        const user = userEvent.setup()
        render(<AuthForm {...defaultProps} onChange={onChange} />)

        await user.type(screen.getByPlaceholderText('Email'), 'a')

        expect(onChange).toHaveBeenCalledWith('email', 'a')
    })

    it('llama a onSubmit al enviar el formulario', async () => {
        const onSubmit = vi.fn()
        const user = userEvent.setup()
        render(<AuthForm {...defaultProps} onSubmit={onSubmit} />)

        await user.click(screen.getByRole('button', { name: 'Enviar' }))

        expect(onSubmit).toHaveBeenCalledOnce()
    })

    it('renderiza checkbox de términos cuando se pasa onTermsChange', () => {
        render(
            <AuthForm
                {...defaultProps}
                termsAccepted={false}
                onTermsChange={vi.fn()}
            />,
        )
        expect(screen.getByText(/términos y condiciones/)).toBeInTheDocument()
    })

    it('deshabilita el botón si los términos no están aceptados', () => {
        render(
            <AuthForm
                {...defaultProps}
                termsAccepted={false}
                onTermsChange={vi.fn()}
            />,
        )
        expect(screen.getByRole('button', { name: 'Enviar' })).toBeDisabled()
    })

    it('habilita el botón cuando los términos se aceptan', () => {
        render(
            <AuthForm
                {...defaultProps}
                termsAccepted={true}
                onTermsChange={vi.fn()}
            />,
        )
        expect(screen.getByRole('button', { name: 'Enviar' })).toBeEnabled()
    })

    it('renderiza el link de switch mode', () => {
        render(
            <AuthForm
                {...defaultProps}
                switchText="¿Ya tenés cuenta?"
                switchLabel="Iniciá sesión"
                onSwitchMode={vi.fn()}
            />,
        )
        expect(screen.getByText('¿Ya tenés cuenta?')).toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Iniciá sesión' })).toBeInTheDocument()
    })

    it('llama a onSwitchMode al hacer click', async () => {
        const onSwitchMode = vi.fn()
        const user = userEvent.setup()
        render(
            <AuthForm
                {...defaultProps}
                switchText="¿Ya tenés cuenta?"
                switchLabel="Iniciá sesión"
                onSwitchMode={onSwitchMode}
            />,
        )

        await user.click(screen.getByRole('button', { name: 'Iniciá sesión' }))

        expect(onSwitchMode).toHaveBeenCalledOnce()
    })

    it('renderiza campo de fecha con ícono de calendario', () => {
        const fields: AuthFormField[] = [
            { name: 'birthDate', type: 'date', placeholder: 'Fecha de nacimiento' },
        ]
        render(
            <AuthForm
                {...defaultProps}
                fields={fields}
                values={{ birthDate: '' }}
            />,
        )
        expect(screen.getByPlaceholderText('Fecha de nacimiento')).toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Abrir calendario' })).toBeInTheDocument()
    })

    it('renderiza toggle de contraseña', () => {
        render(<AuthForm {...defaultProps} />)
        expect(screen.getByRole('button', { name: 'Mostrar contraseña' })).toBeInTheDocument()
    })

    it('renderiza el link de olvidaste tu contraseña cuando se pasa onForgotPassword', () => {
        render(<AuthForm {...defaultProps} onForgotPassword={vi.fn()} />)
        expect(screen.getByRole('button', { name: '¿Olvidaste tu contraseña?' })).toBeInTheDocument()
    })

    it('no renderiza el link de olvidaste tu contraseña si no se pasa onForgotPassword', () => {
        render(<AuthForm {...defaultProps} />)
        expect(screen.queryByRole('button', { name: '¿Olvidaste tu contraseña?' })).not.toBeInTheDocument()
    })

    it('llama a onForgotPassword al hacer click en el link', async () => {
        const onForgotPassword = vi.fn()
        const user = userEvent.setup()
        render(<AuthForm {...defaultProps} onForgotPassword={onForgotPassword} />)

        await user.click(screen.getByRole('button', { name: '¿Olvidaste tu contraseña?' }))

        expect(onForgotPassword).toHaveBeenCalledOnce()
    })

    it('muestra el mensaje de éxito cuando se pasa successMessage', () => {
        render(<AuthForm {...defaultProps} successMessage="¡Contraseña actualizada!" />)
        expect(screen.getByText('¡Contraseña actualizada!')).toBeInTheDocument()
    })

    it('no muestra mensaje de éxito si successMessage es null', () => {
        render(<AuthForm {...defaultProps} successMessage={null} />)
        expect(screen.queryByText('¡Contraseña actualizada!')).not.toBeInTheDocument()
    })
})