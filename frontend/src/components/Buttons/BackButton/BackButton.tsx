import { useNavigate } from 'react-router-dom'

interface BackButtonProps {
  to: string
  className?: string
  onBeforeNavigate?: () => Promise<void> | void
}

export default function BackButton({ to, className = '', onBeforeNavigate }: BackButtonProps) {
  const navigate = useNavigate()

  return (
    <button
      type="button"
      onClick={async () => {
        await onBeforeNavigate?.()
        navigate(to)
      }}
      className={`fixed top-20 left-6 rounded-full bg-[var(--surface-tertiary)] px-4 py-2 
        text-sm text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110 z-50 flex items-center gap-2 ${className}`}    >
      ← Volver
    </button>
  )
}
