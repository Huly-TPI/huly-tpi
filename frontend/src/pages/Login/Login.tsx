import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/apiError'
import { useAuth } from '../../context/auth'
import { forgotPassword, resetPassword } from '../../api/auth'
import { useForm, required, validEmail, minLength } from '../../hooks/useForm'
import AuthPageLayout from '../../layouts/AuthPageLayout/AuthPageLayout'
import AuthForm from '../../components/AuthForm/AuthForm'
import type { AuthFormField } from '../../components/AuthForm/AuthForm'

type Step = 'login' | 'forgot' | 'reset'

// --- Login ---
const LOGIN_FIELDS: AuthFormField[] = [
  { name: 'email', type: 'email', placeholder: 'Email' },
  { name: 'password', type: 'password', placeholder: 'Contraseña' },
]
const LOGIN_RULES = {
  email: [required(), validEmail()],
  password: [required()],
}

// --- Forgot password ---
const FORGOT_FIELDS: AuthFormField[] = [
  { name: 'email', type: 'email', placeholder: 'Email' },
]
const FORGOT_RULES = {
  email: [required(), validEmail()],
}

// --- Reset password ---
const RESET_FIELDS: AuthFormField[] = [
  { name: 'token', type: 'text', placeholder: 'Código recibido por email' },
  { name: 'newPassword', type: 'password', placeholder: 'Nueva contraseña', showPasswordChecklist: true },
]
const RESET_RULES = {
  token: [required()],
  newPassword: [required(), minLength(6)],
}

export default function Login() {
  const navigate = useNavigate()
  const { login } = useAuth()

  const [step, setStep] = useState<Step>('login')
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const loginForm = useForm({ email: '', password: '' }, LOGIN_RULES)
  const forgotForm = useForm({ email: '' }, FORGOT_RULES)
  const resetForm = useForm({ token: '', newPassword: '' }, RESET_RULES)

  const goTo = (target: Step) => {
    setApiError(null)
    setSuccessMessage(null)
    setStep(target)
  }

  const handleLogin = async () => {
    if (!loginForm.validateAll()) return
    setLoading(true)
    setApiError(null)
    try {
      const profile = await login({
        email: loginForm.values.email,
        password: loginForm.values.password,
      })
      navigate(profile.onBoardingCompleted === true ? '/' : '/onboarding')
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        loginForm.setFieldErrors(err.fieldErrors)
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

  const handleForgot = async () => {
    if (!forgotForm.validateAll()) return
    setLoading(true)
    setApiError(null)
    try {
      await forgotPassword(forgotForm.values.email)
      goTo('reset')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error inesperado'
      setApiError(
        message.includes('No existe')
          ? 'No existe una cuenta con ese email'
          : message,
      )
      setLoading(false)
    }
  }

  const handleReset = async () => {
    if (!resetForm.validateAll()) return
    setLoading(true)
    setApiError(null)
    try {
      await resetPassword(resetForm.values.token, resetForm.values.newPassword)
      loginForm.setFieldErrors({})
      setSuccessMessage('¡Contraseña actualizada! Podés iniciar sesión.')
      goTo('login')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error inesperado'
      setApiError(
        message.includes('Token') || message.includes('nválido') || message.includes('xpirado')
          ? 'El código es inválido o ya expiró'
          : message,
      )
      setLoading(false)
    }
  }

  if (step === 'forgot') {
    return (
      <AuthPageLayout variant="login">
        <AuthForm
          title="Recuperar contraseña"
          subtitle="Ingresá tu email y te enviamos un código"
          fields={FORGOT_FIELDS}
          values={forgotForm.values}
          errors={forgotForm.errors}
          onChange={forgotForm.handleChange}
          onSubmit={handleForgot}
          loading={loading}
          submitLabel="Enviar código"
          loadingLabel="Enviando..."
          apiError={apiError}
          switchText="¿Ya recordaste tu contraseña?"
          switchLabel="Volver"
          onSwitchMode={() => goTo('login')}
        />
      </AuthPageLayout>
    )
  }

  if (step === 'reset') {
    return (
      <AuthPageLayout variant="login">
        <AuthForm
          title="Nueva contraseña"
          subtitle="Ingresá el código del email y tu nueva contraseña"
          fields={RESET_FIELDS}
          values={resetForm.values}
          errors={resetForm.errors}
          onChange={resetForm.handleChange}
          onSubmit={handleReset}
          loading={loading}
          submitLabel="Restablecer contraseña"
          loadingLabel="Restableciendo..."
          apiError={apiError}
          switchText="¿Necesitás un código nuevo?"
          switchLabel="Reenviar"
          onSwitchMode={() => goTo('forgot')}
        />
      </AuthPageLayout>
    )
  }

  return (
    <AuthPageLayout variant="login">
      <AuthForm
        title="¡Bienvenido!"
        subtitle="Ingresá a tu jardín"
        fields={LOGIN_FIELDS}
        values={loginForm.values}
        errors={loginForm.errors}
        onChange={loginForm.handleChange}
        onSubmit={handleLogin}
        loading={loading}
        submitLabel="Iniciar sesión"
        loadingLabel="Ingresando..."
        apiError={apiError}
        successMessage={successMessage}
        switchText="¿No tenés cuenta?"
        switchLabel="Registrate"
        onSwitchMode={() => navigate('/register')}
        onForgotPassword={() => goTo('forgot')}
      />
    </AuthPageLayout>
  )
}
