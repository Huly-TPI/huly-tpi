import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../../api/auth'
import { ApiError } from '../../api/apiError'
import { useAuthForm } from '../../hooks/useAuthForm'
import { required, validEmail, minLength } from '../../utils/validation'

import registerBackground from '../../assets/register/background.webp'
import registerCharacter from '../../assets/register/huly.webp'
import cardFrame from '../../assets/register/cardFrame.webp'
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
  password: [required(), minLength(6)],
}

export default function Login() {
  const navigate = useNavigate()
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
      const res = await login({
        email: values.email,
        password: values.password,
      })

      localStorage.setItem('token', res.accessToken)
      localStorage.setItem('role', res.role)
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
            title="¡Bienvenido!"
            subtitle="Ingresá a tu jardín"
            fields={LOGIN_FIELDS}
            values={values}
            errors={errors}
            onChange={handleChange}
            onSubmit={handleSubmit}
            loading={loading}
            submitLabel="🌿 Iniciar sesión"
            loadingLabel="Ingresando..."
            apiError={apiError}
            switchText="¿No tenés cuenta?"
            switchLabel="Registrate"
            onSwitchMode={() => navigate('/register')}
          />
        </div>
      </div>
    </div>
  )
}