import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { backofficeLogin } from '../../api/auth'
import { useAuthForm } from '../../hooks/useAuthForm'
import { required, validEmail } from '../../utils/validation'
import Button from '../../components/Buttons/Button/Button'
import colorLogo from '../../assets/brand/color-logo.webp'
import hojita from '../../assets/backoffice/hojita.webp'

const INITIAL_VALUES = { email: '', password: '' }

const VALIDATION_RULES = {
  email: [required(), validEmail()],
  password: [required()],
}

export default function BackofficeLogin() {
  const navigate = useNavigate()
  const { values, errors, handleChange, validateAll } = useAuthForm(INITIAL_VALUES, VALIDATION_RULES)
  const [apiError, setApiError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!validateAll()) return

    setApiError(null)
    setLoading(true)

    try {
      const res = await backofficeLogin({ email: values.email, password: values.password })
      localStorage.setItem('token', res.accessToken)
      localStorage.setItem('role', res.role)
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
    <div className="flex min-h-screen items-center justify-center bg-[#EDF2ED]">
      <div className="w-full max-w-sm rounded-2xl bg-white p-10 shadow-md">

        <div className="mb-8 flex flex-col items-center gap-3">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#D1CAEF]">
            <img src={hojita} alt="" className="h-8 w-8 object-contain mix-blend-multiply" />
          </div>
          <img src={colorLogo} alt="Huly" className="h-8 object-contain" />
          <p className="text-xs font-bold uppercase tracking-widest text-[#A0AEC0]">
            Backoffice
          </p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-[#4A5568]">Email</label>
            <input
              type="text"
              value={values.email}
              onChange={e => handleChange('email', e.target.value)}
              placeholder="Correo electrónico"
              className="rounded-xl border border-gray-200 px-4 py-2.5 text-sm outline-none focus:border-[#8869AC] focus:ring-2 focus:ring-[#8869AC]/20 transition"
            />
            {errors.email && (
              <p className="text-xs text-red-500">{errors.email}</p>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-[#4A5568]">Contraseña</label>
            <input
              type="password"
              value={values.password}
              onChange={e => handleChange('password', e.target.value)}
              placeholder="••••••••"
              className="rounded-xl border border-gray-200 px-4 py-2.5 text-sm outline-none focus:border-[#8869AC] focus:ring-2 focus:ring-[#8869AC]/20 transition"
            />
            {errors.password && (
              <p className="text-xs text-red-500">{errors.password}</p>
            )}
          </div>

          {apiError && (
            <p className="rounded-xl bg-red-50 px-4 py-2.5 text-sm text-red-600">
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
