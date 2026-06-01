import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { backofficeLogin } from '../../api/auth'
import colorLogo from '../../assets/brand/color-logo.webp'
import hojita from '../../assets/backoffice/hojita.webp'

export default function BackofficeLogin() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const res = await backofficeLogin({ email, password })
      localStorage.setItem('token', res.accessToken)
      localStorage.setItem('role', res.role)
      navigate('/backoffice')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error inesperado'
      setError(
        message === 'Invalid credentials'
          ? 'Email o contraseña incorrectos'
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
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              placeholder="Correo electrónico"
              className="rounded-xl border border-gray-200 px-4 py-2.5 text-sm outline-none focus:border-[#8869AC] focus:ring-2 focus:ring-[#8869AC]/20 transition"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-[#4A5568]">Contraseña</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              placeholder="••••••••"
              className="rounded-xl border border-gray-200 px-4 py-2.5 text-sm outline-none focus:border-[#8869AC] focus:ring-2 focus:ring-[#8869AC]/20 transition"
            />
          </div>

          {error && (
            <p className="rounded-xl bg-red-50 px-4 py-2.5 text-sm text-red-600">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="mt-2 rounded-xl bg-[#8869AC] py-2.5 text-sm font-bold text-white transition hover:bg-[#7559a0] disabled:opacity-60"
          >
            {loading ? 'Ingresando...' : 'Iniciar sesión'}
          </button>
        </form>
      </div>
    </div>
  )
}
