import { useNavigate } from 'react-router-dom'

interface BackButtonProps {
  to: string
  className?: string
  label?: string
  onBeforeNavigate?: () => Promise<void> | void
}

export default function BackButton({ to, className = '', label = 'Volver', onBeforeNavigate }: BackButtonProps) {
  const navigate = useNavigate()

  return (
    <button
      type="button"
      onClick={async () => {
        await onBeforeNavigate?.()
        navigate(to)
      }}
      className={`fixed top-20 left-6 z-50 flex items-center gap-2 rounded-full border-[2px] border-transparent bg-[var(--surface-tertiary)] px-4 py-2 text-sm text-violeta shadow-sm backdrop-blur-sm transition-colors hover:brightness-110 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violeta/60 dark:border-white/10 dark:bg-[rgba(23,32,51,0.92)] dark:text-violeta-claro dark:shadow-[0_8px_22px_rgba(2,6,23,0.32)] dark:hover:bg-[rgba(30,41,59,0.96)] dark:hover:brightness-100 ${className}`}
      aria-label={label}
    >
      <span aria-hidden="true" className="leading-none">
        ←
      </span>
      <span className="hidden sm:inline">{label}</span>
    </button>
  )
}
