import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { register } from '../../api/auth'
import { ApiError } from '../../api/apiError'

import { useAuth } from '../../context/auth'
import { useAuthForm } from '../../hooks/useAuthForm'
import { required, validEmail, minLength, maxLength, matchesField, minAge, safeText } from '../../utils/validation'

import registerBackground from '../../assets/register/background.webp'
import registerCharacter from '../../assets/register/huly.webp'
import cardFrame from '../../assets/register/cardFrame.webp'
import AuthForm from '../../components/AuthForm/AuthForm'
import type { AuthFormField } from '../../components/AuthForm/AuthForm'

const REGISTER_FIELDS: AuthFormField[] = [
    { name: 'name', type: 'text', placeholder: 'Nombre' },
    { name: 'birthDate', type: 'date', placeholder: 'Fecha de nacimiento' },
    { name: 'email', type: 'email', placeholder: 'Email' },
    { name: 'password', type: 'password', placeholder: 'Contraseña' },
    { name: 'confirmPassword', type: 'password', placeholder: 'Confirmar contraseña' },
]

const INITIAL_VALUES = {
    name: '',
    birthDate: '',
    email: '',
    password: '',
    confirmPassword: '',
}

const VALIDATION_RULES = {
    name: [required(), maxLength(50), safeText],
    birthDate: [required('La fecha de nacimiento es requerida'), minAge(13)],
    email: [required(), validEmail(), maxLength(100)],
    password: [required(), minLength(6), maxLength(72)],
    confirmPassword: [required(), matchesField('password')],
}

export default function Register() {
    const navigate = useNavigate()
    const { loginWithToken } = useAuth()
    const { values, errors, handleChange, validateAll, setFieldErrors, getSanitizedValues } = useAuthForm(
        INITIAL_VALUES,
        VALIDATION_RULES,
    )

    const [loading, setLoading] = useState(false)
    const [apiError, setApiError] = useState<string | null>(null)
    const [termsAccepted, setTermsAccepted] = useState(false)

    const handleSubmit = async () => {
        if (!validateAll()) return
        if (!termsAccepted) return

        setLoading(true)
        setApiError(null)

        try {
            const sanitized = getSanitizedValues()

            const res = await register({
                name: sanitized.name,
                birthDate: sanitized.birthDate,
                email: sanitized.email,
                password: sanitized.password,
            })

            if (!res?.accessToken) {
                throw new Error('No se recibió el token de acceso')
            }

            await loginWithToken(res.accessToken)
            navigate('/')
        } catch (err) {
            if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
                setFieldErrors(err.fieldErrors)
            } else {
                setApiError(err instanceof Error ? err.message : 'Error inesperado')
            }
            setLoading(false)
        }
    }

    return (
        <div
            className="relative min-h-screen flex items-center justify-center overflow-hidden"
            style={{
                backgroundImage: `url(${registerBackground})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
            }}
        >
            <div className="absolute inset-0 backdrop-blur-md" />

            <div className="relative z-10 flex w-full max-w-5xl items-center justify-center gap-12 px-4 md:px-10 pt-0 pb-8">
                <img
                    src={registerCharacter}
                    alt="Mascota de Huly"
                    className="hidden lg:block h-[360px] object-contain self-end mb-6"
                />

                <div
                    className="relative w-full max-w-[calc(100%-2rem)] sm:max-w-sm px-8 py-12 md:px-[4.5rem] md:py-16"
                    style={{
                        backgroundImage: `url(${cardFrame})`,
                        backgroundSize: '100% 100%',
                        backgroundRepeat: 'no-repeat',
                    }}
                >
                    <AuthForm
                        title="¡Crea tu cuenta!"
                        subtitle="Comenzá tu aventura en el jardín"
                        fields={REGISTER_FIELDS}
                        values={values}
                        errors={errors}
                        onChange={handleChange}
                        onSubmit={handleSubmit}
                        loading={loading}
                        submitLabel="🌱 Crear cuenta "
                        loadingLabel="Creando tu cuenta..."
                        apiError={apiError}
                        termsAccepted={termsAccepted}
                        onTermsChange={setTermsAccepted}
                        switchText="¿Ya tenés cuenta?"
                        switchLabel="Iniciá sesión"
                        onSwitchMode={() => navigate('/login')}
                    />
                </div>
            </div>
        </div>
    )
}