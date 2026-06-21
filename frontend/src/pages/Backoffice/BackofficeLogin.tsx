import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { backofficeLogin } from '../../api/auth'
import { useForm, required, validEmail } from '../../hooks/useForm'
import Button from '../../components/Buttons/Button/Button'
import { Leaf, Eye, EyeOff } from 'lucide-react'
import colorLogo from '../../assets/brand/color-logo.webp'

import { useAuth } from '../../context/auth'

const INITIAL_VALUES = { email: '', password: '' }

const VALIDATION_RULES = {
  email: [required(), validEmail()],
  password: [required()],
}

const getInputClassName = (hasError: boolean) => {
  return `w-full rounded-xl border px-4 py-2.5 text-sm outline-none transition-all dark:bg-[#09111f] dark:text-gray-100 ${
    hasError
      ? 'border-red-500 focus:border-red-500 focus:ring-2 focus:ring-red-500/20'
      : 'border-gray-200 dark:border-gray-800 focus:border-violeta dark:focus:border-violeta-claro focus:ring-2 focus:ring-violeta/20'
  }`
}

export default function BackofficeLogin() {
  const navigate = useNavigate()
  const { user, loading: authLoading, isAuthenticated, loginWithToken } = useAuth()
  const { values, errors, handleChange, validateAll } = useForm(INITIAL_VALUES, VALIDATION_RULES)

  const [passwordVisible, setPasswordVisible] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#EDF2ED] dark:bg-[#09111f]">
        <span className="text-sm text-gray-500">Cargando...</span>
      </div>
    )
  }

  if (isAuthenticated && user?.role === 'ADMIN') 
    return <Navigate to="/backoffice" replace />

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!validateAll()) return

    setApiError(null)
    setLoading(true)

    try {
      const res = await backofficeLogin({ email: values.email, password: values.password })
      localStorage.setItem('role', res.role)
      await loginWithToken(res.accessToken)
      navigate('/backoffice')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error inesperado'
      setApiError(
        message === 'Invalid credentials'
          ? 'Credenciales incorrectas'
          : message,
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-[#EDF2ED] dark:bg-[#09111f] transition-colors duration-200">
      <div className="w-full max-w-sm rounded-2xl bg-white dark:bg-[#172033] p-10 shadow-md dark:shadow-none dark:border dark:border-gray-800/40">

        <div className="mb-8 flex flex-col items-center gap-3">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#D1CAEF] dark:bg-[#2A233C]">
            <Leaf className="h-8 w-8 text-violeta dark:text-violeta-claro" strokeWidth={2} />
          </div>
          <img src={colorLogo} alt="Huly" className="h-8 object-contain" />
          <p className="text-xs font-bold uppercase tracking-widest text-[#A0AEC0] dark:text-gray-500">
            Backoffice
          </p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-[#4A5568] dark:text-gray-300">Email</label>
            <input
              type="text"
              value={values.email}
              onChange={e => handleChange('email', e.target.value)}
              placeholder="Correo electrónico"
              className={getInputClassName(!!errors.email)}
            />
            {errors.email && (
              <p className="text-xs text-red-500">{errors.email}</p>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-[#4A5568] dark:text-gray-300">Contraseña</label>
            <div className="relative">
              <input
                type={passwordVisible ? 'text' : 'password'}
                value={values.password}
                onChange={e => handleChange('password', e.target.value)}
                placeholder="••••••••"
                className={`pr-12 ${getInputClassName(!!errors.password)}`}
              />
              <button
                type="button"
                onClick={() => setPasswordVisible(prev => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
                aria-label={passwordVisible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {passwordVisible ? (
                  <EyeOff className="h-5 w-5" />
                ) : (
                  <Eye className="h-5 w-5" />
                )}
              </button>
            </div>
            {errors.password && (
              <p className="text-xs text-red-500">{errors.password}</p>
            )}
          </div>

          {apiError && (
            <p className="rounded-xl bg-red-50 dark:bg-red-950/20 px-4 py-2.5 text-sm text-red-600 dark:text-red-400 border border-red-100 dark:border-red-900/30">
              {apiError}
            </p>
          )}

          <Button
            type="submit"
            fullWidth
            isLoading={loading}
            loadingLabel="Ingresando..."
            className="mt-2"
          >
            Iniciar sesión
          </Button>
        </form>
      </div>
    </div>
  )
}
