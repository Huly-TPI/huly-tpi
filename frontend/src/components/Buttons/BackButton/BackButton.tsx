import { useNavigate } from 'react-router-dom'

interface BackButtonProps {
  to: string
  className?: string
}

export default function BackButton({ to, className = '' }: BackButtonProps) {
  const navigate = useNavigate()

  return (
    <button
      type="button"      onClick={() => navigate(to, { viewTransition: true })}
      className={`fixed top-20 left-6 bg-white/80 backdrop-blur-sm rounded-full px-4 py-2 text-violeta text-sm flex items-center gap-2 shadow-sm hover:bg-white transition-colors z-50 ${className}`}
    >
      ← Volver
    </button>
  )
}
