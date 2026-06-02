import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/apiError'
import { useAuth } from '../../context/auth'
import { useAuthForm } from '../../hooks/useAuthForm'
import { required, validEmail } from '../../utils/validation'
import AuthPageLayout from '../../layouts/AuthPageLayout/AuthPageLayout'
import AuthForm from '../../components/AuthForm/AuthForm'
import type { AuthFormField } from '../../components/AuthForm/AuthForm'

const LOGIN_FIELDS: AuthFormField[] = [
  { name: 'email', type: 'email', placeholder: 'Email' },
  { name: 'password', type: 'password', placeholder: 'Contraseña' },
]

const INITIAL_VALUES = {
  email: '',
  password: '',
}

const VALIDATION_RULES = {
  email: [required(), validEmail()],
  password: [required()],
}

export default function Login() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const { values, errors, handleChange, validateAll, setFieldErrors } = useAuthForm(
    INITIAL_VALUES,
    VALIDATION_RULES,
  )

  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)

  const handleSubmit = async () => {
    if (!validateAll()) return

    setLoading(true)
    setApiError(null)

    try {
      const profile = await login({
        email: values.email,
        password: values.password,
      })
      navigate(profile.onBoardingCompleted === true ? '/' : '/onboarding')
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setFieldErrors(err.fieldErrors)
      } else {
        const message = err instanceof Error ? err.message : 'Error inesperado'
        setApiError(
          message === 'Invalid credentials'
            ? 'Email o contraseña incorrectos'
            : message,
        )
      }
      setLoading(false)
    }
  }

  return (
    <AuthPageLayout>
      <AuthForm
        title="¡Bienvenido!"
        subtitle="Ingresá a tu jardín"
        fields={LOGIN_FIELDS}
        values={values}
        errors={errors}
        onChange={handleChange}
        onSubmit={handleSubmit}
        loading={loading}
        submitLabel="Iniciar sesión"
        loadingLabel="Ingresando..."
        apiError={apiError}
        switchText="¿No tenés cuenta?"
        switchLabel="Registrate"
        onSwitchMode={() => navigate('/register')}
      />
    </AuthPageLayout>
  )
}
