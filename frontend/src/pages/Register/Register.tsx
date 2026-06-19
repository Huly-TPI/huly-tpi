import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { register } from '../../api/auth'
import { useAuth } from '../../context/auth'
import { ApiError } from '../../api/apiError'
import { useForm, ValidationRule, required, validEmail, minLength, maxLength, matchesField, safeText } from '../../hooks/useForm'
import AuthPageLayout from '../../layouts/AuthPageLayout/AuthPageLayout'
import hulyGreeting from '../../assets/register/huly-greeting.webp'
import AuthForm from '../../components/AuthForm/AuthForm'
import type { AuthFormField } from '../../components/AuthForm/AuthForm'

const REGISTER_NAME_PATTERN = /^(?=(?:.*\p{L}){3,})[\p{L}]+(?:\s+[\p{L}]+)*$/u

export const minAge = (min: number, message?: string): ValidationRule =>
  (value) => {
    if (!value) 
      return undefined
    
    const birth = new Date(value)
    const today = new Date()
    let age = today.getFullYear() - birth.getFullYear()
    const monthDiff = today.getMonth() - birth.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) 
      age -= 1
    
    if (age >= min) 
      return undefined
    
    if (message) 
      return message
    
    return `Debés tener al menos ${min} años`
  }

export const validRegisterName = (
  message = 'El nombre debe tener al menos 3 letras. Solo puede contener letras y espacios',
): ValidationRule =>
  (value) => {
    const normalizedValue = value.trim()
    if (REGISTER_NAME_PATTERN.test(normalizedValue)) 
      return undefined
    
    return message
  }

const REGISTER_FIELDS: AuthFormField[] = [
  { name: 'name', type: 'text', placeholder: 'Nombre' },
  { name: 'birthDate', type: 'date', placeholder: 'Fecha de nacimiento' },
  { name: 'email', type: 'email', placeholder: 'Email' },
  { name: 'password', type: 'password', placeholder: 'Contraseña', showPasswordChecklist: true },
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
  name: [required(), maxLength(50), safeText, validRegisterName()],
  birthDate: [required('La fecha de nacimiento es requerida'), minAge(13)],
  email: [required(), validEmail(), maxLength(100)],
  password: [required(), minLength(6)],
  confirmPassword: [required(), matchesField('password')],
}

export default function Register() {
  const navigate = useNavigate()
  const { loginWithToken } = useAuth()
  const { values, errors, handleChange, validateAll, setFieldErrors, getSanitizedValues } = useForm(
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

      const profile = await loginWithToken(res.accessToken)
      navigate(profile.onBoardingCompleted === false ? '/onboarding' : '/')
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
    <AuthPageLayout reversed characterImage={hulyGreeting}>
      <AuthForm
        title="¡Creá tu cuenta!"
        subtitle="Comenzá tu aventura en el jardín"
        fields={REGISTER_FIELDS}
        values={values}
        errors={errors}
        onChange={handleChange}
        onSubmit={handleSubmit}
        loading={loading}
        submitLabel="Crear cuenta "
        loadingLabel="Creando tu cuenta..."
        apiError={apiError}
        termsAccepted={termsAccepted}
        onTermsChange={setTermsAccepted}
        switchText="¿Ya tenés cuenta?"
        switchLabel="Iniciá sesión"
        onSwitchMode={() => navigate('/login')}
      />
    </AuthPageLayout>
  )
}
