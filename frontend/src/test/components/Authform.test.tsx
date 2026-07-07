import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AuthForm from '../../components/AuthForm/AuthForm'
import type { AuthFormField } from '../../components/AuthForm/AuthForm'
import { clickButton, typePlaceholder, verifyTextPresent, verifyTextNotPresent, verifyPlaceholderPresent, verifyButtonDisabled, verifyButtonEnabled } from '../testHelpers'

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
    let onChangeMock: any
    let onSubmitMock: any
    let onSwitchModeMock: any
    let onForgotPasswordMock: any
    let onTermsChangeMock: any

    beforeEach(() => {
        onChangeMock = vi.fn()
        onSubmitMock = vi.fn()
        onSwitchModeMock = vi.fn()
        onForgotPasswordMock = vi.fn()
        onTermsChangeMock = vi.fn()
    })

    it('renderiza el título', () => {
        renderAuthForm({ title: '¡Crea tu cuenta!' })
        verifyTitleIsPresent('¡Crea tu cuenta!')
    })

    it('renderiza el subtítulo cuando se pasa', () => {
        renderAuthForm({ subtitle: 'Comenzá tu aventura' })
        verifySubtitleIsPresent('Comenzá tu aventura')
    })

    it('no renderiza subtítulo si no se pasa', () => {
        renderAuthForm()
        verifySubtitleNotPresent('Comenzá tu aventura')
    })

    it('renderiza todos los campos', () => {
        renderAuthForm()
        verifyFieldsArePresent(['Email', 'Contraseña'])
    })

    it('renderiza labels accesibles para cada campo', () => {
        renderAuthForm()
        verifyAccessibleLabels(['Email', 'Contraseña'])
    })

    it('muestra el botón con el label correcto', () => {
        renderAuthForm({ submitLabel: 'Registrarse' })
        verifyButtonWithLabel('Registrarse')
    })

    it('muestra el label de carga cuando loading es true', () => {
        renderAuthForm({ loading: true, loadingLabel: 'Cargando...' })
        verifyButtonDisabledWithLabel('Cargando...')
    })

    it('muestra errores por campo', () => {
        renderAuthForm({ errors: { email: 'Email inválido' } })
        verifyFieldError('Email inválido')
    })

    it('muestra error de API', () => {
        renderAuthForm({ apiError: 'Error del servidor' })
        verifyApiError('Error del servidor')
    })

    it('marca el input con aria-invalid cuando tiene error', () => {
        renderAuthForm({ errors: { email: 'Requerido' } })
        verifyFieldHasAriaInvalid('Email')
    })

    it('llama a onChange al escribir en un campo', () => {
        renderAuthForm()
        return typeIntoField('Email', 'a').then(() => {
            verifyOnChangeCalledWith('email', 'a')
        })
    })

    it('llama a onSubmit al enviar el formulario', () => {
        renderAuthForm()
        return submitForm().then(() => {
            verifyOnSubmitCalled()
        })
    })

    it('renderiza checkbox de términos cuando se pasa onTermsChange', () => {
        renderWithTerms(false)
        verifyTermsMessagePresent()
    })

    it('deshabilita el botón si los términos no están aceptados', () => {
        renderWithTerms(false)
        verifyButtonDisabledWithLabel('Enviar')
    })

    it('habilita el botón cuando los términos se aceptan', () => {
        renderWithTerms(true)
        verifyButtonEnabledWithLabel('Enviar')
    })

    it('renderiza el link de switch mode', () => {
        renderWithSwitch()
        verifySwitchLinkRendered()
    })

    it('llama a onSwitchMode al hacer click', () => {
        renderWithSwitch()
        return clickSwitchButton().then(() => {
            verifyOnSwitchModeCalled()
        })
    })

    it('renderiza campo de fecha con ícono de calendario', () => {
        renderDateField()
        verifyCalendarButtonPresent()
    })

    it('renderiza toggle de contraseña', () => {
        renderAuthForm()
        verifyShowPasswordButtonPresent()
    })

    it('renderiza el link de olvidaste tu contraseña cuando se pasa onForgotPassword', () => {
        renderWithForgotPassword()
        verifyForgotPasswordButtonPresent()
    })

    it('no renderiza el link de olvidaste tu contraseña si no se pasa onForgotPassword', () => {
        renderAuthForm()
        verifyForgotPasswordButtonNotPresent()
    })

    it('llama a onForgotPassword al hacer click en el link', () => {
        renderWithForgotPassword()
        return clickForgotPasswordButton().then(() => {
            verifyOnForgotPasswordCalled()
        })
    })

    it('muestra el mensaje de éxito cuando se pasa successMessage', () => {
        renderAuthForm({ successMessage: '¡Contraseña actualizada!' })
        verifySuccessMessage('¡Contraseña actualizada!')
    })

    it('no muestra mensaje de éxito si successMessage es null', () => {
        renderAuthForm({ successMessage: null })
        verifySuccessMessageNotPresent('¡Contraseña actualizada!')
    })
    const renderAuthForm = (props?: any) => {
        render(<AuthForm {...defaultProps} onChange={onChangeMock} onSubmit={onSubmitMock} {...props} />)
    }

    const verifyTitleIsPresent = (title: string) => {
        verifyTextPresent(title)
    }

    const verifySubtitleIsPresent = (subtitle: string) => {
        verifyTextPresent(subtitle)
    }

    const verifySubtitleNotPresent = (subtitle: string) => {
        verifyTextNotPresent(subtitle)
    }

    const verifyFieldsArePresent = (placeholders: string[]) => {
        placeholders.forEach(ph => verifyPlaceholderPresent(ph))
    }

    const verifyAccessibleLabels = (labels: string[]) => {
        labels.forEach(label => {
            expect(screen.getByLabelText(label)).toBeInTheDocument()
        })
    }

    const verifyButtonWithLabel = (label: string) => {
        expect(screen.getByRole('button', { name: label })).toBeInTheDocument()
    }

    const verifyButtonDisabledWithLabel = (label: string) => {
        verifyButtonDisabled(label)
    }

    const verifyButtonEnabledWithLabel = (label: string) => {
        verifyButtonEnabled(label)
    }

    const verifyFieldError = (errorText: string) => {
        verifyTextPresent(errorText)
        expect(screen.getByText(errorText)).toHaveAttribute('role', 'alert')
    }

    const verifyApiError = (errorText: string) => {
        verifyTextPresent(errorText)
        expect(screen.getByText(errorText)).toHaveAttribute('role', 'alert')
    }

    const verifyFieldHasAriaInvalid = (placeholder: string) => {
        expect(screen.getByPlaceholderText(placeholder)).toHaveAttribute('aria-invalid', 'true')
    }

    const typeIntoField = (placeholder: string, text: string) => {
        const user = userEvent.setup()
        return typePlaceholder(user, placeholder, text)
    }

    const verifyOnChangeCalledWith = (name: string, value: string) => {
        expect(onChangeMock).toHaveBeenCalledWith(name, value)
    }

    const submitForm = () => {
        const user = userEvent.setup()
        return clickButton(user, 'Enviar')
    }

    const verifyOnSubmitCalled = () => {
        expect(onSubmitMock).toHaveBeenCalledOnce()
    }

    const renderWithTerms = (accepted: boolean) => {
        renderAuthForm({
            termsAccepted: accepted,
            onTermsChange: onTermsChangeMock,
        })
    }

    const verifyTermsMessagePresent = () => {
        verifyTextPresent('términos y condiciones')
    }

    const renderWithSwitch = () => {
        renderAuthForm({
            switchText: '¿Ya tenés cuenta?',
            switchLabel: 'Iniciá sesión',
            onSwitchMode: onSwitchModeMock,
        })
    }

    const verifySwitchLinkRendered = () => {
        verifyTextPresent('¿Ya tenés cuenta?')
        expect(screen.getByRole('button', { name: 'Iniciá sesión' })).toBeInTheDocument()
    }

    const clickSwitchButton = () => {
        const user = userEvent.setup()
        return clickButton(user, 'Iniciá sesión')
    }

    const verifyOnSwitchModeCalled = () => {
        expect(onSwitchModeMock).toHaveBeenCalledOnce()
    }

    const renderDateField = () => {
        const fields: AuthFormField[] = [
            { name: 'birthDate', type: 'date', placeholder: 'Fecha de nacimiento' },
        ]
        renderAuthForm({
            fields,
            values: { birthDate: '' }
        })
    }

    const verifyCalendarButtonPresent = () => {
        verifyPlaceholderPresent('Fecha de nacimiento')
        expect(screen.getByRole('button', { name: 'Abrir calendario' })).toBeInTheDocument()
    }

    const verifyShowPasswordButtonPresent = () => {
        expect(screen.getByRole('button', { name: 'Mostrar contraseña' })).toBeInTheDocument()
    }

    const renderWithForgotPassword = () => {
        renderAuthForm({ onForgotPassword: onForgotPasswordMock })
    }

    const verifyForgotPasswordButtonPresent = () => {
        expect(screen.getByRole('button', { name: '¿Olvidaste tu contraseña?' })).toBeInTheDocument()
    }

    const verifyForgotPasswordButtonNotPresent = () => {
        expect(screen.queryByRole('button', { name: '¿Olvidaste tu contraseña?' })).not.toBeInTheDocument()
    }

    const clickForgotPasswordButton = () => {
        const user = userEvent.setup()
        return clickButton(user, '¿Olvidaste tu contraseña?')
    }

    const verifyOnForgotPasswordCalled = () => {
        expect(onForgotPasswordMock).toHaveBeenCalledOnce()
    }

    const verifySuccessMessage = (message: string) => {
        verifyTextPresent(message)
    }

    const verifySuccessMessageNotPresent = (message: string) => {
        verifyTextNotPresent(message)
    }
})